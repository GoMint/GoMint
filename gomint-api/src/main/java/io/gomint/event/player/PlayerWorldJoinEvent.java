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
 * Event is called when a player joins a world (through teleportation or after server join, called after
 * {@linkplain PlayerJoinEvent}).
 * <br><br>
 * Event is called in new world's thread. Event is called after
 * {@linkplain io.gomint.event.entity.EntityTeleportEvent EntityTeleportEvent} and {@linkplain PlayerWorldLeaveEvent}.
 *
 * @author Janmm14
 * @version 1.0
 * @stability 2
 */
public class PlayerWorldJoinEvent extends SimplePlayerEvent {

    @Nullable
    private final World fromWorld;

    /**
     * Create an event for announcing a player joining a world
     *
     * @param player the player joining the world
     */
    public PlayerWorldJoinEvent(EntityPlayer player, @Nullable World fromWorld) {
        super(player);
        this.fromWorld = fromWorld;
    }

    /**
     * Get old world of player or {@code null} if joining the server
     *
     * @return old world of player or {@code null} if joining the server
     */
    @Nullable
    public World from() {
        return this.fromWorld;
    }

    @Override
    public String toString() {
        return "PlayerWorldJoinEvent{" +
            "player=" + this.player() +
            ", world=" + this.world() +
            ", from=" + this.fromWorld +
            '}';
    }

}
