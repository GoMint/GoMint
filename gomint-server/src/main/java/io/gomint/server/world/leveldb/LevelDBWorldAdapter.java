/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.leveldb;

import com.google.common.io.Files;
import io.gomint.leveldb.DB;
import io.gomint.leveldb.Iterator;
import io.gomint.leveldb.LibraryLoader;
import io.gomint.leveldb.WriteBatch;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.GoMintServer;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.plugin.PluginClassloader;
import io.gomint.server.util.Allocator;
import io.gomint.server.util.DumpUtil;
import io.gomint.server.util.Precondition;
import io.gomint.server.util.Values;
import io.gomint.server.world.BlockRuntimeIDs;
import io.gomint.server.world.ChunkAdapter;
import io.gomint.server.world.ChunkCache;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.WorldCreateException;
import io.gomint.server.world.WorldLoadException;
import io.gomint.server.world.block.Block;
import io.gomint.taglib.AllocationLimitReachedException;
import io.gomint.taglib.NBTReader;
import io.gomint.taglib.NBTStream;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Chunk;
import io.gomint.world.Difficulty;
import io.gomint.world.Gamerule;
import io.gomint.world.generator.ChunkGenerator;
import io.gomint.world.generator.GeneratorContext;
import io.gomint.world.generator.integrated.LayeredGenerator;
import io.gomint.world.generator.integrated.NormalGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author geNAZt
 * @version 1.0
 */
