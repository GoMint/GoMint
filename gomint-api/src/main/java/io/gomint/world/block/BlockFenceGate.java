/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @author jihuayu
 * @version 1.0
 * @stability 3
 */
public interface BlockFenceGate extends BlockDirection<BlockFenceGate> {

    /**
     * Toggle this fence gate
     */
    BlockFenceGate toggle();


    /**
     * Set open for this fence gate
     *
     * @param open true if open, false if not
     * @return block for chaining
     */
    BlockFenceGate open(boolean open);

    /**
     * Check if this fence gate is open
     *
     * @return true if open, false if not
     */
    boolean open();


    //TODO: docs
    BlockFenceGate inWall(boolean inWall);

    //TODO: docs
    boolean inWall();

    /**
     * Set the type for this fence gate
     *
     * @param logType type of wood
     * @return block for chaining
     */
    BlockFenceGate type(LogType logType);

    /**
     * Get the type of fence gate from which this fence gate has been made
     *
     * @return type of fence gate
     */
    LogType type();

}
