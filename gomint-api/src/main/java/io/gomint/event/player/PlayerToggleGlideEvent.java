/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerToggleGlideEvent extends CancellablePlayerEvent<PlayerToggleFlyEvent> {

    private final boolean newStatus;

    public PlayerToggleGlideEvent(EntityPlayer player, boolean newStatus) {
        super(player);
        this.newStatus = newStatus;
    }

    /**
     * Get the status the client wants to set.
     *
     * @return true when the client wants to start flying, false otherwise
     */
    public boolean newStatus() {
        return this.newStatus;
    }

    @Override
    public String toString() {
        return "PlayerToggleGlideEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", newStatus=" + this.newStatus +
            '}';
    }

}