public class LevelDBWorldAdapter extends WorldAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelDBWorldAdapter.class);

    static {
        if (!LibraryLoader.load()) {
            System.out.println("Could not load native leveldb. Please be sure you have a supported OS installed");
            System.exit(-1);
        }
    }

    private DB db;
    private int worldVersion;

    // Generator things
    private int generatorType;
    private int generatorVersion;
    private String generatorOptions;
    private long generatorSeed;

    // Custom generators
    private Class<? extends ChunkGenerator> generatorClass;
    private GeneratorContext generatorContext;

    private LevelDBWorldAdapter(final GoMintServer server, final String name, final Class<? extends ChunkGenerator> generator) throws WorldCreateException {
        super(server, new File(name), name);
        this.chunkCache = new ChunkCache(this);

        // Build up generator
        GeneratorContext context = new GeneratorContext();
        this.constructGenerator(generator, context);

        // Generate a spawnpoint
        BlockPosition spawnPoint = this.chunkGenerator.spawnPoint();
        this.spawn = new Location(this, spawnPoint.x(), spawnPoint.y(), spawnPoint.z());

        // Take over level name
        this.levelName = name;

        // We need a level.dat
        try {
            this.saveLevelDat();
        } catch (IOException e) {
            throw new WorldCreateException("level.dat for world '" + name + "' could not be saved", e);
        }

        try {
            this.open();
        } catch (WorldLoadException e) {
            throw new WorldCreateException("Could not open/load world", e);
        }
    }

    /**
     * Construct and init a new levedb based World
     *
     * @param server   which has requested to load this world
     * @param worldDir the folder where the world should be in
     * @param name     of this world
     * @throws WorldLoadException Thrown in case the world could not be loaded successfully
     */
    LevelDBWorldAdapter(GoMintServer server, File worldDir, String name) throws WorldLoadException {
        super(server, worldDir, name);
        this.chunkCache = new ChunkCache(this);

        this.loadLevelDat();

        // We only support storage version 3 and up (MC:PE >= 1.0)
        if (this.worldVersion < 8) {
            throw new WorldLoadException("Version of the world is too old. Please update your MC:PE and import this world. After that you can use the exported version again.");
        }

        this.prepareGenerator();
        this.open();
    }

    private void open() throws WorldLoadException {
        try {
            this.db = new DB(new File(this.worldDir, "db"));
            this.db.open();
        } catch (Exception e) {
            throw new WorldLoadException("Could not open leveldb connection: " + e.getMessage());
        }

        this.prepareSpawnRegion();

        // Adjust spawn location if needed
        this.adjustSpawn();
    }

    /**
     * Create a new leveldb based world. This will not override old worlds. It will fail with a WorldCreateException when
     * a folder has been found with the same name (regardless of the content of that folder)
     *
     * @param server    which wants to create this world
     * @param name      of the new world
     * @param generator which is used to generate this worlds chunks and spawn point
     * @return new world
     * @throws WorldCreateException when there already is a world or a error during creating occured
     */
    public static LevelDBWorldAdapter create(GoMintServer server, String name, Class<? extends ChunkGenerator> generator) throws WorldCreateException {
        File worldFolder = new File(name);
        if (worldFolder.exists()) {
            throw new WorldCreateException("Folder with name '" + name + "' already exists");
        }

        if (!worldFolder.mkdir()) {
            throw new WorldCreateException("World '" + name + "' could not be created. Folder could not be created");
        }

        File regionFolder = new File(worldFolder, "db");
        if (!regionFolder.mkdir()) {
            throw new WorldCreateException("World '" + name + "' could not be created. Folder could not be created");
        }

        return new LevelDBWorldAdapter(server, name, generator);
    }

    private void saveLevelDat() throws IOException {
        File levelDat = new File(this.worldDir, "level.dat");
        if (levelDat.exists()) {
            // Backup old level.dat
            Files.copy(levelDat, new File(this.worldDir, "level.dat.bak"));

            // Delete the old one
            levelDat.delete();
        }

        //
        NBTTagCompound compound = new NBTTagCompound("");

        // Add version number
        compound.addValue("StorageVersion", 8);

        // Spawn
        compound.addValue("SpawnX", (int) this.spawn.x());
        compound.addValue("SpawnY", (int) this.spawn.y());
        compound.addValue("SpawnZ", (int) this.spawn.z());
        compound.addValue("Difficulty", this.difficulty.getDifficultyDegree());

        // Level name
        compound.addValue("LevelName", this.levelName);

        // Time
        compound.addValue("Time", (long) this.worldTime);

        // Gamerules
        for (Map.Entry<Gamerule<?>, Object> entry : this.gamerules.entrySet()) {
            compound.addValue(entry.getKey().name(), entry.getKey().createNBTValue(entry.getValue()));
        }

        // Save generator
        this.saveGenerator(compound);

        // Save level.dat
        try (FileOutputStream stream = new FileOutputStream(levelDat)) {
            stream.write(new byte[8]);

            ByteBuf data = PooledByteBufAllocator.DEFAULT.heapBuffer();
            compound.writeTo(data, ByteOrder.LITTLE_ENDIAN);
            stream.write(data.array(), data.arrayOffset(), data.readableBytes());
            data.release();
        }
    }

    private void saveGenerator(NBTTagCompound compound) {
        if (this.chunkGenerator instanceof NormalGenerator) {
            compound.addValue("Generator", 1);
            compound.addValue("RandomSeed", (long) this.chunkGenerator.context().get("seed"));
        } else if (this.chunkGenerator instanceof LayeredGenerator) {
            compound.addValue("Generator", 2);
            compound.addValue("FlatWorldLayers", "{}"); // TODO: Better persist solution for generators
        } else {
            compound.addValue("Generator", -1);
            compound.addValue("GeneratorClass", this.chunkGenerator.getClass().getName());
            compound.addValue("GeneratorContext", this.chunkGenerator.context().toString());
        }
    }

    /**
     * Loads an leveldb world given the path to the world's directory. This operation
     * performs synchronously and will at least load the entire spawn region before
     * completing.
     *
     * @param server      The GoMint Server which runs this
     * @param pathToWorld The path to the world's directory
     * @return The leveldb world adapter used to access the world
     * @throws WorldLoadException Thrown in case the world could not be loaded successfully
     */
    public static LevelDBWorldAdapter load(GoMintServer server, File pathToWorld) throws WorldLoadException {
        return new LevelDBWorldAdapter(server, pathToWorld, pathToWorld.getName());
    }

    @Override
    public WorldAdapter spawnLocation(Location location) {
        super.spawnLocation(location);
        this.sneakySaveLevelDat();
        return this;
    }

    @Override
    public WorldAdapter difficulty(Difficulty difficulty) {
        super.difficulty(difficulty);
        this.sneakySaveLevelDat();
        return this;
    }

    @Override
    protected void prepareGenerator() {
        if (this.generatorType == -1) { // Custom generators
            try {
                constructGenerator(this.generatorClass, this.generatorContext);
            } catch (WorldCreateException e) {
                this.logger().error("Could not reconstruct generator class", e);
            }
        } else {
            Generators generators = Generators.valueOf(this.generatorType);
            if (generators != null) {
                switch (generators) {
                    case NORMAL:
                        GeneratorContext context = new GeneratorContext();
                        context.put("seed", this.generatorSeed);
                        this.chunkGenerator = new NormalGenerator(this, context);
                        break;

                    case FLAT:
                        context = new GeneratorContext();

                        // Check for flat configuration
                        JSONParser parser = new JSONParser();
                        try {
                            List<Block> blocks = new ArrayList<>();

                            JSONObject jsonObject = (JSONObject) parser.parse(this.generatorOptions);
                            if (jsonObject != null && jsonObject.containsKey("block_layers")) {
                                JSONArray blockLayers = (JSONArray) jsonObject.get("block_layers");
                                for (Object layer : blockLayers) {
                                    JSONObject layerConfig = (JSONObject) layer;
                                    int count = 1;
                                    if (layerConfig.containsKey("count")) {
                                        count = ((Long) layerConfig.get("count")).intValue();
                                    }

                                    // TODO: look at new format of the flat layers
                                    String blockId = ((String) layerConfig.get("block_name"));
                                    byte blockData = ((Long) layerConfig.get("block_data")).byteValue();

                                    Block block = this.server.blocks().get(BlockRuntimeIDs.toBlockIdentifier(blockId, null));
                                    for ( int i = 0; i < count; i++ ) {
                                        blocks.add( block );
                                    }
                                }
                            }

                            context.put("amountOfLayers", blocks.size());
                            int i = 0;
                            for (Block block : blocks) {
                                context.put("layer." + (i++), block);
                            }
                        } catch (ParseException e) {
                            // Ignore this, if this happens the context is empty and the generator will fallback to default
                            // behaviour
                        }

                        this.chunkGenerator = new LayeredGenerator(this, context);
                        break;
                }
            }
        }
    }

    public ByteBuf getKey(int chunkX, int chunkZ, byte dataType) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(9);
        return buf.writeIntLE(chunkX).writeIntLE(chunkZ).writeByte(dataType);
    }

    public ByteBuf getKeySubChunk(int chunkX, int chunkZ, byte dataType, byte subChunk) {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(10);
        return buf.writeIntLE(chunkX).writeIntLE(chunkZ).writeByte(dataType).writeByte(subChunk);
    }

    private void loadLevelDat() throws WorldLoadException {
        File levelDat = new File(this.worldDir, "level.dat");
        if (!levelDat.exists() || !levelDat.isFile()) {
            throw new WorldLoadException("Missing level.dat");
        }

        // Default the settings
        this.levelName = "";
        this.spawn = new Location(this, 0, 0, 0);
        this.worldVersion = 0;

        try (FileInputStream stream = new FileInputStream(levelDat)) {
            // Skip some data. For example the amount of bytes of this NBT Tag
            stream.skip(8);

            byte[] data = stream.readAllBytes();
            ByteBuf buf = Allocator.allocate(data);

            NBTStream nbtStream = new NBTStream(buf, ByteOrder.LITTLE_ENDIAN);
            nbtStream.addListener((path, value) -> {
                switch (path) {
                    case ".Time":
                        LevelDBWorldAdapter.this.worldTime = ((Long) value).intValue();
                        break;
                    case ".GeneratorClass":
                        LevelDBWorldAdapter.this.generatorClass = (Class<? extends ChunkGenerator>) PluginClassloader.find((String) value);
                        break;
                    case ".GeneratorContext":
                        LevelDBWorldAdapter.this.generatorContext = new GeneratorContext();

                        String jsonContext = (String) value;
                        JSONParser parser = new JSONParser();
                        JSONObject jsonObject = (JSONObject) parser.parse(jsonContext);
                        jsonObject.forEach((o, o2) -> LevelDBWorldAdapter.this.generatorContext.put((String) o, o2));

                        break;
                    case ".RandomSeed":
                        LevelDBWorldAdapter.this.generatorSeed = (long) value;
                        break;
                    case ".FlatWorldLayers":
                        LevelDBWorldAdapter.this.generatorOptions = (String) value;
                        break;
                    case ".Generator":
                        LevelDBWorldAdapter.this.generatorType = (int) value;
                        break;
                    case ".LevelName":
                        LevelDBWorldAdapter.this.levelName = (String) value;
                        break;
                    case ".SpawnX":
                        LevelDBWorldAdapter.this.spawn.x((int) value);
                        break;
                    case ".SpawnY":
                        LevelDBWorldAdapter.this.spawn.y((int) value);
                        break;
                    case ".SpawnZ":
                        LevelDBWorldAdapter.this.spawn.z((int) value);
                        break;
                    case ".StorageVersion":
                        LevelDBWorldAdapter.this.worldVersion = (int) value;
                        break;
                    case ".Difficulty":
                        LevelDBWorldAdapter.this.difficulty = Difficulty.valueOf((int) value);
                        break;
                    default:
                        // Check for game rule
                        Gamerule<?> gamerule = Gamerule.byNBTName(path.substring(1));
                        if (gamerule != null) {
                            this.gamerule(gamerule, gamerule.createValueFromString(value));
                        }

                        break;
                }
            });

            // CHECKSTYLE:OFF
            try {
                nbtStream.parse();
            } catch (Exception e) {
                throw new WorldLoadException("Could not load level.dat NBT: " + e.getMessage());
            }
            // CHECKSTYLE:ON

            buf.release();
        } catch (IOException e) {
            throw new WorldLoadException("Failed to load leveldb world: " + e.getMessage());
        }
    }

    @Override
    public ChunkAdapter loadChunk(int x, int z, boolean generate) {
        Precondition.safeWorldAccess(this, true);
        ChunkAdapter chunk = this.chunkCache.getChunk(x, z);
        if (chunk == null) {
            DB.Snapshot snapshot = this.db.getSnapshot();

            // Check if we have a version
            ByteBuf key = this.getKey(x, z, (byte) 0x2c);
            byte[] version = this.db.get(snapshot, key);
            key.release();

            if (version == null) {
                key = this.getKey(x, z, (byte) 0x76);
                version = this.db.get(snapshot, key);
                key.release();
            }

            if (version == null) {
                if (generate) {
                    return this.generate(x, z, false);
                } else {
                    return (ChunkAdapter) this.generateEmptyChunk(x, z);
                }
            }

            // Get the finalized value, only needed for vanilla though, other implementations don't use this (null = true)
            key = this.getKey(x, z, (byte) 0x36);
            byte[] finalized = this.db.get(snapshot, key);
            key.release();

            // byte v = version[0];
            boolean populated = finalized == null || finalized[0] == 2;

            LevelDBChunkAdapter loadingChunk = new LevelDBChunkAdapter(this, x, z, populated);
            loadingChunk.prepareVersion(version[0]);

            for (int sectionY = 0; sectionY < 16; sectionY++) {
                key = this.getKeySubChunk(x, z, (byte) 0x2f, (byte) sectionY);
                byte[] chunkData = this.db.get(snapshot, key);
                key.release();

                if (chunkData != null) {
                    loadingChunk.loadSection(sectionY, chunkData);
                } else {
                    break;
                }
            }

            key = this.getKey(x, z, (byte) 0x31);
            byte[] tileEntityData = this.db.get(snapshot, key);
            key.release();

            if (tileEntityData != null) {
                loadingChunk.loadTileEntities(tileEntityData);
            }

            key = this.getKey(x, z, (byte) 0x2d);
            byte[] biomes = this.db.get(snapshot, key);
            key.release();

            if (biomes != null && biomes.length == 768) { // There are 256 bytes versions of this which only contain 0 bytes
                loadingChunk.loadHeightAndBiomes(biomes);
            } else {
                loadingChunk.calculateHeightmap(240);
            }

            // Give it into the chunk cache before we populate
            if (!this.chunkCache.putChunk(loadingChunk)) {
                loadingChunk.release();
                return this.chunkCache.getChunk(x, z);
            }

            // Do some work on the chunk if needed (like population)
            if (!populated) {
                this.addPopulateTask(loadingChunk);
            }

            // Load entities
            key = this.getKey(x, z, (byte) 0x32);
            byte[] entityData = this.db.get(snapshot, key);
            key.release();

            if (entityData != null) {
                loadingChunk.loadEntities(entityData);
            }

            snapshot.close();
            return loadingChunk;
        }

        return chunk;
    }

    @Override
    protected void saveChunk(ChunkAdapter chunk) {
        if (chunk == null) {
            return;
        }

        LevelDBChunkAdapter adapter = (LevelDBChunkAdapter) chunk;
        adapter.save(this.db);
    }

    @Override
    protected void closeFDs() {
        try {
            this.db.close();
        } catch (Exception e) {
            this.logger.error("Could not close leveldb", e);
        }
    }

    private byte[] getPersistenceId(UUID uuid) {
        String key = "player_" + uuid.toString();

        ByteBuf playerInfoKey = PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(key.getBytes());
        byte[] playerInfoNbt = this.db.get(playerInfoKey);
        playerInfoKey.release();

        if (playerInfoNbt == null) {
            return null;
        }

        ByteBuf buf = Unpooled.wrappedBuffer(playerInfoNbt);
        NBTReader reader = new NBTReader(buf, ByteOrder.LITTLE_ENDIAN);
        try {
            NBTTagCompound playerInfoCompound = reader.parse();
            byte[] serverId = playerInfoCompound.getString("ServerId", "").getBytes();
            if (serverId.length > 0) {
                return serverId;
            }
        } catch (IOException | AllocationLimitReachedException e) {
            this.logger.warn("Could not load player information for loading", e);
        }

        return null;
    }

    @Override
    public void persistPlayer(EntityPlayer player) {
        ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.directBuffer();

        NBTTagCompound compound = player.persistToNBT();
        try {
            WriteBatch batch = new WriteBatch();

            compound.writeTo(buf, ByteOrder.LITTLE_ENDIAN);
            byte[] persistenceId = this.getPersistenceId(player.uuid());
            if (persistenceId == null) {
                UUID uuid = UuidUtil.getTimeBasedUuid();

                NBTTagCompound persistenceIdInformation = new NBTTagCompound("");
                persistenceIdInformation.addValue("MsaId", player.uuid().toString());
                persistenceIdInformation.addValue("SelfSignedId", "");
                persistenceIdInformation.addValue("ServerId", "player_server_" + uuid.toString());

                ByteBuf pbuf = UnpooledByteBufAllocator.DEFAULT.directBuffer();
                persistenceIdInformation.writeTo(pbuf, ByteOrder.LITTLE_ENDIAN);

                persistenceId = ("player_server_" + uuid.toString()).getBytes();
                batch.put(PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(("player_" + player.uuid()).getBytes()), pbuf);
            }

            batch.put(PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(persistenceId), buf);
            this.db.write(batch);

            batch.clear();
            batch.close();
        } catch (IOException e) {
            this.logger.warn("Could not persist player information", e);
        }
    }

    @Override
    public boolean loadPlayer(EntityPlayer player) {
        try {
            byte[] persistenceId = this.getPersistenceId(player.uuid());
            if (persistenceId != null) {
                ByteBuf playerPersistenceId = PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(persistenceId);
                byte[] playerNbtData = this.db.get(playerPersistenceId);
                playerPersistenceId.release();

                ByteBuf buf = Unpooled.wrappedBuffer(playerNbtData);
                NBTReader reader = new NBTReader(buf, ByteOrder.LITTLE_ENDIAN);

                NBTTagCompound playerNbt = reader.parse();
                player.initFromNBT(playerNbt);
                return true;
            }
        } catch (IOException | AllocationLimitReachedException e) {
            this.logger.warn("Could not load player information", e);
        }

        return false;
    }

    @Override
    public Chunk generateEmptyChunk(int x, int z) {
        return new LevelDBChunkAdapter(this, x, z);
    }

    protected final void sneakySaveLevelDat() {
        try {
            this.saveLevelDat();
        } catch (IOException cause) {
            this.logger.warn("level.dat for world '{}' could not be saved: ", this.levelName, cause);
        }
    }

}
