/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.math.Location;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerRespawnEvent extends CancellablePlayerEvent<PlayerRespawnEvent> {

    private Location respawnLocation;

    public PlayerRespawnEvent(EntityPlayer player, Location location) {
        super(player);
        this.respawnLocation = location;
    }

    /**
     * Position where the player should respawn
     *
     * @return location of respawn
     */
    public Location respawnLocation() {
        return this.respawnLocation;
    }

    /**
     * Set the location of this respawn
     *
     * @param respawnLocation which should be used to this respawn
     */
    public PlayerRespawnEvent respawnLocation(Location respawnLocation) {
        this.respawnLocation = respawnLocation;
        return this;
    }

    @Override
    public String toString() {
        return "PlayerRespawnEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", respawnLocation=" + this.respawnLocation +
            '}';
    }

}
