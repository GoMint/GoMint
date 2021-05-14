/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.event.Event;
import io.gomint.event.interfaces.PlayerEvent;

/**
 * Represents a not cancellable event with a player involved
 *
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public class SimplePlayerEvent extends Event implements PlayerEvent {

    private final EntityPlayer player;

    /**
     * Create a new player based event
     *
     * @param player for which this event is
     */

    public SimplePlayerEvent(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public EntityPlayer player() {
        return this.player;
    }

    @Override
    public String toString() {
        return "SimplePlayerEvent{" +
            "player=" + this.player +
            '}';
    }

}
