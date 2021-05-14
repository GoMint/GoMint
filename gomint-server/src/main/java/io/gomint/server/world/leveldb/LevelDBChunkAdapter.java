/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.leveldb;

import io.gomint.leveldb.DB;
import io.gomint.leveldb.WriteBatch;
import io.gomint.math.Location;
import io.gomint.math.MathUtils;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.tileentity.SerializationReason;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.maintenance.ReportUploader;
import io.gomint.server.util.Allocator;
import io.gomint.server.util.BlockIdentifier;
import io.gomint.server.util.Palette;
import io.gomint.server.util.Values;
import io.gomint.server.util.collection.FixedReadOnlyMap;
import io.gomint.server.world.BlockRuntimeIDs;
import io.gomint.server.world.ChunkAdapter;
import io.gomint.server.world.ChunkSlice;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.Block;
import io.gomint.taglib.AllocationLimitReachedException;
import io.gomint.taglib.NBTReader;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.taglib.NBTWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class LevelDBChunkAdapter extends ChunkAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelDBChunkAdapter.class);
    private static final int BLOCK_VERSION = 17760256;

    /**
     * Create a new level db backed chunk
     *
     * @param worldAdapter which loaded this chunk
     * @param x            position of chunk
     * @param z            position of chunk
     * @param populated    true when chunk is already populated, false when not
     */
    public LevelDBChunkAdapter(WorldAdapter worldAdapter, int x, int z, boolean populated) {
        super(worldAdapter, x, z);
        this.populated(populated);
        this.loadedTime = this.lastSavedTimestamp = worldAdapter.currentTickTime();
    }

    public LevelDBChunkAdapter(WorldAdapter worldAdapter, int x, int z) {
        super(worldAdapter, x, z);
        this.loadedTime = worldAdapter.currentTickTime();
    }

    void save(DB db) {
        WriteBatch writeBatch = new WriteBatch();

        // We do blocks first
        for (int i = 0; i < this.chunkSlices.length; i++) {
            if (this.chunkSlices[i] == null) {
                continue;
            }

            saveChunkSlice(i, writeBatch);
        }

        // Write version
        ByteBuf key = ((LevelDBWorldAdapter) this.world).getKey(this.x, this.z, (byte) 0x2c);
        ByteBuf val = PooledByteBufAllocator.DEFAULT.directBuffer(1).writeByte(Values.LATEST_CHUNK_VERSION);
        writeBatch.put(key, val);

        // Save metadata
        key = ((LevelDBWorldAdapter) this.world).getKey(this.x, this.z, (byte) 0x36);
        val = PooledByteBufAllocator.DEFAULT.directBuffer(4).writeByte(populated() ? 2 : 0)
            .writeByte(0).writeByte(0).writeByte(0);
        writeBatch.put(key, val);

        // Save tiles
        ByteBuf out = PooledByteBufAllocator.DEFAULT.directBuffer();
        NBTWriter nbtWriter = new NBTWriter(out, ByteOrder.LITTLE_ENDIAN);
        for (TileEntity tileEntity : this.getTileEntities()) {
            NBTTagCompound compound = new NBTTagCompound("");
            tileEntity.toCompound(compound, SerializationReason.PERSIST);

            try {
                nbtWriter.write(compound);
            } catch (IOException e) {
                LOGGER.warn("Could not write tile to leveldb", e);
            }
        }

        if (out.readableBytes() > 0) {
            key = ((LevelDBWorldAdapter) this.world).getKey(this.x, this.z, (byte) 0x31);
            writeBatch.put(key, out);
        }

        // TODO: Store entities

        // Save biome and height
        ByteBuf outHB = PooledByteBufAllocator.DEFAULT.directBuffer(768);
        key = ((LevelDBWorldAdapter) this.world).getKey(this.x, this.z, (byte) 0x2d);

        for (short height : this.height) {
            outHB.writeShortLE(height);
        }

        outHB.writeBytes(this.biomes.asReadOnly());
        writeBatch.put(key, outHB);

        db.write(writeBatch);

        // Release bound memory
        writeBatch.clear();
        writeBatch.close();
    }

    private void saveChunkSlice(int i, WriteBatch writeBatch) {
        ChunkSlice slice = this.chunkSlices[i];
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer();

        buffer.writeByte((byte) 8);
        buffer.writeByte((byte) slice.amountOfLayers());

        for (int layer = 0; layer < slice.amountOfLayers(); layer++) {
            List<BlockIdentifier> blocks = slice.getBlocks(layer);

            // Count how many unique blocks we have in this chunk
            int[] indexIDs = new int[4096];

            IntList indexList = new IntArrayList();
            IntList runtimeIndex = new IntArrayList();
            Int2ObjectMap<BlockIdentifier> block = new Int2ObjectOpenHashMap<>();

            int foundIndex = 0;

            int lastBlockId = -1;
            int runtimeIdCounter = 0;

            for (short blockIndex = 0; blockIndex < indexIDs.length; blockIndex++) {
                int blockId = blocks.get(blockIndex).runtimeId();

                if (lastBlockId != blockId) {
                    foundIndex = indexList.indexOf(blockId);
                    if (foundIndex == -1) {
                        int runtimeId = runtimeIdCounter++;
                        block.put(runtimeId, blocks.get(blockIndex));
                        runtimeIndex.add(runtimeId);
                        indexList.add(blockId);
                        foundIndex = indexList.size() - 1;
                    }

                    lastBlockId = blockId;
                }

                indexIDs[blockIndex] = foundIndex;
            }

            // Get correct wordsize
            int value = indexList.size();
            if (value == 0) {
                LOGGER.error("Trying to persist without any blocks");
                ReportUploader.create().includeWorlds()
                    .tag("invalid.leveldb.palette.store")
                    .property("block.count", String.valueOf(indexIDs.length))
                    .upload("Invalid amount of runtime index entries");
            }

            int numberOfBits = MathUtils.fastFloor(MathUtils.log2(value)) + 1;

            // Prepare palette
            int amountOfBlocks = MathUtils.fastFloor(32f / (float) numberOfBits);

            Palette palette = new Palette(buffer, amountOfBlocks, false);

            byte paletteWord = (byte) ((byte) (palette.getPaletteVersion().getVersionId() << 1) | 1);
            buffer.writeByte(paletteWord);
            palette.addIndexIDs(indexIDs);
            palette.finish();

            // Write persistent ids
            buffer.writeIntLE(indexList.size());
            for (int value1 : runtimeIndex.toArray(new int[0])) {
                BlockIdentifier blockIdentifier = block.get(value1);

                NBTTagCompound compound = new NBTTagCompound("");
                compound.addValue("name", blockIdentifier.blockId());
                compound.addValue("states", blockIdentifier.nbt());
                compound.addValue("version", BLOCK_VERSION);

                try {
                    compound.writeTo(buffer, ByteOrder.LITTLE_ENDIAN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        ByteBuf key = ((LevelDBWorldAdapter) this.world).getKeySubChunk(this.x, this.z, (byte) 0x2f, (byte) i);
        buffer.readerIndex(0);
        writeBatch.put(key, buffer);
    }

    void loadSection(int sectionY, byte[] chunkData) {
        ByteBuf buffer = Allocator.allocate(chunkData);

        // First byte is chunk section version
        byte subchunkVersion = buffer.readByte();
        int storages = 1;
        switch (subchunkVersion) {
            case 8:
                storages = buffer.readByte();
            case 1:
                for (int sI = 0; sI < storages; sI++) {
                    byte data = buffer.readByte();
                    boolean isPersistent = ((data >> 8) & 1) != 1; // last bit is the isPresent state (shift and mask it to 1)
                    byte wordTemplate = (byte) (data >>> 1); // Get rid of the last bit (which seems to be the isPresent state)

                    Palette palette = new Palette(buffer, wordTemplate, true);
                    short[] indexes = palette.getIndexes();

                    // Read NBT data
                    int needed = buffer.readIntLE();
                    Int2IntMap chunkPalette = new Int2IntOpenHashMap(needed); // Varint my ass
                    chunkPalette.defaultReturnValue(-1);

                    int index = 0;
                    NBTReader reader = new NBTReader(buffer, ByteOrder.LITTLE_ENDIAN);
                    while (index < needed) {
                        try {
                            NBTTagCompound compound = reader.parse();
                            String blockId = compound.getString("name", "minecraft:air");
                            NBTTagCompound states = compound.getCompound("states", false);
                            if (states != null) {
                                FixedReadOnlyMap mStates = new FixedReadOnlyMap(states.entrySet());
                                BlockIdentifier identifier = BlockRuntimeIDs.toBlockIdentifier(blockId, mStates);
                                if (identifier == null) {
                                    LOGGER.error("Unknown block / state config: {} / {}", blockId, states);
                                }

                                chunkPalette.put(index++, identifier.runtimeId());
                            } else {
                                chunkPalette.put(index++, BlockRuntimeIDs.toBlockIdentifier(blockId, null).runtimeId());
                            }
                        } catch (IOException | AllocationLimitReachedException e) {
                            LOGGER.error("Error in loading tile entities", e);
                            break;
                        }
                    }

                    ChunkSlice slice = this.ensureSlice(sectionY);
                    for (short i = 0; i < indexes.length; i++) {
                        int runtimeID = chunkPalette.get(indexes[i]);
                        if (runtimeID == -1) {
                            LOGGER.error("Invalid runtime in storage");
                            runtimeID = BlockRuntimeIDs.toBlockIdentifier("minecraft:air", null).runtimeId();
                        }

                        slice.setRuntimeIdInternal(i, sI, runtimeID);
                    }

                    slice.resetPersistenceFlag(); // Since we have loaded and nothing changed in storage we reset this
                }

                break;
        }

        buffer.release();
    }

    void loadTileEntities(byte[] tileEntityData) {
        ByteBuf data = Allocator.allocate(tileEntityData);
        NBTReader nbtReader = new NBTReader(data, ByteOrder.LITTLE_ENDIAN);
        while (data.readableBytes() > 0) {
            TileEntity tileEntity = null;

            try {
                NBTTagCompound compound = nbtReader.parse();

                Block block = this.blockAt(compound.getInteger("x", 0) & 0xF, compound.getInteger("y", 0), compound.getInteger("z", 0) & 0xF);

                tileEntity = this.world.server().tileEntities().construct(compound, block);
                if (tileEntity != null) {
                    this.addTileEntity(tileEntity);
                }
            } catch (IOException | AllocationLimitReachedException e) {
                LOGGER.debug("Error in loading tile entities", e);
                break;
            }
        }

        data.release();
    }

    void loadEntities(byte[] entityData) {
        ByteBuf data = Allocator.allocate(entityData);
        NBTReader nbtReader = new NBTReader(data, ByteOrder.LITTLE_ENDIAN);
        while (data.readableBytes() > 0) {
            try {
                NBTTagCompound compound = nbtReader.parse();
                String identifier = compound.getString("identifier", null);
                if (identifier != null) {
                    var entity = this.world.server().entities().create(identifier);
                    if (entity != null) {
                        ((Entity<?>) entity).initFromNBT(compound);
                        Location location = entity.location();
                        location.world(this.world);
                        entity.spawn(location);
                    }
                }
            } catch (IOException | AllocationLimitReachedException e) {
                LOGGER.error("Error in loading entities", e);
                break;
            }
        }

        data.release();
    }

    public void loadHeightAndBiomes(byte[] heightAndBiomes) {
        ByteBuf buf = Unpooled.wrappedBuffer(heightAndBiomes);
        short[] height = new short[16 * 16];
        for (int i = 0; i < height.length; i++) {
            height[i] = buf.readShortLE();
        }

        byte[] biomes = new byte[16 * 16];
        buf.readBytes(biomes);

        this.heightMap(height);
        this.setBiomes(biomes);
    }

    /**
     * Prepare maybe needed converters
     *
     * @param version
     */
    public void prepareVersion(byte version) {

    }

}
