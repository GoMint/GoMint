/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.interfaces;

import io.gomint.entity.EntityPlayer;
import io.gomint.world.World;

/**
 * Marks an event that takes place in relation to a player
 *
 * @author Janmm14
 * @version 1.0
 * @stability 1
 */
public interface PlayerEvent extends EntityEvent {

    /**
     * Get the player which is affected by this event
     *
     * @return the player which is affected by this event
     */
    EntityPlayer player();

    @Override
    default World world() {
        return player().world();
    }

    /**
     * Get the player which is affected by this event
     *
     * @return the player which is affected by this event
     * @deprecated Use {@linkplain #player()} instead
     */
    @Override
    @Deprecated(since = "2.0")
    default EntityPlayer entity() {
        return player();
    }

}
