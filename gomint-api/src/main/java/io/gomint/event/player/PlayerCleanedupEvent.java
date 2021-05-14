/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * This event gets called once the entity has been cleaned up and despawned. There is NO data left in the server except
 * the object copy in this event.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerCleanedupEvent extends SimplePlayerEvent {

    /**
     * Construct a quit event with the player who disconnected
     *
     * @param player The player which disconnected
     */
    public PlayerCleanedupEvent(EntityPlayer player) {
        super(player);
    }

    @Override
    public String toString() {
        return "PlayerCleanedupEvent{" +
            "player=" + this.player() +
            '}';
    }

}
