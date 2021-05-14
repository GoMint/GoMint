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
public class PlayerDropItemEvent extends CancellablePlayerEvent<PlayerDropItemEvent> {

    private final ItemStack<?> itemStack;

    public PlayerDropItemEvent(EntityPlayer player, ItemStack<?> itemStack) {
        super(player);
        this.itemStack = itemStack;
    }

    /**
     * Get the item stack which should be dropped
     *
     * @return item stack which should be dropped
     */
    public ItemStack<?> itemStack() {
        return this.itemStack;
    }

    @Override
    public String toString() {
        return "PlayerDropItemEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", itemStack=" + this.itemStack +
            '}';
    }

}
