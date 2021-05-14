/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.world.World;

import javax.annotation.Nullable;

/**
 * Event is called when a player is about to leave a world (through teleportation or when leaving the server, called
 * before {@linkplain PlayerQuitEvent}).
 * <br><br>
 * Event is called in old world's thread. Event is called after
 * {@linkplain io.gomint.event.entity.EntityTeleportEvent EntityTeleportEvent}.
 * 
 * @author Janmm14
 * @version 1.0
 * @stability 2
 */
public class PlayerWorldLeaveEvent extends SimplePlayerEvent {

    @Nullable
    private final World toWorld;

    /**
     * Create an event for announcing a player joining a world
     *
     * @param player the player joining the world
     * @param toWorld new world of player or {@code null} if leaving the server
     */
    public PlayerWorldLeaveEvent(EntityPlayer player, @Nullable World toWorld) {
        super(player);
        this.toWorld = toWorld;
    }

    /**
     * Get new world of player or {@code null} if leaving the server
     * @return new world of player or {@code null} if leaving the server
     */
    @Nullable
    public World to() {
        return this.toWorld;
    }

    @Override
    public String toString() {
        return "PlayerWorldLeaveEvent{" +
            "player=" + this.player() +
            ", world=" + this.world() +
            ", to=" + this.toWorld +
            '}';
    }

}
