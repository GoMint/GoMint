/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.inventory.item.ItemStack;

import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerDeathEvent extends SimplePlayerEvent {

    private String deathMessage;
    private boolean dropInventory;
    private final List<ItemStack<?>> drops;

    public PlayerDeathEvent(EntityPlayer player, String deathMessage, boolean dropInventory, List<ItemStack<?>> drops) {
        super(player);
        this.deathMessage = deathMessage;
        this.dropInventory = dropInventory;
        this.drops = drops;
    }

    /**
     * Get the death message which will be displayed in chat
     *
     * @return death message, can be null
     */
    public String deathMessage() {
        return this.deathMessage;
    }

    /**
     * Set a new death message
     *
     * @param deathMessage which will be used, can be null or empty string to not display anything
     */
    public PlayerDeathEvent deathMessage(String deathMessage) {
        this.deathMessage = deathMessage;
        return this;
    }

    /**
     * Should the inventory of the player be dropped?
     *
     * @return true when if will be dropped, false when not
     */
    public boolean dropInventory() {
        return this.dropInventory;
    }

    /**
     * Set the drop inventory flag
     *
     * @param dropInventory false will not drop the inventory, true will drop the inventory
     */
    public PlayerDeathEvent dropInventory(boolean dropInventory) {
        this.dropInventory = dropInventory;
        return this;
    }

    /**
     * A list of items which will be dropped if {@link #dropInventory()} returns true
     *
     * @return list of items
     */
    public List<ItemStack<?>> drops() {
        return this.drops;
    }

    @Override
    public String toString() {
        return "PlayerDeathEvent{" +
            "player=" + this.player() +
            ", deathMessage='" + this.deathMessage + '\'' +
            ", dropInventory=" + this.dropInventory +
            ", drops=" + this.drops +
            '}';
    }

}
