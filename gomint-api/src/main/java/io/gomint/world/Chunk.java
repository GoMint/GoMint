/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world;

import io.gomint.entity.Entity;
import io.gomint.world.block.Block;
import io.gomint.world.generator.ChunkGenerator;

import java.util.function.Consumer;

/**
 * @author geNAZt
 * @author BlackyPaw
 * @version 1.0
 * @stability 3
 */
public interface Chunk {

    /**
     * X coordinate of the chunk
     *
     * @return x coordinate of the chunk
     */
    int x();

    /**
     * Z coordinate of the chunk
     *
     * @return z coordinate of the chunk
     */
    int z();

    /**
     * Gets the block at the specified position.
     *
     * @param x The x-coordinate of the block
     * @param y The y-coordinate of the block
     * @param z The z-coordinate of the block
     * @return The block itself or null if the given coordinates lie not within this chunk
     */
    <T extends Block> T blockAt(int x, int y, int z );

    /**
     * Gets the block at the specified position.
     *
     * @param x     The x-coordinate of the block
     * @param y     The y-coordinate of the block
     * @param z     The z-coordinate of the block
     * @param layer on which the block is
     * @return The block itself or null if the given coordinates lie not within this chunk
     */
    <T extends Block> T blockAt(int x, int y, int z, WorldLayer layer );

    /**
     * Iterate over all entities in this chunk and run entityConsumer on every entity.
     *
     * @param entityConsumer which gets called for every found entity
     * @return chunk for chaining
     */
    Chunk iterateAllEntities(Consumer<Entity<?>> entityConsumer);

    /**
     * Iterate over all entities in this chunk and run entityConsumer on every correct one.
     *
     * @param entityClass    for which we search
     * @param entityConsumer which gets called for every found entity
     * @param <T>            type of entity
     * @return chunk for chaining
     */
    <T extends Entity<?>> Chunk iterateEntities( Class<T> entityClass, Consumer<T> entityConsumer );

    /**
     * Set the block at the position to the one given in this method call. Please only use this in
     * {@link ChunkGenerator} instances.
     *
     * @param x     coordinate in the chunk (0-15) of the block to replace
     * @param y     coordinate in the chunk (0-255) of the block to replace
     * @param z     coordinate in the chunk (0-15) of the block to replace
     * @param block which should be used to replace selected block
     * @return chunk for chaining
     */
    Chunk block(int x, int y, int z, Block block );

    /**
     * Set the block at the position to the one given in this method call. Please only use this in
     * {@link ChunkGenerator} instances.
     *
     * @param x     coordinate in the chunk (0-15) of the block to replace
     * @param y     coordinate in the chunk (0-255) of the block to replace
     * @param z     coordinate in the chunk (0-15) of the block to replace
     * @param layer on which the block should be placed
     * @param block which should be used to replace selected block
     * @return chunk for chaining
     */
    Chunk block(int x, int y, int z, WorldLayer layer, Block block );

    /**
     * Sets a block column's biome.
     *
     * @param x     The x-coordinate of the block column
     * @param z     The z-coordinate of the block column
     * @param biome The biome to set
     * @return chunk for chaining
     */
    Chunk biome(int x, int z, Biome biome );

    /**
     * Gets a block column's biome.
     *
     * @param x The x-coordinate of the block column
     * @param z The z-coordinate of the block column
     * @return The block column's biome
     */
    Biome biome(int x, int z );

    /**
     * Get the world from which this chunk comes from
     *
     * @return world of this chunk
     */
    World world();

}
