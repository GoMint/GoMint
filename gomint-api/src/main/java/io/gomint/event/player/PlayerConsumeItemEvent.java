/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.inventory.item.ItemStack;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerConsumeItemEvent extends CancellablePlayerEvent<PlayerConsumeItemEvent> {

    private final ItemStack<?> itemStack;

    /**
     * Create a new consumer item event which gets called when a player tires to eat/drink a item
     *
     * @param player    which consumes the item
     * @param itemStack which gets consumed
     */
    public PlayerConsumeItemEvent(EntityPlayer player, ItemStack<?> itemStack) {
        super(player);
        this.itemStack = itemStack;
    }

    /**
     * Get the itemstack which should be consumed
     *
     * @return itemstack which should be consumed
     */
    public ItemStack<?> itemStack() {
        return this.itemStack;
    }

    @Override
    public String toString() {
        return "PlayerConsumeItemEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", itemStack=" + this.itemStack +
            '}';
    }

}
