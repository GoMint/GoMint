/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.interfaces;

import io.gomint.world.World;

/**
 * Marks an event that takes place in relation to a world
 *
 * @author Janmm14
 * @version 1.0
 * @stability 1
 * @see #world() 
 */
public interface WorldEvent {

    /**
     * Get the world this event originates from
     *
     * @return the world this event originates from
     */
    World world();

}
