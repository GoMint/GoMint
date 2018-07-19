/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface BlockTallGrass extends Block {

    public enum Type {
        DEAD_BUSH,
        GRASS,
        FERN,
    }

    /**
     * Set the grass type of this tall grass block
     *
     * @param type of this block
     */
    void setGrassType( Type type );

    /**
     * Get the type of this tall grass block
     *
     * @return type of this block
     */
    Type getGrassType();

}
