/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.BlockColor;
import io.gomint.world.block.data.Direction;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface BlockBed extends Block {

    /**
     * Get the color of this bed
     *
     * @return color of this bed
     */
    BlockColor color();

    /**
     * Set the color of this bed
     *
     * @param color which should be used from now on
     * @return block for chaining
     */
    BlockBed color(BlockColor color);

    /**
     * Get the other half of this multi block structure,
     *
     * @return the other half of the bed or null when no other half has been found
     */
    BlockBed otherHalf();

    /**
     * Is this block the head part of this multi block structure?
     *
     * @return true if this is the head part, false if not
     */
    boolean head();

    /**
     * Set this as top part of the multi block
     *
     * @param value if the block is head or not
     * @return block for chaining
     */
    BlockBed head(boolean value);

    //TODO: docs
    Direction face();

    //TODO: docs
    BlockBed face(Direction value);
}
