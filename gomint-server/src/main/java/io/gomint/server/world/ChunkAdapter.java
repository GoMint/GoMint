/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world;

import io.gomint.jraknet.PacketBuffer;
import io.gomint.math.BlockPosition;
import io.gomint.server.async.Delegate2;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.tileentity.SerializationReason;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.network.packet.PacketWorldChunk;
import io.gomint.server.util.Cache;
import io.gomint.server.util.Precondition;
import io.gomint.server.world.storage.TemporaryStorage;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.taglib.NBTWriter;
import io.gomint.world.Biome;
import io.gomint.world.Chunk;
import io.gomint.world.WorldLayer;
import io.gomint.world.block.Block;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author BlackyPaw
 * @version 1.0
 */
public class ChunkAdapter implements Chunk {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkAdapter.class);
    private static final byte[] EMPTY_BIOMES = new byte[256];

    // CHECKSTYLE:OFF
    // World
    protected final WorldAdapter world;

    // Chunk
    protected final int x;
    protected final int z;
    protected long inhabitedTime;

    // Biomes
    protected final ByteBuf biomes = PooledByteBufAllocator.DEFAULT.directBuffer(16 * 16);

    // Blocks
    protected ChunkSlice[] chunkSlices = new ChunkSlice[16];
    protected short[] height = new short[16 * 16];

    // Players / Chunk GC
    protected List<EntityPlayer> players = new ArrayList<>();
    private long lastPlayerOnThisChunk;
    protected long loadedTime;
    protected long lastSavedTimestamp;
    private AtomicInteger refCount = new AtomicInteger(0);

    // Entities
    protected Long2ObjectMap<io.gomint.entity.Entity<?>> entities = null;

    // State saving flag
    private boolean populated;

    // CHECKSTYLE:ON

    public ChunkAdapter(WorldAdapter world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;

        // Write zeros
        this.biomes.writeBytes(EMPTY_BIOMES);
    }

    @Override
    public String toString() {
        return "ChunkAdapter{" +
            "world=" + this.world +
            ", x=" + this.x +
            ", z=" + this.z +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkAdapter adapter = (ChunkAdapter) o;
        return this.x == adapter.x &&
                this.z == adapter.z &&
            Objects.equals(this.world, adapter.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.world, this.x, this.z);
    }

    public void populated(boolean populated) {
        this.populated = populated;
    }

    public boolean populated() {
        return this.populated;
    }

    public ChunkSlice[] chunkSlices() {
        return this.chunkSlices;
    }

    @Override
    public WorldAdapter world() {
        return this.world;
    }

    /**
     * Ticks this chunk for random block updates
     *
     * @param currentTimeMS The current time in milliseconds. Used to reduce the number of calls to System#currentTimeMillis()
     * @param dT            The delta from the full second which has been calculated in the last tick
     */
    final void tickRandomBlocks(long currentTimeMS, float dT) {
        for (ChunkSlice chunkSlice : this.chunkSlices) {
            if (chunkSlice != null && !chunkSlice.allAir()) {
                this.tickRandomBlocksForSlice(chunkSlice, currentTimeMS, dT);
            }
        }
    }

    private void tickRandomBlocksForSlice(ChunkSlice chunkSlice, long currentTimeMS, float dT) {
        this.iterateRandomBlocks(chunkSlice, currentTimeMS, dT, this.world.config().randomUpdatesPerTick());
    }

    private void iterateRandomBlocks(ChunkSlice chunkSlice, long currentTimeMS, float dT, int randomUpdatesPerTick) {
        for (int i = 0; i < randomUpdatesPerTick; i++) {
            this.world.lcg = this.world.lcg * 3 + 1013904223;
            short index = (short) ((this.world.lcg >> 2) & 0xfff);
            Block block = chunkSlice.getBlockInstanceInternal(index, 0, null);
            this.tickRandomBlock(block, currentTimeMS, dT);
        }
    }

    private void tickRandomBlock(Block block, long currentTimeMS, float dT) {
        switch (block.blockType()) {
            case BEETROOT:
            case GRASS_BLOCK:
            case FARMLAND:
            case MYCELIUM:
            case SAPLING:
            case LEAVES:
            case SNOW_LAYER:
            case ICE:
            case CROPS:
            case COCOA:
            case VINES:
                this.updateRandomBlock(block, currentTimeMS, dT);
                break;

            default:
                break;
        }
    }

    private void updateRandomBlock(Block block, long currentTimeMS, float dT) {
        long next = ((io.gomint.server.world.block.Block) block)
            .update(UpdateReason.RANDOM, currentTimeMS, dT);

        if (next > currentTimeMS) {
            this.world.addTickingBlock(next, block.position());
        }
    }

    public ChunkSlice ensureSlice(int y) {
        ChunkSlice slice = this.chunkSlices[y];
        if (slice != null) {
            return slice;
        }

        // Ensure all chunk slices till y
        for (int i = 0; i < y; i++) {
            this.internalEnsureChunkSlice(i);
        }

        return this.internalEnsureChunkSlice(y);
    }

    private ChunkSlice internalEnsureChunkSlice(int y) {
        ChunkSlice slice = this.chunkSlices[y];
        if (slice != null) {
            return slice;
        } else {
            this.chunkSlices[y] = new ChunkSlice(this, y);
            return this.chunkSlices[y];
        }
    }

    /**
     * Add a player to this chunk. This is needed to know when we can GC a chunk
     *
     * @param player The player which we want to add to this chunk
     */
    void addPlayer(EntityPlayer player) {
        this.players.add(player);

        if (this.entities == null) {
            this.entities = new Long2ObjectOpenHashMap<>();
        }

        this.entities.put(player.id(), player);
    }

    /**
     * Remove a player from this chunk. This is needed to know when we can GC a chunk
     *
     * @param player The player which we want to remove from this chunk
     */
    void removePlayer(EntityPlayer player) {
        this.players.remove(player);
        this.lastPlayerOnThisChunk = System.currentTimeMillis();

        if (this.entities == null) {
            return;
        }

        this.entities.remove(player.id());
        if (this.entities.size() == 0) {
            this.entities = null;
        }
    }

    /**
     * Add a entity to this chunk
     *
     * @param entity The entity which should be added
     */
    protected void addEntity(Entity<?> entity) {
        if (this.entities == null) {
            this.entities = new Long2ObjectOpenHashMap<>();
        }

        this.entities.put(entity.id(), entity);
    }

    /**
     * Remove a entity from this chunk
     *
     * @param entity The entity which should be removed
     */
    void removeEntity(Entity<?> entity) {
        if (this.entities == null) {
            return;
        }

        this.entities.remove(entity.id());
        if (this.entities.size() == 0) {
            this.entities = null;
        }
    }

    /**
     * Gets the time at which this chunk was last written out to disk.
     *
     * @return The timestamp this chunk was last written out at
     */
    public long lastSavedTimestamp() {
        return this.lastSavedTimestamp;
    }

    /**
     * Sets the timestamp on which this chunk was last written out to disk.
     *
     * @param timestamp The timestamp to set
     */
    void lastSavedTimestamp(long timestamp) {
        this.lastSavedTimestamp = timestamp;

        // Unflag all chunk slices
        for (ChunkSlice chunkSlice : this.chunkSlices) {
            if (chunkSlice != null) {
                chunkSlice.resetPersistenceFlag();
            }
        }
    }

    // ==================================== MANIPULATION ==================================== //

    /**
     * Makes a request to package this chunk asynchronously. The package that will be
     * given to the provided callback will be a world chunk packet inside a batch packet.
     * <p>
     * This operation is done asynchronously in order to limit how many chunks are being
     * packaged in parallel as well as to cache some chunk packets.
     *
     * @param callback The callback to be invoked once the operation is complete
     */
    void packageChunk(Delegate2<Long, ChunkAdapter> callback) {
        this.world.notifyPackageChunk(this.x, this.z, callback);
    }

    /**
     * Checks if this chunk can be gced
     *
     * @param currentTimeMillis The time when this collection cycle started
     * @return true when it can be gced, false when not
     */
    boolean canBeGCed(long currentTimeMillis) {
        int secondsAfterLeft = this.world.config().secondsUntilGCAfterLastPlayerLeft();
        int waitAfterLoad = this.world.config().waitAfterLoadForGCSeconds();

        return this.refCount.get() == 0 &&
            this.populated && currentTimeMillis - this.loadedTime > TimeUnit.SECONDS.toMillis(waitAfterLoad) &&
            this.players.isEmpty() &&
            currentTimeMillis - this.lastPlayerOnThisChunk > TimeUnit.SECONDS.toMillis(secondsAfterLeft);
    }

    /**
     * Return a collection of players which are currently on this chunk
     *
     * @return non modifiable collection of players on this chunk
     */
    public Collection<EntityPlayer> players() {
        return Collections.unmodifiableCollection(this.players);
    }

    /**
     * Gets the x-coordinate of the chunk.
     *
     * @return The chunk's x-coordinate
     */
    @Override
    public int x() {
        return this.x;
    }

    /**
     * Gets the z-coordinate of the chunk.
     *
     * @return The chunk's z-coordinate
     */
    @Override
    public int z() {
        return this.z;
    }

    /**
     * Add a new tile entity to the chunk
     *
     * @param tileEntity The NBT tag of the tile entity which should be added
     */
    protected void addTileEntity(TileEntity tileEntity) {
        BlockPosition tileEntityLocation = tileEntity.getBlock().position();
        int xPos = tileEntityLocation.x() & 0xF;
        int yPos = tileEntityLocation.y();
        int zPos = tileEntityLocation.z() & 0xF;

        ChunkSlice slice = ensureSlice(yPos >> 4);
        slice.addTileEntity(xPos, yPos - slice.sectionY() * 16, zPos, tileEntity);
    }

    /**
     * Sets the ID of a block at the specified coordinates given in chunk coordinates.
     *
     * @param x         The x-coordinate of the block
     * @param y         The y-coordinate of the block
     * @param z         The z-coordinate of the block
     * @param layer     layer on which this block is
     * @param runtimeId The ID to set the block to
     */
    public void block(int x, int y, int z, int layer, int runtimeId) {
        int ySection = y >> 4;
        ChunkSlice slice = ensureSlice(ySection);
        slice.setBlock(x, y - (ySection << 4), z, layer, runtimeId);
    }

    /**
     * Sets the ID of a block at the specified coordinates given in chunk coordinates.
     *
     * @param x     The x-coordinate of the block
     * @param y     The y-coordinate of the block
     * @param z     The z-coordinate of the block
     * @param layer in which the block is
     * @return The ID of the block
     */
    public String getBlock(int x, int y, int z, int layer) {
        ChunkSlice slice = ensureSlice(y >> 4);
        return slice.getBlock(x, y - 16 * (y >> 4), z, layer);
    }

    /**
     * Sets the maximum block height at a specific coordinate-pair.
     *
     * @param x      The x-coordinate relative to the chunk
     * @param z      The z-coordinate relative to the chunk
     * @param height The maximum block height
     */
    private void setHeight(int x, int z, short height) {
        this.height[(z << 4) + x] = height;
    }

    /**
     * Gets the maximum block height at a specific coordinate-pair. Requires the height
     * map to be up-to-date.
     *
     * @param x The x-coordinate relative to the chunk
     * @param z The z-coordinate relative to the chunk
     * @return The maximum block height
     */
    public int getHeight(int x, int z) {
        return this.height[(z << 4) + x];
    }

    @Override
    public ChunkAdapter biome(int x, int z, Biome biome) {
        Precondition.safeWorldAccess(this.world, true);
        this.biomes.setByte((x << 4) + z, (byte) biome.id());
        return this;
    }

    @Override
    public Biome biome(int x, int z) {
        Precondition.safeWorldAccess(this.world, true);
        return Biome.byId(this.biomes.getByte((x << 4) + z));
    }

    @Override
    public <T extends Block> T blockAt(int x, int y, int z) {
        return blockAt(x, y, z, WorldLayer.NORMAL);
    }

    public <T extends Block> T blockAt(int x, int y, int z, int layer) {
        Precondition.safeWorldAccess(this.world, true);
        ChunkSlice slice = ensureSlice(y >> 4);
        return slice.getBlockInstance(x, y & 0x000000F, z, layer);
    }

    @Override
    public <T extends Block> T blockAt(int x, int y, int z, WorldLayer layer) {
        return this.blockAt(x, y, z, layer.ordinal());
    }

    public TemporaryStorage getTemporaryStorage(int x, int y, int z, int layer) {
        ChunkSlice slice = ensureSlice(y >> 4);
        return slice.getTemporaryStorage(x, y - 16 * (y >> 4), z, layer);
    }

    public void resetTemporaryStorage(int x, int y, int z, int layer) {
        ChunkSlice slice = ensureSlice(y >> 4);
        slice.resetTemporaryStorage(x, y - 16 * (y >> 4), z, layer);
    }

    // ==================================== MISCELLANEOUS ==================================== //

    /**
     * Recalculates the height map of the chunk.
     *
     * @param maxHeight max height of this chunk. Used to reduce load on the CPU
     */
    public void calculateHeightmap(int maxHeight) {
        if (maxHeight == 0) {
            return;
        }

        for (int i = 0; i < 16; ++i) {
            for (int k = 0; k < 16; ++k) {
                for (int j = (maxHeight + 16) - 1; j > 0; --j) {
                    if (!this.getBlock(i, j, k, 0).equals("minecraft:air")) { // For height MC uses normal layer (0)
                        this.setHeight(i, k, (short) ((short) j + 1));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Invoked by the world's asynchronous worker thread once the chunk is supposed
     * to actually pack itself into a world chunk packet.
     *
     * @return The world chunk packet that is to be sent
     */
    public PacketWorldChunk createPackagedData(Cache cache, boolean cached) {
        // Detect how much data we can skip
        int topEmpty = 15;
        for (int i = 15; i >= 0; i--) {
            ChunkSlice slice = this.chunkSlices[i];
            if (slice == null || slice.allAir()) {
                topEmpty = i;
            } else {
                break;
            }
        }

        PacketBuffer buffer = new PacketBuffer(cached ? topEmpty * 8 + 16 : (topEmpty + 2) * 4096);

        long[] hashes = new long[topEmpty + 1];
        if (cached) {
            for (int i = 0; i < topEmpty; i++) {
                ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
                PacketBuffer packetBuffer = new PacketBuffer(buf);
                ensureSlice(i).serializeNetwork(packetBuffer);
                packetBuffer.release();
                hashes[i] = cache.add(packetBuffer.getBuffer());
            }

            hashes[hashes.length - 1] = cache.add(this.biomes.asReadOnly().retain().readerIndex(0));
        } else {
            for (int i = 0; i < topEmpty; i++) {
                ensureSlice(i).serializeNetwork(buffer);
            }

            buffer.writeBytes(this.biomes.asReadOnly().readerIndex(0));
        }

        // Border blocks
        buffer.writeSignedVarInt(0);
        // buffer.writeSignedVarInt(0);

        // Write tile entity data
        Collection<TileEntity> tileEntities = this.getTileEntities();
        if (!tileEntities.isEmpty()) {
            NBTWriter nbtWriter = new NBTWriter(buffer.getBuffer(), ByteOrder.LITTLE_ENDIAN);
            nbtWriter.setUseVarint(true);

            for (TileEntity tileEntity : tileEntities) {
                NBTTagCompound compound = new NBTTagCompound("");
                tileEntity.toCompound(compound, SerializationReason.NETWORK);

                try {
                    nbtWriter.write(compound);
                } catch (IOException e) {
                    LOGGER.warn("Could not persist nbt for network", e);
                }
            }
        }

        PacketWorldChunk packet = new PacketWorldChunk();
        packet.setX(this.x);
        packet.setZ(this.z);
        packet.setSubChunkCount(topEmpty);

        if (cached) {
            packet.setCached(true);
            packet.setHashes(hashes);
        }

        packet.setData(buffer.getBuffer().retain());
        buffer.release();
        return packet;
    }

    /**
     * Get all tiles in this chunk for saving the data
     *
     * @return collection of all tiles in this chunks
     */
    public Collection<TileEntity> getTileEntities() {
        List<TileEntity> tileEntities = new ArrayList<>();

        for (ChunkSlice chunkSlice : this.chunkSlices) {
            if (chunkSlice != null && chunkSlice.tileEntities() != null) {
                tileEntities.addAll(chunkSlice.tileEntities().values());
            }
        }

        return tileEntities;
    }

    /**
     * Check if this chunk contains the given entity
     *
     * @param entity The entity which should be checked for
     * @return true if the chunk contains that entity, false if not
     */
    public boolean knowsEntity(Entity<?> entity) {
        return this.entities != null && this.entities.containsKey(entity.id());
    }

    @Override
    public ChunkAdapter iterateAllEntities(Consumer<io.gomint.entity.Entity<?>> entityConsumer) {
        Precondition.safeWorldAccess(this.world, false);
        if (this.entities != null) {
            for (Long2ObjectMap.Entry<io.gomint.entity.Entity<?>> entry : this.entities.long2ObjectEntrySet()) {
                entityConsumer.accept(entry.getValue());
            }
        }

        return this;
    }

    @Override
    public <T extends io.gomint.entity.Entity<?>> ChunkAdapter iterateEntities(Class<T> entityClass, Consumer<T> entityConsumer) {
        Precondition.safeWorldAccess(this.world, false);
        if (this.entities != null) {
            for (Long2ObjectMap.Entry<io.gomint.entity.Entity<?>> entry : this.entities.long2ObjectEntrySet()) {
                if (entityClass.isAssignableFrom(entry.getValue().getClass())) {
                    entityConsumer.accept((T) entry.getValue());
                }
            }
        }

        return this;
    }

    @Override
    public ChunkAdapter block(int x, int y, int z, Block block) {
        return this.block(x, y, z, WorldLayer.NORMAL, block);
    }

    @Override
    public ChunkAdapter block(int x, int y, int z, WorldLayer layer, Block block) {
        Precondition.safeWorldAccess(this.world, true);
        int layerID = layer.ordinal();

        io.gomint.server.world.block.Block implBlock = (io.gomint.server.world.block.Block) block;

        // Copy block id
        this.block(x, y, z, layerID, implBlock.runtimeId());

        // Copy NBT
        if (implBlock.tileEntity() != null) {
            // Get compound
            NBTTagCompound compound = new NBTTagCompound("");
            implBlock.tileEntity().toCompound(compound, SerializationReason.PERSIST);

            // Change position
            int fullX = CoordinateUtils.getChunkMin(this.x) + x;
            int fullZ = CoordinateUtils.getChunkMin(this.z) + z;

            // Change the position
            compound.addValue("x", fullX);
            compound.addValue("y", y);
            compound.addValue("z", fullZ);

            // Create new tile entity
            TileEntity tileEntity = this.world.server().tileEntities().construct(compound,
                this.blockAt(compound.getInteger("x", 0) & 0xF, compound.getInteger("y", 0), compound.getInteger("z", 0) & 0xF));
            this.setTileEntity(x, y, z, tileEntity);
        }

        return this;
    }

    public void setTileEntity(int x, int y, int z, TileEntity tileEntity) {
        ChunkSlice slice = ensureSlice(y >> 4);
        slice.addTileEntity(x, y - 16 * (y >> 4), z, tileEntity);
    }

    public void removeTileEntity(int x, int y, int z) {
        ChunkSlice slice = ensureSlice(y >> 4);
        slice.removeTileEntity(x, y - 16 * (y >> 4), z);
    }

    public long longHashCode() {
        return CoordinateUtils.toLong(this.x, this.z);
    }

    public Long2ObjectMap<io.gomint.entity.Entity<?>> entities() {
        return this.entities;
    }

    public void tickTiles(long currentTimeMS, float dT) {
        for (ChunkSlice chunkSlice : this.chunkSlices) {
            if (chunkSlice != null && chunkSlice.tileEntities() != null) {
                ObjectIterator<Short2ObjectMap.Entry<TileEntity>> iterator = chunkSlice.tileEntities().short2ObjectEntrySet().fastIterator();
                while (iterator.hasNext()) {
                    TileEntity tileEntity = iterator.next().getValue();
                    tileEntity.update(currentTimeMS, dT);
                }
            }
        }
    }

    public int getRuntimeID(int x, int y, int z, int layer) {
        ChunkSlice slice = ensureSlice(y >> 4);
        return slice.getRuntimeID(x, y - 16 * (y >> 4), z, layer);
    }

    public void heightMap(short[] height) {
        this.height = height;
    }

    public void setBiomes(byte[] biomes) {
        this.biomes.setBytes(0, biomes);
    }

    public void release() {
        this.biomes.release();

        for (ChunkSlice slice : this.chunkSlices) {
            if (slice != null) {
                slice.release();
            }
        }
    }

    public boolean isNeedsPersistence() {
        // Ask all sections if they need persistence
        for (ChunkSlice slice : this.chunkSlices) {
            if (slice != null && slice.isNeedsPersistence()) {
                return true;
            }
        }

        return false;
    }

    public void populate() {
        if (!this.populated()) {
            LOGGER.debug("Starting populating chunk {} / {}", this.x(), this.z());

            this.world.chunkGenerator.populate(this);
            this.calculateHeightmap(240);
            this.populated(true);

            this.lastSavedTimestamp(this.world.currentTickTime());
        }
    }

    public void retainForConnection() {
        LOGGER.debug("Incrementing on send ref count: {}", this.refCount.incrementAndGet());
    }

    public void releaseForConnection() {
        LOGGER.debug("Decrementing on send ref count: {}", this.refCount.decrementAndGet());
    }

}
