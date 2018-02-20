/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world;

import io.gomint.entity.Entity;
import io.gomint.world.block.Block;

import java.util.Collection;
import java.util.List;

/**
 * @author geNAZt
 * @author BlackyPaw
 * @version 1.0
 */
public interface Chunk {

    /**
     * X coordinate of the chunk
     *
     * @return x coordinate of the chunk
     */
    int getX();

    /**
     * Z coordinate of the chunk
     *
     * @return z coordinate of the chunk
     */
    int getZ();

    /**
     * Gets the block at the specified position.
     *
     * @param x The x-coordinate of the block
     * @param y The y-coordinate of the block
     * @param z The z-coordinate of the block
     * @return The block itself or null if the given coordinates lie not within this chunk
     */
    <T extends Block> T getBlockAt( int x, int y, int z );

    /**
     * Get a list of all entities which are located in this chunk
     *
     * @return either null if there are not entities in this chunk or a list of entities
     */
    Collection<Entity> getEntities();

    /**
     * Set the block at the position to the one given in this method call. Please only use this in
     * {@link io.gomint.world.generator.ChunkGenerator} instances.
     *
     * @param x coordinate in the chunk (0-15) of the block to replace
     * @param y coordinate in the chunk (0-255) of the block to replace
     * @param z coordinate in the chunk (0-15) of the block to replace
     * @param block which should be used to replace selected block
     */
    void setBlock( int x, int y, int z, Block block );

}
