/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world;

import io.gomint.entity.Entity;
import io.gomint.entity.EntityPlayer;
import io.gomint.event.player.PlayerInteractEvent;
import io.gomint.event.world.WorldLoadEvent;
import io.gomint.event.world.WorldUnloadEvent;
import io.gomint.inventory.item.ItemAir;
import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemType;
import io.gomint.math.AxisAlignedBB;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.scheduler.Task;
import io.gomint.scheduler.WorldScheduler;
import io.gomint.server.GoMintServer;
import io.gomint.server.async.Delegate;
import io.gomint.server.async.Delegate2;
import io.gomint.server.async.MultiOutputDelegate;
import io.gomint.server.config.WorldConfig;
import io.gomint.server.entity.passive.EntityItem;
import io.gomint.server.entity.passive.EntityXPOrb;
import io.gomint.server.entity.tileentity.SerializationReason;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.maintenance.ReportUploader;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketSetDifficulty;
import io.gomint.server.network.packet.PacketSetSpawnPosition;
import io.gomint.server.network.packet.PacketTileEntityData;
import io.gomint.server.network.packet.PacketUpdateBlock;
import io.gomint.server.network.packet.PacketWorldEvent;
import io.gomint.server.network.packet.PacketWorldSoundEvent;
import io.gomint.server.plugin.PluginClassloader;
import io.gomint.server.scheduler.AsyncScheduler;
import io.gomint.server.scheduler.SyncSchedulerAdapter;
import io.gomint.server.scheduler.SyncTaskManager;
import io.gomint.server.util.CallerDetectorUtil;
import io.gomint.server.util.EnumConnectors;
import io.gomint.server.util.ExceptionUtil;
import io.gomint.server.util.Precondition;
import io.gomint.server.util.Values;
import io.gomint.server.util.tick.ClientTickable;
import io.gomint.server.util.tick.Tickable;
import io.gomint.server.world.block.Air;
import io.gomint.server.world.storage.TemporaryStorage;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Biome;
import io.gomint.world.Chunk;
import io.gomint.world.Difficulty;
import io.gomint.world.Gamemode;
import io.gomint.world.Gamerule;
import io.gomint.world.Particle;
import io.gomint.world.ParticleData;
import io.gomint.world.Sound;
import io.gomint.world.SoundData;
import io.gomint.world.World;
import io.gomint.world.WorldLayer;
import io.gomint.world.block.Block;
import io.gomint.world.block.data.Facing;
import io.gomint.world.generator.ChunkGenerator;
import io.gomint.world.generator.GeneratorContext;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author BlackyPaw
 * @author Janmm14
 * @version 1.0
 */
public abstract class WorldAdapter extends ClientTickable implements World, Tickable {

    // Shared objects
    protected final GoMintServer server;
    protected final Logger logger;

    // World properties
    protected final File worldDir;
    protected String levelName;
    protected Location spawn;
    protected Map<Gamerule<?>, Object> gamerules = new HashMap<>();
    private WorldConfig config;
    protected int worldTime; // Stored in ticks

    private int lastWorldTimeUpdate;

    /**
     * Get the difficulty of this world
     */
    protected Difficulty difficulty = Difficulty.NORMAL;

    // Chunk Handling
    protected ChunkCache chunkCache;
    protected ChunkGenerator chunkGenerator;

    // Entity Handling
    private EntityManager entityManager;
    private EntitySpawner entitySpawner;

    // Block ticking
    int lcg = ThreadLocalRandom.current().nextInt();
    private TickList tickQueue = new TickList();

    // I/O
    private AtomicBoolean asyncWorkerRunning;
    private Task asyncWorkerTask;
    private BlockingQueue<AsyncChunkTask> asyncChunkTasks;
    private Queue<AsyncChunkPackageTask> chunkPackageTasks;

    // Scheduler
    private SyncTaskManager syncTaskManager;
    private SyncSchedulerAdapter syncSchedulerAdapter;
    private long asyncAllowedThreadId = -1;

    // General ticking
    private long threadId = -1;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private long currentTickTime;
    private long sleepBalance;
    private AtomicReference<Consumer<EntityPlayer>> unloadPlayerConsumer = new AtomicReference<>();
    private AtomicReference<Runnable> unloadInternalListener = new AtomicReference<>();

    // Additional informations for API usage
    private double tps;

    // EntityPlayer handling
    private Object2ObjectMap<io.gomint.server.entity.EntityPlayer, ChunkAdapter> players;
    private List<PlayerConnection> newConnections;

    private final String name;

    protected WorldAdapter(GoMintServer server, File worldDir, String name) {
        this.name = name;
        this.server = server;
        this.logger = LoggerFactory.getLogger("io.gomint.World-" + worldDir.getName());
        this.worldDir = worldDir;
        this.entityManager = new EntityManager(this);
        this.config = this.server.worldConfigOf(name);
        this.players = new Object2ObjectOpenHashMap<>();
        this.newConnections = new ArrayList<>();
        this.asyncChunkTasks = new LinkedBlockingQueue<>();
        this.chunkPackageTasks = new ConcurrentLinkedQueue<>();
        this.entitySpawner = new EntitySpawner(this);
        this.syncTaskManager = new SyncTaskManager();
        this.syncSchedulerAdapter = new SyncSchedulerAdapter(this.syncTaskManager);
        this.startAsyncWorker(server.asyncScheduler());
        this.initGamerules();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldAdapter that = (WorldAdapter) o;
        return Objects.equals(this.worldDir, that.worldDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldDir);
    }

    @Override
    public String toString() {
        return "WorldAdapter{" +
            "worldDir=" + this.worldDir +
            ", levelName='" + this.levelName + '\'' +
            '}';
    }

    public GoMintServer server() {
        return this.server;
    }

    public Logger logger() {
        return this.logger;
    }

    public File worldDir() {
        return this.worldDir;
    }

    public Map<Gamerule<?>, Object> gamerules() {
        return this.gamerules;
    }

    public WorldConfig config() {
        return this.config;
    }

    @Override
    public Difficulty difficulty() {
        return this.difficulty;
    }

    // ==================================== GENERAL ACCESSORS ==================================== //

    /**
     * Get the current view of players on this world.
     *
     * @return The Collection View of the Players currently on this world
     */
    public Object2ObjectMap<io.gomint.server.entity.EntityPlayer, ChunkAdapter> getPlayers0() {
        return this.players;
    }

    public void addNewConnection(PlayerConnection connection) {
        this.newConnections.add(connection);
    }

    /**
     * Get a collection (set) of all players online on this world
     *
     * @return collection of all players online on this world
     */
    @Override
    public Set<EntityPlayer> onlinePlayers() {
        Precondition.safeWorldAccess(this, false);
        return Collections.unmodifiableSet(this.players.keySet());
    }

    @Override
    public WorldAdapter playSound(Vector vector, Sound sound, byte pitch, SoundData data) {
        return this.playSound(null, vector, sound, pitch, data);
    }

    /**
     * Play a sound at the location given
     *
     * @param player Which should get this sound, if null all get the sound
     * @param vector The location where the sound should be played
     * @param sound  The sound which should be played
     * @param pitch  The pitch at which the sound should be played
     * @param data   additional data for the sound
     * @throws IllegalArgumentException when the sound data given is incorrect for the sound wanted to play
     */
    public WorldAdapter playSound(EntityPlayer player, Vector vector, Sound sound, byte pitch, SoundData data) {
        int soundData = -1;

        switch (sound) {
            case LAND:
            case BREAK_BLOCK:
            case PLACE:
            case HIT:
                // Need a block
                if (data.block() == null) {
                    throw new IllegalArgumentException("Sound " + sound + " needs block sound data");
                }

                soundData = BlockRuntimeIDs.toBlockIdentifier(this.server.blocks().getID(data.block()), null).runtimeId();
                break;

            case NOTE:
                // Check if needed data is there
                if (data.instrument() == null) {
                    throw new IllegalArgumentException("Sound NOTE needs instrument sound data");
                }

                switch (data.instrument()) {
                    case PIANO:
                        soundData = 0;
                        break;
                    case BASS_DRUM:
                        soundData = 1;
                        break;
                    case CLICK:
                        soundData = 2;
                        break;
                    case TABOUR:
                        soundData = 3;
                        break;
                    case BASS:
                        soundData = 4;
                        break;
                    default:
                        soundData = -1;
                        break;
                }

                break;

            default:
                break;
        }

        return this.playSound(player, vector, sound, pitch, soundData);
    }

