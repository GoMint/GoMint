/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.GlassColor;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 1
 */
public interface BlockStainedGlassPane extends Block {

    /**
     * Get the color of this block
     *
     * @return color of this block
     */
    GlassColor color();

    /**
     * Set the color of this block
     *
     * @param color which this block should be
     */
    BlockStainedGlassPane color(GlassColor color );

}
