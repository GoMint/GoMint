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
public class PlayerFoodLevelChangeEvent extends CancellablePlayerEvent<PlayerFoodLevelChangeEvent> {

    private final float change;

    /**
     * Create a new event for changing food level on a player
     *
     * @param player for which we want to change the food level
     * @param change amount which should be applied after this event
     */
    public PlayerFoodLevelChangeEvent(EntityPlayer player, float change) {
        super(player);
        this.change = change;
    }

    /**
     * Amount of change this event should apply when not cancelled
     *
     * @return amount of change
     */
    public float change() {
        return this.change;
    }

    @Override
    public String toString() {
        return "PlayerFoodLevelChangeEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", change=" + this.change +
            '}';
    }

}