    @Override
    public World playSound(Vector vector, Sound sound, byte pitch) {
        return this.playSound(null, vector, sound, pitch, -1);
    }

    /**
     * Play a sound at the location given
     *
     * @param player    Which should get this sound, if null all get the sound
     * @param vector    The location where the sound should be played
     * @param sound     The sound which should be played
     * @param pitch     The pitch at which the sound should be played
     * @param extraData any data which should be send to the client to identify the sound
     */
    public WorldAdapter playSound(EntityPlayer player, Vector vector, Sound sound, byte pitch, int extraData) {
        // There are sounds which don't work but have level event counterparts so we use them for now
        switch (sound) {
            case IMITATE_GHAST:
                this.sendLevelEvent(player, vector, LevelEvent.SOUND_GHAST, pitch);

                break;
            default:
                PacketWorldSoundEvent soundPacket = new PacketWorldSoundEvent();
                soundPacket.setType(EnumConnectors.SOUND_CONNECTOR.convert(sound));
                // soundPacket.setPitch( pitch );
                soundPacket.setExtraData(extraData);
                soundPacket.setPosition(vector);

                if (player == null) {
                    Precondition.safeWorldAccess(this, false);
                    sendToVisible(vector.toBlockPosition(), soundPacket, entity -> true);
                } else {
                    io.gomint.server.entity.EntityPlayer implPlayer = (io.gomint.server.entity.EntityPlayer) player;
                    implPlayer.connection().addToSendQueue(soundPacket);
                }

                break;
        }

        return this;
    }

    @Override
    public String folder() {
        return this.name;
    }

    @Override
    public String name() {
        return this.levelName;
    }

    @Override
    public WorldAdapter spawnLocation(Location location) {
        Precondition.safeWorldAccess(this, false);
        this.spawn = Objects.requireNonNull(location, "Failed reassigning spawn location: Param 'location' is null");

        for (io.gomint.server.entity.EntityPlayer player : this.players.keySet()) {
            PacketSetSpawnPosition packet = new PacketSetSpawnPosition();
            packet.setSpawnType(PacketSetSpawnPosition.SpawnType.WORLD);
            packet.setPlayerPosition(player.position().toBlockPosition());
            packet.setWorldSpawn(location.toBlockPosition());
            player.connection().addToSendQueue(packet);
        }

        return this;
    }

    @Override
    public Location spawnLocation() {
        return this.spawn;
    }

    @Override
    public WorldAdapter difficulty(Difficulty difficulty) {
        Precondition.safeWorldAccess(this, false);
        this.difficulty = difficulty;

        PacketSetDifficulty packet = new PacketSetDifficulty();
        packet.setDifficulty(difficulty.getDifficultyDegree());

        this.broadcastPacket(packet);

        return this;
    }

    @Override
    public <T extends Block> T blockAt(BlockPosition pos) {
        return this.blockAt(pos.x(), pos.y(), pos.z());
    }

    private Block getOptionalBlockAt(BlockPosition position) {
        if (position.y() < 0 || position.y() > 255) {
            return null;
        }

        ChunkAdapter chunkAdapter = this.getChunk(position.x() >> 4, position.z() >> 4);
        if (chunkAdapter != null) {
            return chunkAdapter.blockAt(position.x() & 0xF, position.y(), position.z() & 0xF);
        }

        return null;
    }

    @Override
    public <T extends Block> T blockAt(int x, int y, int z) {
        return this.blockAt(x, y, z, WorldLayer.NORMAL);
    }

    @Override
    public <T extends Block> T blockAt(int x, int y, int z, WorldLayer layer) {
        Precondition.safeWorldAccess(this, true);
        // Secure location
        if (y < 0 || y > 255) {
            return (T) this.server.blocks().get(BlockRuntimeIDs.toBlockIdentifier("minecraft:air", null),
                (byte) (y > 255 ? 15 : 0), (byte) 0, null, new Location(this, x, y, z), new BlockPosition(x, y, z), layer.ordinal(), null, (short) 0);
        }

        ChunkAdapter chunk = this.loadChunk(x >> 4, z >> 4, false);
        if (chunk == null) {
            return (T) this.server.blocks().get(BlockRuntimeIDs.toBlockIdentifier("minecraft:air", null),
                (byte) (y > 255 ? 15 : 0), (byte) 0, null, new Location(this, x, y, z), new BlockPosition(x, y, z), layer.ordinal(), null, (short) 0);
        }

        return chunk.blockAt(x & 0xF, y, z & 0xF, layer.ordinal());
    }

    public void setBlock(BlockPosition pos, int layer, int runtimeId) {
        final ChunkAdapter chunk = this.loadChunk(
            CoordinateUtils.fromBlockToChunk(pos.x()),
            CoordinateUtils.fromBlockToChunk(pos.z()),
            true);

        chunk.block(pos.x() & 0xF, pos.y(), pos.z() & 0xF, layer, runtimeId);
    }

    public int getRuntimeID(BlockPosition position, int layer) {
        // Sanity check
        if (position.y() < 0) {
            this.logger.warn("Got request for block under y 0", new Exception());
            return 0;
        }

        final ChunkAdapter chunk = this.loadChunk(
            CoordinateUtils.fromBlockToChunk(position.x()),
            CoordinateUtils.fromBlockToChunk(position.z()),
            true);

        return chunk.getRuntimeID(position.x() & 0xF, position.y(), position.z() & 0xF, layer);
    }

    /**
     * Get the current id at the given location
     *
     * @param position where we want to search
     * @param layer    where the block is
     * @return id of the block
     */
    public String getBlockId(BlockPosition position, int layer) {
        // Sanity check
        if (position.y() < 0) {
            this.logger.warn("Got request for block under y 0", new Exception());
            return "minecraft:air";
        }

        final ChunkAdapter chunk = this.loadChunk(
            CoordinateUtils.fromBlockToChunk(position.x()),
            CoordinateUtils.fromBlockToChunk(position.z()),
            true);

        return chunk.getBlock(position.x() & 0xF, position.y(), position.z() & 0xF, layer);
    }

    /**
     * Get the biome of a specific block
     *
     * @param position which should be searched
     * @return biome of the block
     */
    public Biome getBiome(BlockPosition position) {
        int xChunk = CoordinateUtils.fromBlockToChunk(position.x());
        int zChunk = CoordinateUtils.fromBlockToChunk(position.z());

        final ChunkAdapter chunk = this.loadChunk(xChunk, zChunk, true);
        return chunk.biome(position.x() & 0xF, position.z() & 0xF);
    }

    private void initGamerules() {
        this.gamerule(Gamerule.DO_DAYLIGHT_CYCLE, true);
    }

