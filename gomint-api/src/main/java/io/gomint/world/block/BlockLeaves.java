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
 * @version 1.0
 * @stability 3
 */
public interface BlockLeaves extends Block {

    /**
     * Set the type of leave
     *
     * @param type of the leave
     * @return block for chaining
     */
    BlockLeaves type(LogType type);

    /**
     * Get type of leave
     *
     * @return type of leave
     */
    LogType type();


    // TODO: docs
    BlockLeaves decay(boolean decay);

    // TODO: docs
    boolean decay();

    // TODO: docs
    BlockLeaves persistent(boolean persistent);

    // TODO: docs
    boolean persistent();
}