    @Override
    public WorldAdapter gamerule(Gamerule<?> gamerule, Object value) {
        Precondition.safeWorldAccess(this, false);
        this.gamerules.put(gamerule, value);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T gamerule(Gamerule<T> gamerule) {
        Precondition.safeWorldAccess(this, false);
        return (T) this.gamerules.get(gamerule);
    }

    // ==================================== UPDATING ==================================== //

    public void startThread() {
        this.server.worldManager().executorService().execute(this::tickLoop);
    }

    private String getUpdatedThreadName(String name, String newSuffix) {
        int i = name.indexOf(':');
        if (i == -1) {
            name = name + ": " + newSuffix;
        } else {
            name = name.substring(0, i) + ": " + newSuffix;
        }
        return name;
    }

    private void setThreadInfo() {
        Thread thread = Thread.currentThread();
        this.threadId = thread.getId();
        thread.setName(getUpdatedThreadName(thread.getName(), this.worldDir.getName()));
    }

    private void setThreadInfoEnd() {
        this.threadId = -1;
        Thread thread = Thread.currentThread();
        thread.setName(getUpdatedThreadName(thread.getName(), "**prev** " + this.worldDir.getName()));
    }

    private void tickLoop() {
        setThreadInfo();
        this.logger.debug("Thread id is #{}", this.threadId);

        this.server.pluginManager().callEvent(new WorldLoadEvent(this));

        // Calculate the nanoseconds we need for the tick loop
        int targetTPS = targetTPS();
        if (targetTPS > 1000) {
            this.logger.warn("Setting target TPS above 1k is not supported, target TPS has been set to 1k");
            targetTPS = 1000;
        }

        long skipNanos = TimeUnit.SECONDS.toNanos(1) / targetTPS;
        this.logger.info("Setting skipNanos to: {}", skipNanos);

        // Tick loop
        float lastTickTime = Float.MIN_NORMAL;

        while (this.running.get()) {
            try {
                // Tick all major subsystems:
                this.currentTickTime = System.currentTimeMillis();
                long internalDiffTime = System.nanoTime();
                this.server.watchdog().add(this.currentTickTime, 30, TimeUnit.SECONDS);

                // Tick networking at every tick
                PlayerConnection[] connections = this.newConnections.toArray(new PlayerConnection[0]);
                for (int i = 0, arrayLength = connections.length; i < arrayLength; i++) {
                    connections[i].update(this.currentTickTime, lastTickTime);
                }
                for (io.gomint.server.entity.EntityPlayer player : this.players.keySet()) {
                    player.connection().update(this.currentTickTime, lastTickTime);
                }

                this.syncTaskManager.update(this.currentTickTime);
                this.update(this.currentTickTime, lastTickTime);

                this.server.watchdog().done();

                // Check if we got shutdown
                if (!this.running.get()) {
                    break;
                }

                long startSleep = System.nanoTime();
                long diff = startSleep - internalDiffTime;
                boolean warn = false;
                if (diff <= skipNanos) {
                    long sleepNeeded = (skipNanos - diff) - this.sleepBalance;
                    this.sleepBalance = 0;

                    LockSupport.parkNanos(sleepNeeded);

                    long endSleep = System.nanoTime();
                    long sleptFor = endSleep - startSleep;
                    diff = skipNanos;

                    if (sleptFor > sleepNeeded) {
                        this.sleepBalance = sleptFor - sleepNeeded;
                    }
                } else {
                    warn = true;
                }

                lastTickTime = (float) diff / TimeUnit.SECONDS.toNanos(1);
                this.tps = (1 / (double) lastTickTime);

                // Due to the fact that we
                if (this.tps > this.targetTPS()) {
                    this.tps = this.targetTPS();
                }

                if (warn) {
                    this.logger.warn("Running behind: {} / {} tps", this.tps, (1 / (skipNanos / (float) TimeUnit.SECONDS.toNanos(1))));
                }
            } catch (Throwable t) {
                this.logger.error("Error during world tick, shutting down world", t);
                ReportUploader.create()
                    .exception(ExceptionUtil.wrap(t))
                    .tag("world-tick")
                    .property("world-name", folder())
                    .includeWorlds()
                    .upload();
                this.running.set(false);
            }
        }
        this.logger.info("Stopping world");

        // Clear scheduler
        this.logger.debug("Clearing sync tasks");
        this.syncTaskManager.clear();

        // Unload all players via API
        final Consumer<EntityPlayer> playerConsumer = this.unloadPlayerConsumer.get();
        if (playerConsumer != null) {
            this.logger.debug("Calling unload players consumer");
            Set<EntityPlayer> playerCopy = new HashSet<>(this.players.keySet());
            playerCopy.forEach(playerConsumer);
        }

        this.logger.debug("Calling WorldUnloadEvent");
        this.server.pluginManager().callEvent(new WorldUnloadEvent(this));
        
        // Kick all remaining players from the server
        // Plugins had the chance to use unloadPlayerConsumer and WorldUnloadEvent to teleport players away
        if (!this.players.isEmpty()) {
            this.logger.debug("Kicking remaining players");
            String reason = "World " + folder() + " closed";
            Set<EntityPlayer> playerCopy = new HashSet<>(this.players.keySet());
            for (EntityPlayer plr : playerCopy) {
                plr.disconnect(reason);
            }
        }

        // Stop this world
        this.logger.debug("Closing world's internal async tasks");
        this.close();

        if (this.config.saveOnUnload()) {
            // Save this world
            this.logger.info("Saving world");
            this.chunkCache.saveAll();
            this.logger.info("World saved");
        }

        // Unload all chunks
        this.logger.debug("Clearing chunk cache");
        this.chunkCache.unloadAll();

        // Close the generator
        this.logger.debug("Closing chunk generator");
        this.chunkGenerator.close();

        // Drop all FDs
        this.logger.debug("Closing world files");
        this.closeFDs();

        // Remove world from manager
        this.logger.debug("Remove world from manager");
        this.server.worldManager().unloadWorld(this);

        Runnable unloadListener = this.unloadInternalListener.get();
        if (unloadListener != null) {
            this.logger.debug("Calling internal unload listener");
            unloadListener.run();
        }

        this.logger.debug("Edit thread name to indicate world stop");
        setThreadInfoEnd();
        this.logger.debug("Thread name edited, end reached.");
    }

    @Override
    public void update(long currentTimeMS, float dT) {
        // ---------------------------------------
        // Tick the chunk cache to get rid of Chunks
        if (!this.config.disableChunkGC()) {
            this.chunkCache.tick(currentTimeMS);
        }

        // ---------------------------------------
        // Update all blocks

        // Tick chunks (random blocks and tiles)
        this.tickChunks(currentTimeMS, dT, !this.config.disableRandomTicking());

        // Scheduled blocks
        while (this.tickQueue.getNextTaskTime() < currentTimeMS) {
            BlockPosition blockToUpdate = this.tickQueue.getNextElement();
            if (blockToUpdate == null) {
                break;
            }

            // Get the block
            Block block = getOptionalBlockAt(blockToUpdate);
            if (block != null) {
                // CHECKSTYLE:OFF
                try {
                    io.gomint.server.world.block.Block block1 = (io.gomint.server.world.block.Block) block;
                    long next = block1.update(UpdateReason.SCHEDULED, currentTimeMS, dT);

                    // Reschedule if needed
                    if (next > currentTimeMS) {
                        this.tickQueue.add(next, blockToUpdate);
                    }
                } catch (Exception e) {
                    this.logger.error("Error whilst ticking block @ " + blockToUpdate, e);
                }
                // CHECKSTYLE:ON
            }
        }

        // ---------------------------------------
        // Update all entities
        this.entitySpawner.update(currentTimeMS, dT);
        this.entityManager.update(currentTimeMS, dT);

        // ---------------------------------------
        // Chunk packages are done in main thread in order to be able to
        // cache packets without possibly getting into race conditions:
        while (!this.chunkPackageTasks.isEmpty()) {
            // One chunk per tick at max:
            AsyncChunkPackageTask task = this.chunkPackageTasks.poll();
            ChunkAdapter chunk = this.getChunk(task.x(), task.z());
            if (chunk == null) {
                chunk = this.loadChunk(task.x(), task.z(), false);
            }

            if (chunk != null) {
                packageChunk(chunk, task.callback());
            }
        }

        // ---------------------------------------
        // Perform regular updates:
        super.update(currentTimeMS, dT);
    }

    @Override
    public void updateClientTick() {
        if (this.gamerule(Gamerule.DO_DAYLIGHT_CYCLE)) {
            this.worldTime++;
            this.correctTime();

            if (this.lastWorldTimeUpdate++ > Values.MAX_SYNC_DELAY) {
                this.lastWorldTimeUpdate = 0;
                this.sendTime();
            }
        }
    }

    private void tickChunks(long currentTimeMS, float dT, boolean tickRandomBlocks) {
        long[] tickingHashes = this.chunkCache.getTickingChunks(dT);
        for (long chunkHash : tickingHashes) {
            ChunkAdapter chunkAdapter = this.chunkCache.getChunkInternal(chunkHash);
            if (tickRandomBlocks) {
                chunkAdapter.tickRandomBlocks(currentTimeMS, dT);
            }

            chunkAdapter.tickTiles(currentTimeMS, dT);
        }
    }

    // ==================================== ENTITY MANAGEMENT ==================================== //

    /**
     * Removes a player from this world and cleans up its references
     *
     * @param player The player entity which should be removed from the world
     */
    public void removePlayer(io.gomint.server.entity.EntityPlayer player) {
        ChunkAdapter chunkAdapter = this.players.remove(player);
        if (chunkAdapter != null) {
            chunkAdapter.removePlayer(player);
        }

        this.entityManager.despawnEntity(player);
    }

    /**
     * Gets an entity given its unique ID.
     *
     * @param entityId The entity's unique ID
     * @return The entity if found or null otherwise
     */
    public Entity<?> findEntity(long entityId) {
        return this.entityManager.findEntity(entityId);
    }

    /**
     * Spawns the given entity at the specified position.
     *
     * @param entity The entity to spawn
     * @param vector The vector which contains the position of the spawn
     */
    public void spawnEntityAt(Entity<?> entity, Vector vector) {
        this.spawnEntityAt(entity, vector.x(), vector.y(), vector.z());
    }

    /**
     * Spawns the given entity at the specified position.
     *
     * @param entity    The entity to spawn
     * @param positionX The x coordinate to spawn the entity at
     * @param positionY The y coordinate to spawn the entity at
     * @param positionZ The z coordinate to spawn the entity at
     */
    public void spawnEntityAt(Entity<?> entity, float positionX, float positionY, float positionZ) {
        this.entityManager.spawnEntityAt(entity, positionX, positionY, positionZ);
    }

    /**
     * Spawns the given entity at the specified position with the specified rotation.
     *
     * @param entity    The entity to spawn
     * @param positionX The x coordinate to spawn the entity at
     * @param positionY The y coordinate to spawn the entity at
     * @param positionZ The z coordinate to spawn the entity at
     * @param yaw       The yaw value of the entity ; will be applied to both the entity's body and head
     * @param pitch     The pitch value of the entity
     */
    public void spawnEntityAt(Entity<?> entity, float positionX, float positionY, float positionZ, float yaw, float pitch) {
        this.entityManager.spawnEntityAt(entity, positionX, positionY, positionZ, yaw, pitch);
    }

    // ==================================== CHUNK MANAGEMENT ==================================== //

    @Override
    public ChunkAdapter getChunk(int x, int z) {
        Precondition.safeWorldAccess(this, false);
        return this.chunkCache.getChunk(x, z);
    }

    public ChunkAdapter getChunk(long hash) {
        return this.chunkCache.getChunkInternal(hash);
    }

    @Override
    public Chunk getOrGenerateChunk(int x, int z) {
        Precondition.safeWorldAccess(this, false);
        Chunk chunk = this.getChunk(x, z);
        if (chunk == null) {
            return this.generate(x, z, true);
        }

        return chunk;
    }

    /**
     * Gets a chunk asynchronously. This allows to load or generate the chunk if it is not yet available
     * and then return it once it gets available. The callback is guaranteed to be invoked: if the chunk
     * could not be loaded nor be generated it will be passed null as its argument.
     *
     * @param x        The x-coordinate of the chunk
     * @param z        The z-coordinate of the chunk
     * @param generate Whether or not to generate teh chunk if it does not yet exist
     * @param callback The callback to be invoked once the chunk is available
     */
    public void getOrLoadChunk(int x, int z, boolean generate, Delegate<ChunkAdapter> callback) {
        Precondition.safeWorldAccess(this, false);
        // Early out:
        ChunkAdapter chunk = this.chunkCache.getChunk(x, z);
        if (chunk != null) {
            callback.invoke(chunk);
            return;
        }

        // Check if we already have a task
        AsyncChunkLoadTask oldTask = this.findAsyncChunkLoadTask(x, z);
        if (oldTask != null) {
            this.logger.debug("Found loader for chunk {} {}", x, z);

            // Set generating if needed
            if (!oldTask.allowGenerate() && generate) {
                oldTask.allowGenerate(true);
            }

            // Check for multi callback
            MultiOutputDelegate<ChunkAdapter> multiOutputDelegate;
            if (oldTask.callback() instanceof MultiOutputDelegate) {
                multiOutputDelegate = (MultiOutputDelegate<ChunkAdapter>) oldTask.callback();
                multiOutputDelegate.getOutputs().offer(callback);
            } else {
                Delegate<ChunkAdapter> delegate = oldTask.callback();
                multiOutputDelegate = new MultiOutputDelegate<>();
                multiOutputDelegate.getOutputs().offer(delegate);
                multiOutputDelegate.getOutputs().offer(callback);
                oldTask.callback(multiOutputDelegate);
            }

            return;
        }

        // Schedule this chunk for asynchronous loading:
        AsyncChunkLoadTask task = new AsyncChunkLoadTask(x, z, generate, callback);
        this.asyncChunkTasks.offer(task);
    }

    private AsyncChunkLoadTask findAsyncChunkLoadTask(int x, int z) {
        for (AsyncChunkTask task : this.asyncChunkTasks) {
            if (task instanceof AsyncChunkLoadTask) {
                AsyncChunkLoadTask loadTask = (AsyncChunkLoadTask) task;
                if (loadTask.x() == x && loadTask.z() == z) {
                    return loadTask;
                }
            }
        }

        return null;
    }

    /**
     * Send a chunk of this world to the client
     *
     * @param x            The x-coordinate of the chunk
     * @param z            The z-coordinate of the chunk
     * @param sendDelegate delegate which may add the chunk to the send queue or add all entities on it to the send queue
     */
    public void sendChunk(int x, int z, Delegate2<Long, ChunkAdapter> sendDelegate) {
        this.getOrLoadChunk(x, z, true, chunk -> chunk.packageChunk(sendDelegate));
    }

    /**
     * Move a player to a new chunk. This is done so we know which player is in which chunk so we can unload unneeded
     * Chunks better and faster.
     *
     * @param x      The x-coordinate of the chunk
     * @param z      The z-coordinate of the chunk
     * @param player The player which should be set into the chunk
     */
    public void movePlayerToChunk(int x, int z, io.gomint.server.entity.EntityPlayer player) {
        Precondition.safeWorldAccess(this, false);
        ChunkAdapter oldChunk = this.players.get(player);
        ChunkAdapter newChunk = this.loadChunk(x, z, true);

        if (oldChunk == null) {
            newChunk.addPlayer(player);
            this.newConnections.remove(player.connection());
            this.players.put(player, newChunk);
        }

        if (oldChunk != null && !oldChunk.equals(newChunk)) {
            oldChunk.removePlayer(player);
            newChunk.addPlayer(player);
            this.players.put(player, newChunk);
        }
    }

    /**
     * Prepares the region surrounding the world's spawn point.
     */
    protected void prepareSpawnRegion() {
        long start = System.currentTimeMillis();

        final int spawnRadius = this.config.amountOfChunksForSpawnArea();
        if (spawnRadius == 0) {
            return;
        }

        final int chunkX = CoordinateUtils.fromBlockToChunk((int) this.spawn.x());
        final int chunkZ = CoordinateUtils.fromBlockToChunk((int) this.spawn.z());

        int amountOfChunksLoaded = 0;
        for (int i = chunkX - spawnRadius; i <= chunkX + spawnRadius; i++) {
            for (int j = chunkZ - spawnRadius; j <= chunkZ + spawnRadius; j++) {
                this.loadChunk(i, j, true);
                amountOfChunksLoaded++;
            }
        }

        this.logger.info("Loaded {} chunks in {} ms", amountOfChunksLoaded, (System.currentTimeMillis() - start));
    }

    /**
     * Load a Chunk from the underlying implementation
     *
     * @param x        The x coordinate of the chunk we want to load
     * @param z        The x coordinate of the chunk we want to load
     * @param generate A boolean which decides whether or not the chunk should be generated when not found
     * @return The loaded or generated Chunk
     */
    public abstract ChunkAdapter loadChunk(int x, int z, boolean generate);

    /**
     * Saves the given chunk to its respective region file. The respective region file
     * is created automatically if it does not yet exist.
     *
     * @param chunk The chunk to be saved
     */
    protected abstract void saveChunk(ChunkAdapter chunk);

    /**
     * Saves the given chunk to its region file asynchronously.
     *
     * @param chunk The chunk to save
     */
    void saveChunkAsynchronously(ChunkAdapter chunk) {
        AsyncChunkSaveTask task = new AsyncChunkSaveTask(chunk);
        this.asyncChunkTasks.offer(task);
    }

    /**
     * Notifies the world that the given chunk was told to package itself. This will effectively
     * produce an asynchronous chunk task which will be completed by the asynchronous worker thread.
     *
     * @param x        The x coordinate of the chunk we want to package
     * @param z        The z coordinate of the chunk we want to package
     * @param callback The callback to be invoked once the chunk is packaged
     */
    void notifyPackageChunk(int x, int z, Delegate2<Long, ChunkAdapter> callback) {
        AsyncChunkPackageTask task = new AsyncChunkPackageTask(x, z, callback);
        this.chunkPackageTasks.add(task);
    }

    /**
     * Package a Chunk into a ChunkData Packet for Raknet. This is done to enable caching of those packets.
     *
     * @param chunk    The chunk which should be packed
     * @param callback The callback which should be invoked when the packing has been done
     */
    private void packageChunk(ChunkAdapter chunk, Delegate2<Long, ChunkAdapter> callback) {
        chunk.createPackagedData(null, false); // We generate some garbage to warm caches
        callback.invoke(CoordinateUtils.toLong(chunk.x(), chunk.z()), chunk);
    }

    // ==================================== NETWORKING HELPERS ==================================== //

    /**
     * Send a packet to all players which can see the position
     *
     * @param position  where the packet will have its impact
     * @param packet    which should be sent
     * @param predicate which decides over each entity if they will get the packet sent or not
     */
    public void sendToVisible(BlockPosition position, Packet packet, Predicate<Entity<?>> predicate) {
        int posX = CoordinateUtils.fromBlockToChunk(position.x());
        int posZ = CoordinateUtils.fromBlockToChunk(position.z());
        this.sendToVisible(posX, posZ, packet, predicate);
    }

    public void sendToVisible(int posX, int posZ, Packet packet, Predicate<Entity<?>> predicate) {
        for (EntityPlayer player : this.onlinePlayers()) {
            io.gomint.server.entity.EntityPlayer p = (io.gomint.server.entity.EntityPlayer) player;

            if (p.knowsChunk(posX, posZ) && predicate.test(player)) {
                ((io.gomint.server.entity.EntityPlayer) player).connection().addToSendQueue(packet);
            }
        }
    }

    // ==================================== ASYNCHRONOUS WORKER ==================================== //

    /**
     * Starts the asynchronous worker thread used by the world to perform I/O operations for chunks.
     */
    private void startAsyncWorker(AsyncScheduler scheduler) {
        this.asyncWorkerRunning = new AtomicBoolean(true);

        this.asyncWorkerTask = scheduler.scheduleAsync(WorldAdapter.this::asyncWorkerLoop, 5, 5, TimeUnit.MILLISECONDS);
    }

    /**
     * Main loop of the world's asynchronous worker thread.
     */
    private void asyncWorkerLoop() {
        if (!this.asyncWorkerRunning.get()) {
            return;
        }
        this.asyncAllowedThreadId = Thread.currentThread().getId();

        try {
            // Fast out
            while (!this.asyncChunkTasks.isEmpty()) {
                try {
                    AsyncChunkTask task = this.asyncChunkTasks.poll((long) Values.CLIENT_TICK_MS, TimeUnit.MILLISECONDS);
                    if (task == null) {
                        return;
                    }

                    ChunkAdapter chunk;
                    switch (task.type()) {
                        case LOAD:
                            AsyncChunkLoadTask load = (AsyncChunkLoadTask) task;
                            this.logger().debug("Loading chunk {} / {}", load.x(), load.z());
                            chunk = this.loadChunk(load.x(), load.z(), load.allowGenerate());

                            load.callback().invoke(chunk);
                            break;

                        case SAVE:
                            AsyncChunkSaveTask save = (AsyncChunkSaveTask) task;
                            chunk = save.chunk();

                            this.logger.debug("Async saving of chunk {} / {}", chunk.x(), chunk.z());
                            this.saveChunk(chunk);

                            break;

                        case POPULATE:
                            AsyncChunkPopulateTask populateTask = (AsyncChunkPopulateTask) task;

                            ChunkAdapter chunkToPopulate = populateTask.chunk();
                            chunkToPopulate.populate();

                            break;

                        default:
                            // Log some error when this happens

                            break;
                    }
                } catch (Throwable cause) {
                    // Catching throwable in order to make sure no uncaught exceptions puts
                    // the asynchronous worker into nirvana:
                    this.logger.error("Error whilst doing async work: ", cause);
                }
            }
        } finally {
            this.asyncAllowedThreadId = -1;
        }
    }

    /**
     * Send the block given under the position to all players in the chunk of the block. This method may
     * delay the block position update when called from async. It syncs up with the main ticker first in that case
     *
     * @param pos The position of the block to update
     */
    public void updateBlock(BlockPosition pos) {
        // Players can't see unpopulated chunks
        ChunkAdapter adapter = this.getChunk(pos.x() >> 4, pos.z() >> 4);
        if (!adapter.populated()) {
            return;
        }
        updateBlock0(adapter, pos);
    }

    /**
     * This helper method executes a block update schedule on the main thread
     *
     * @param adapter in which the block update happens
     * @param pos     of the block which changes
     */
    public void updateBlock0(ChunkAdapter adapter, BlockPosition pos) {
        if (!mainThread(true)) {
            syncScheduler().execute(() -> {
                updateBlock1(adapter, pos);
            });
        } else {
            updateBlock1(adapter, pos);
        }
    }

    /**
     * This helper method executes a block update schedule on the main thread
     *
     * @param adapter in which the block update happens
     * @param pos     of the block which changes
     */
    private void updateBlock1(ChunkAdapter adapter, BlockPosition pos) {
        Precondition.safeWorldAccess(this, false);
        if (!this.players.isEmpty()) {
            var iterator = Object2ObjectMaps.fastIterator(this.players);
            while (iterator.hasNext()) {
                Object2ObjectMap.Entry<io.gomint.server.entity.EntityPlayer, ChunkAdapter> entry = iterator.next();
                io.gomint.server.entity.EntityPlayer player = entry.getKey();
                if (player.knowsChunk(adapter)) {
                    player.blockUpdates().add(pos);
                }
            }
        }
    }

    /**
     * Append all packages needed to update a specific block
     *
     * @param connection which should get the packets
     * @param pos        of the block which should be updated
     */
    public void appendUpdatePackets(PlayerConnection connection, BlockPosition pos) {
        io.gomint.server.world.block.Block block = blockAt(pos);

        // Update the block
        PacketUpdateBlock updateBlock = new PacketUpdateBlock();
        updateBlock.setPosition(pos);

        updateBlock.setBlockId(block.runtimeId());
        updateBlock.setFlags(PacketUpdateBlock.FLAG_ALL);

        connection.addToSendQueue(updateBlock);

        // Check for tile entity
        if (block.tileEntity() != null) {
            PacketTileEntityData tileEntityData = new PacketTileEntityData();
            tileEntityData.setPosition(pos);

            NBTTagCompound compound = new NBTTagCompound("");
            block.tileEntity().toCompound(compound, SerializationReason.NETWORK);

            tileEntityData.setCompound(compound);
            connection.addToSendQueue(tileEntityData);
        }
    }

    /**
     * Get the amount of players online on this world
     *
     * @return amount of players online on this world
     */
    public int amountOfPlayers() {
        return this.players.size();
    }

    /**
     * Get all entities which touch or are inside this bounding box
     *
     * @param bb        the bounding box which should be used to collect entities in
     * @param exception a entity which should not be included in the list
     * @return either null if there are no entities or a collection of entities
     */
    public Collection<Entity<?>> getNearbyEntities(AxisAlignedBB bb, Entity<?> exception) {
        Set<Entity<?>> nearby = null;

        int minX = MathUtils.fastFloor((bb.minX() - 2) / 16);
        int maxX = MathUtils.fastCeil((bb.maxX() + 2) / 16);
        int minZ = MathUtils.fastFloor((bb.minZ() - 2) / 16);
        int maxZ = MathUtils.fastCeil((bb.maxZ() + 2) / 16);

        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                ChunkAdapter chunk = this.getChunk(x, z);
                if (chunk != null && chunk.entities() != null) {
                    for (Long2ObjectMap.Entry<io.gomint.entity.Entity<?>> entry : chunk.entities().long2ObjectEntrySet()) {
                        Entity<?> entity = entry.getValue();
                        if (!entity.equals(exception)) {
                            AxisAlignedBB entityBB = entity.boundingBox();
                            if (entityBB.intersectsWith(bb)) {
                                if (nearby == null) {
                                    nearby = new ObjectOpenHashSet<>();
                                }

                                nearby.add(entity);
                            }
                        }
                    }
                }
            }
        }

        return nearby;
    }

    private <T> List<T> iterateBlocks(int minX, int maxX, int minY, int maxY, int minZ, int maxZ, AxisAlignedBB
        bb, boolean returnBoundingBoxes, boolean includePassThrough) {
        List<T> values = null;

        for (int z = minZ; z < maxZ; ++z) {
            for (int x = minX; x < maxX; ++x) {
                for (int y = minY; y < maxY; ++y) {
                    Block block = this.blockAt(x, y, z);

                    if ((!block.canPassThrough() || includePassThrough) && block.intersectsWith(bb)) {
                        if (values == null) {
                            values = new ArrayList<>();
                        }

                        if (returnBoundingBoxes) {
                            List<AxisAlignedBB> bbs = block.boundingBoxes();
                            if (bbs != null) {
                                values.addAll((Collection<? extends T>) bbs);
                            }
                        } else {
                            values.add((T) block);
                        }
                    }
                }
            }
        }

        return values;
    }

    /**
     * Get blocks which collide with the given entity.
     *
     * @param entity             which is used to check for block collisions
     * @param includePassThrough if the result should also include blocks which can normally be passed through
     * @return list of blocks with which the entity collides, or null when no block has been found
     */
    public List<Block> getCollisionBlocks(io.gomint.entity.Entity<?> entity, boolean includePassThrough) {
        AxisAlignedBB bb = entity.boundingBox().grow(0.1f, 0.01f, 0.1f);

        int minX = MathUtils.fastFloor(bb.minX());
        int minY = MathUtils.fastFloor(bb.minY());
        int minZ = MathUtils.fastFloor(bb.minZ());
        int maxX = MathUtils.fastCeil(bb.maxX());
        int maxY = MathUtils.fastCeil(bb.maxY());
        int maxZ = MathUtils.fastCeil(bb.maxZ());

        return iterateBlocks(minX, maxX, minY, maxY, minZ, maxZ, bb, false, includePassThrough);
    }

    @Override
    public List<AxisAlignedBB> collisionCubes(io.gomint.entity.Entity<?> entity, AxisAlignedBB bb,
                                              boolean includeEntities) {
        Precondition.safeWorldAccess(this, false);
        int minX = MathUtils.fastFloor(bb.minX());
        int minY = MathUtils.fastFloor(bb.minY());
        int minZ = MathUtils.fastFloor(bb.minZ());
        int maxX = MathUtils.fastCeil(bb.maxX());
        int maxY = MathUtils.fastCeil(bb.maxY());
        int maxZ = MathUtils.fastCeil(bb.maxZ());

        List<AxisAlignedBB> collisions = iterateBlocks(minX, maxX, minY, maxY, minZ, maxZ, bb, true, false);

        if (includeEntities) {
            Collection<io.gomint.entity.Entity<?>> entities = getNearbyEntities(bb.grow(0.25f, 0.25f, 0.25f), entity);
            if (entities != null) {
                for (io.gomint.entity.Entity<?> entity1 : entities) {
                    if (collisions == null) {
                        collisions = new ArrayList<>();
                    }

                    collisions.add(entity1.boundingBox());
                }
            }
        }

        return collisions;
    }

    /**
     * Use a item on a block to interact / place it down
     *
     * @param itemInHand    of the player which wants to interact
     * @param blockPosition on which we want to use the item
     * @param face          on which we interact
     * @param clickPosition the exact position on the block we interact with
     * @param entity        which interacts with the block
     * @return true when interaction was successful, false when not
     */
    public boolean useItemOn(ItemStack<?> itemInHand, BlockPosition blockPosition, Facing face, Vector
        clickPosition, io.gomint.server.entity.EntityPlayer entity) {
        Block blockClicked = this.blockAt(blockPosition);
        if (blockClicked instanceof Air) {
            return false;
        }

        PlayerInteractEvent interactEvent = new PlayerInteractEvent(entity, PlayerInteractEvent.ClickType.RIGHT, blockClicked);
        this.server.pluginManager().callEvent(interactEvent);

        if (interactEvent.cancelled()) {
            return false;
        }

        // TODO: Event stuff and spawn protection / Adventure gamemode

        io.gomint.server.world.block.Block clickedBlock = (io.gomint.server.world.block.Block) blockClicked;
        boolean interacted = false;
        if (!entity.sneaking()) {
            interacted = clickedBlock.interact(entity, face, clickPosition, itemInHand);
        }

        // Let the item interact
        boolean itemInteracted = ((io.gomint.server.inventory.item.ItemStack<?>) itemInHand)
            .interact(entity, face, clickPosition, clickedBlock);

        if ((!interacted && !itemInteracted) || entity.sneaking()) {
            Block block = ((io.gomint.server.inventory.item.ItemStack<?>) itemInHand).block();
            boolean canBePlaced = block != null && !(itemInHand instanceof ItemAir);
            if (canBePlaced) {
                Block blockReplace = blockClicked.side(face);
                io.gomint.server.world.block.Block replaceBlock = (io.gomint.server.world.block.Block) blockReplace;

                if (clickedBlock.canBeReplaced(itemInHand)) {
                    replaceBlock = clickedBlock;
                } else if (!replaceBlock.canBeReplaced(itemInHand)) {
                    return false;
                }

                // We got the block we want to replace
                // Let the item build up the block
                boolean success = this.server.blocks().replaceWithItem((io.gomint.server.world.block.Block) block,
                    entity, clickedBlock, replaceBlock, face, itemInHand, clickPosition);
                if (success) {
                    // Play sound
                    io.gomint.server.world.block.Block newBlock = replaceBlock.world().blockAt(replaceBlock.position());
                    playSound(null, new Vector(newBlock.position()), Sound.PLACE, (byte) 1, BlockRuntimeIDs.toBlockIdentifier(newBlock.blockId(), null).runtimeId());

                    if (entity.gamemode() != Gamemode.CREATIVE) {
                        ((io.gomint.server.inventory.item.ItemStack<?>) itemInHand).afterPlacement();
                    }
                }

                return success;
            }
        }

        return interacted || itemInteracted;
    }

    public <T extends Block> T scheduleNeighbourUpdates(T block) {
        if (!mainThread(true)) {
            // We don't update from async
            return block;
        }

        io.gomint.server.world.block.Block implBlock = (io.gomint.server.world.block.Block) block;
        for (Facing face : Facing.values()) {
            io.gomint.server.world.block.Block neighbourBlock = implBlock.side(face);

            // CHECKSTYLE:OFF
            try {
                long next = neighbourBlock.update(UpdateReason.NEIGHBOUR_UPDATE, this.currentTickTime, 0f);
                if (next > this.currentTickTime) {
                    BlockPosition position = neighbourBlock.position();
                    this.tickQueue.add(next, position);
                }
            } catch (Exception e) {
                this.logger.error("Exception while updating block @ {}", neighbourBlock.position(), e);
            }
            // CHECKSTYLE:ON
        }

        return block;
    }

    @Override
    public EntityItem createItemDrop(Vector vector, ItemStack<?> item) {
        Precondition.safeWorldAccess(this, false);
        EntityItem entityItem = new EntityItem(item, this);
        spawnEntityAt(entityItem, vector);
        return entityItem;
    }

    private void close() {
        // Stop async worker
        this.asyncWorkerRunning.set(false);
        this.asyncWorkerTask.cancel();
    }

    public TemporaryStorage getTemporaryBlockStorage(BlockPosition position, int layer) {
        // Get chunk
        ChunkAdapter chunk = this.loadChunk(position.x() >> 4, position.z() >> 4, true);
        return chunk.getTemporaryStorage(position.x() & 0xF, position.y(), position.z() & 0xF, layer);
    }

    /**
     * Generate a new chunk, either with async or synced up populations
     *
     * @param x              coord of the chunk
     * @param z              coord of the chunk
     * @param syncPopulation true when the populators should be run sync
     * @return freshly generated chunk
     */
    public ChunkAdapter generate(int x, int z, boolean syncPopulation) {
        if (this.chunkGenerator != null) {
            this.logger.debug("Generating chunk {} / {}", x, z);

            ChunkAdapter chunk = (ChunkAdapter) this.chunkGenerator.generate(x, z);
            if (chunk != null) {
                chunk.calculateHeightmap(240);

                if (!this.chunkCache.putChunk(chunk)) {
                    chunk.release();
                    return this.chunkCache.getChunk(x, z);
                } else {
                    if (syncPopulation) {
                        chunk.populate();
                    } else {
                        this.addPopulateTask(chunk);
                    }
                }

                return chunk;
            }
        }

        return null;
    }

    public void addPopulateTask(ChunkAdapter chunk) {
        this.asyncChunkTasks.offer(new AsyncChunkPopulateTask(chunk));
    }

    public void sendLevelEvent(Vector position, int levelEvent, int data) {
        this.sendLevelEvent(null, position, levelEvent, data);
    }

    public WorldAdapter sendLevelEvent(EntityPlayer player, Vector position, int levelEvent, int data) {
        Precondition.safeWorldAccess(this, false);
        PacketWorldEvent worldEvent = new PacketWorldEvent();
        worldEvent.setData(data);
        worldEvent.setEventId(levelEvent);
        worldEvent.setPosition(position);

        if (player != null) {
            ((io.gomint.server.entity.EntityPlayer) player).connection().addToSendQueue(worldEvent);
        } else {
            sendToVisible(position.toBlockPosition(), worldEvent, entity -> true);
        }

        return this;
    }

    public void storeTileEntity(BlockPosition position, TileEntity tileEntity) {
        // Get chunk
        ChunkAdapter chunk = this.loadChunk(position.x() >> 4, position.z() >> 4, true);
        chunk.setTileEntity(position.x() & 0xF, position.y(), position.z() & 0xF, tileEntity);
    }

    public void removeTileEntity(BlockPosition position) {
        // Get chunk
        ChunkAdapter chunk = this.loadChunk(position.x() >> 4, position.z() >> 4, true);
        chunk.removeTileEntity(position.x() & 0xF, position.y(), position.z() & 0xF);
    }

    public boolean breakBlock(BlockPosition position, List<ItemStack<?>> drops, boolean creative) {
        io.gomint.server.world.block.Block block = blockAt(position);
        if (block.onBreak(creative)) {
            if (!drops.isEmpty()) {
                for (ItemStack<?> itemStack : drops) {
                    EntityItem item = this.createItemDrop(new Vector(block.position()).add(0.5f, 0.5f, 0.5f), itemStack);
                    item.velocity(new Vector(ThreadLocalRandom.current().nextFloat() * 0.2f - 0.1f, 0.2f, ThreadLocalRandom.current().nextFloat() * 0.2f - 0.1f));
                }
            }

            // Break animation (this also plays the break sound in the client)
            sendLevelEvent(position.toVector().add(.5f, .5f, .5f), LevelEvent.PARTICLE_DESTROY, block.runtimeId());

            // Schedule neighbour updates
            scheduleNeighbourUpdates(block.performBreak(creative));
            return true;
        } else {
            return false;
        }
    }

    public void resetTemporaryStorage(BlockPosition position, int layer) {
        // Get chunk
        int x = position.x(), y = position.y(), z = position.z();
        int xChunk = CoordinateUtils.fromBlockToChunk(x);
        int zChunk = CoordinateUtils.fromBlockToChunk(z);

        ChunkAdapter chunk = this.loadChunk(xChunk, zChunk, true);
        chunk.resetTemporaryStorage(x & 0xF, y, z & 0xF, layer);
    }

    public void scheduleBlockUpdate(Location location, long delay, TimeUnit unit) {
        BlockPosition position = location.toBlockPosition();
        long key = this.currentTickTime + unit.toMillis(delay);
        this.tickQueue.add(key, position);
    }

    public void dropItem(Vector vector, ItemStack<?> drop) {
        if (drop.itemType() == ItemType.AIR) {
            return;
        }

        Vector motion = new Vector(ThreadLocalRandom.current().nextFloat() * 0.2f - 0.1f,
            0.2f, ThreadLocalRandom.current().nextFloat() * 0.2f - 0.1f);

        EntityItem item = this.createItemDrop(vector, drop);
        item.velocity(motion);
    }

    @Override
    public WorldAdapter sendParticle(Vector location, Particle particle) {
        return this.sendParticle(null, location, particle, 0);
    }

    public WorldAdapter sendParticle(EntityPlayer player, Vector location, Particle particle, int data) {
        int eventId;
        switch (particle) {
            case PUNCH_BLOCK:
                eventId = LevelEvent.PARTICLE_PUNCH_BLOCK;
                break;
            case BREAK_BLOCK:
                eventId = LevelEvent.PARTICLE_DESTROY;
                break;
            default:
                eventId = LevelEvent.ADD_PARTICLE_MASK | EnumConnectors.PARTICLE_CONNECTOR.convert(particle).id();
        }

        return sendLevelEvent(player, location, eventId, data);
    }

    @Override
    public WorldAdapter sendParticle(Vector location, Particle particle, ParticleData data) {
        return this.sendParticle(null, location, particle, data);
    }

    public WorldAdapter sendParticle(EntityPlayer player, Vector location, Particle particle, ParticleData data) {
        int dataNumber = 0;

        switch (particle) {
            case FALLING_DUST:
                if (data.red() == -1 || data.blue() == -1 || data.green() == -1 || data.alpha() == -1) {
                    throw new IllegalArgumentException("Particle data does not reflect color for particle " + particle);
                }

                dataNumber = ((data.alpha() & 0xff) << 24) |
                    ((data.red() & 0xff) << 16) |
                    ((data.green() & 0xff) << 8) |
                    (data.blue() & 0xff);

                break;

            case PUNCH_BLOCK:
                if (data.block() == null || data.face() == -1) {
                    throw new IllegalArgumentException("Particle data does not reflect block and face data for particle " + particle);
                }

                io.gomint.server.world.block.Block block = (io.gomint.server.world.block.Block) data.block();
                dataNumber = block.runtimeId() | (data.face() << 24);

                break;

            case BREAK_BLOCK:
                if (data.block() == null) {
                    throw new IllegalArgumentException("Particle data does not reflect block data for particle " + particle);
                }

                block = (io.gomint.server.world.block.Block) data.block();
                dataNumber = block.runtimeId();

                break;
        }

        return this.sendParticle(player, location, particle, dataNumber);
    }

    public void createExpOrb(Location location, int amount) {
        EntityXPOrb xpOrb = new EntityXPOrb((WorldAdapter) location.world(), amount);
        spawnEntityAt(xpOrb, location.x(), location.y(), location.z(), location.yaw(), location.pitch());
    }

    public void removeEntity(io.gomint.server.entity.Entity<?> entity) {
        // Just tell the entity manager, it will handle the rest
        this.entityManager.despawnEntity(entity);
    }

    @Override
    public void unload(Consumer<EntityPlayer> playerConsumer) {
        this.unloadPlayerConsumer.set(playerConsumer);
        this.running.set(false);
    }

    public void unloadInternal(Consumer<EntityPlayer> playerConsumer, Runnable unloadedListener) {
        this.unloadPlayerConsumer.set(playerConsumer);
        this.unloadInternalListener.set(unloadedListener);
        this.running.set(false);
    }

    protected abstract void closeFDs();

    @Override
    public <T extends Block> WorldAdapter iterateBlocks(Class<T> blockClass, Consumer<T> blockConsumer) {
        Precondition.safeWorldAccess(this, false);
        // Get the id of the block which we search
        String blockId = this.server.blocks().getID(blockClass);

        // Iterate over all chunks
        this.chunkCache.iterateAll(chunkAdapter -> {
            int chunkZ = chunkAdapter.z();
            int chunkX = chunkAdapter.x();

            for (int i = 0; i < 2; i++) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 256; y++) {
                        for (int z = 0; z < 16; z++) {
                            String currentBlockId = chunkAdapter.getBlock(x, y, z, i);
                            if (currentBlockId.equals(blockId)) {
                                T block = blockAt((chunkX << 4) + x, y, (chunkZ << 4) + z);
                                blockConsumer.accept(block);
                            }
                        }
                    }
                }
            }
        });

        return this;
    }

    @Override
    public WorldAdapter iterateChunks(Consumer<Chunk> chunkConsumer) {
        Precondition.safeWorldAccess(this, false);
        // Iterate over all chunks
        this.chunkCache.iterateAll(chunkConsumer::accept);
        return this;
    }

    @Override
    public WorldAdapter iterateAllEntities(Consumer<Entity<?>> entityConsumer) {
        Precondition.safeWorldAccess(this, false);
        // Iterate over all chunks
        this.chunkCache.iterateAll(chunkAdapter -> chunkAdapter.iterateAllEntities(entityConsumer));
        return this;
    }

    @Override
    public <T extends Entity<T>> WorldAdapter iterateEntities(Class<T> entityClass, Consumer<T> entityConsumer) {
        Precondition.safeWorldAccess(this, false);
        // Iterate over all chunks
        this.chunkCache.iterateAll(chunkAdapter -> chunkAdapter.iterateEntities(entityClass, entityConsumer));
        return this;
    }

    /**
     * Adjust the spawn level to the first in air block
     */
    protected void adjustSpawn() {
        int airRuntime = BlockRuntimeIDs.toBlockIdentifier("minecraft:air", null).runtimeId();

        BlockPosition check = new BlockPosition((int) this.spawn.x(), 0, (int) this.spawn.z());
        for (int i = 255; i > 0; i--) {
            check.y(i);
            if (this.getRuntimeID(check, 0) != airRuntime) {
                this.spawn.y(1 + i);
                break;
            }
        }
    }

    /**
     * Tell the world implementation it should build up its chunk generator
     */
    protected abstract void prepareGenerator();

    public void constructGenerator(Class<? extends ChunkGenerator> generator, GeneratorContext context) throws
        WorldCreateException {
        try {
            this.chunkGenerator = generator.getConstructor(World.class, GeneratorContext.class).newInstance(this, context);
        } catch (NoSuchMethodException e) {
            throw new WorldCreateException("The given generator does not provide a (World, GeneratorContext) constructor");
        } catch (IllegalAccessException e) {
            throw new WorldCreateException("The given generator can't be constructed. Be sure the (World, GeneratorContext) constructor is public");
        } catch (InstantiationException e) {
            throw new WorldCreateException("The generator given is either an abstracted class or some kind of interface");
        } catch (InvocationTargetException e) {
            throw new WorldCreateException("The constructor of the generator has thrown this exception", e);
        }
    }

    public void addTickingBlock(long time, BlockPosition position) {
        this.tickQueue.add(time, position);
    }

    public boolean isUpdateScheduled(BlockPosition position) {
        return this.tickQueue.contains(position);
    }

    @Override
    public WorldAdapter save() {
        Precondition.safeWorldAccess(this, false);
        this.chunkCache.saveAll();
        return this;
    }

    public ChunkCache chunkCache() {
        return this.chunkCache;
    }

    /**
     * Gets called when a player gets switched to this world
     *
     * @param player which has been switched to this world
     */
    public void playerSwitched(io.gomint.server.entity.EntityPlayer player) {
        // Set difficulty
        PacketSetDifficulty packetSetDifficulty = new PacketSetDifficulty();
        packetSetDifficulty.setDifficulty(this.difficulty.getDifficultyDegree());
        player.connection().addToSendQueue(packetSetDifficulty);
    }

    /**
     * Persists a player to the underlying storage
     *
     * @param player which should be persisted
     */
    public abstract void persistPlayer(io.gomint.server.entity.EntityPlayer player);

    /**
     * Load a player state from the underlying storage
     *
     * @param player which should be loaded
     */
    public abstract boolean loadPlayer(io.gomint.server.entity.EntityPlayer player);

    @Override
    public Block highestBlockAt(int x, int z) {
        Precondition.safeWorldAccess(this, false);
        ChunkAdapter chunk = this.loadChunk(x >> 4, z >> 4, true);
        int y = chunk.getHeight(x & 0xF, z & 0xF);
        return chunk.blockAt(x & 0xF, y - 1, z & 0xF);
    }

    @Override
    public Block highestBlockAt(int x, int z, WorldLayer layer) {
        Precondition.safeWorldAccess(this, false);
        ChunkAdapter chunk = this.loadChunk(x >> 4, z >> 4, true);
        int y = chunk.getHeight(x & 0xF, z & 0xF);
        return chunk.blockAt(x & 0xF, y - 1, z & 0xF, layer);
    }

    protected final void broadcastPacket(Packet packet) {
        for (EntityPlayer player : this.onlinePlayers()) {
            io.gomint.server.entity.EntityPlayer handle = (io.gomint.server.entity.EntityPlayer) player;
            handle.connection().send(packet);
        }
    }

    public int dimension() {
        return 0; // TODO: Implement proper dimensions
    }

    @Override
    public WorldAdapter unloadChunk(int x, int z) {
        Precondition.safeWorldAccess(this, false);
        this.chunkCache.unload(x, z);
        return this;
    }

    @Override
    public WorldAdapter time(Duration time) {
        Precondition.safeWorldAccess(this, false);
        // Since 0 is not 0 ticks in MC we need to shift a little bit
        float tickAt0 = Values.TICKS_ON_ZERO;

        this.worldTime = (int) (tickAt0 + (time.getSeconds() * Values.CYCLE_TICKS_PER_SECOND));
        this.correctTime();
        this.sendTime();

        return this;
    }

    @Override
    public Duration time() {
        long seconds = (long) (this.worldTime / Values.CYCLE_TICKS_PER_SECOND);

        // Since 0 is not at 0 we need to offset 6 hours
        seconds += Values.SECONDS_ON_ZERO;

        if (seconds >= TimeUnit.HOURS.toSeconds(24)) {
            seconds -= TimeUnit.HOURS.toSeconds(24);
        }

        return Duration.ofSeconds(seconds);
    }

    private void correctTime() {
        while (this.worldTime >= Values.FULL_DAY_CYCLE) {
            this.worldTime -= Values.FULL_DAY_CYCLE;
        }
    }

    private void sendTime() {
        int ticks = this.timeAsTicks();

        for (io.gomint.server.entity.EntityPlayer player : this.players.keySet()) {
            player.connection().sendWorldTime(ticks);
        }
    }

    public int timeAsTicks() {
        return this.worldTime;
    }

    @Override
    public Set<Entity<?>> entitiesByTag(String tag) {
        Precondition.safeWorldAccess(this, false);
        return this.entityManager.findEntities(tag);
    }

    public SyncSchedulerAdapter syncScheduler() {
        return this.syncSchedulerAdapter;
    }

    @Override
    public WorldScheduler scheduler() {
        final PluginClassloader classLoader = CallerDetectorUtil.getCallerPluginLoader();
        return classLoader.instance.scheduler().withWorld(this);
    }

    @Override
    public boolean mainThread() {
        return mainThread(false);
    }

    /**
     * Check if current thread is this world's main thread
     *
     * @param allowForIOThread whether to allow async world io thread
     * @return true if main thread, false if not
     */
    public boolean mainThread(boolean allowForIOThread) {
        if (this.threadId == -1) {
            return true;
        } else {
            long id = Thread.currentThread().getId();
            return this.threadId == id || allowForIOThread && this.asyncAllowedThreadId == id;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    @Override
    public double tps() {
        return this.tps;
    }

    @Override
    public int targetTPS() {
        return this.server.serverConfig().targetTPS();
    }

    public long currentTickTime() {
        return this.currentTickTime;
    }

}
