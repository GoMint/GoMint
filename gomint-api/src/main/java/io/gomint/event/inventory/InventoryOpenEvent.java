/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.inventory;

import io.gomint.entity.EntityPlayer;
import io.gomint.event.player.CancellablePlayerEvent;
import io.gomint.inventory.Inventory;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class InventoryOpenEvent extends CancellablePlayerEvent<InventoryOpenEvent> {

    private Inventory<?> inventory;

    /**
     * Create new inventory open event
     *
     * @param player    who opened the inventory
     * @param inventory which should be opened
     */
    public InventoryOpenEvent(EntityPlayer player, Inventory<?> inventory) {
        super(player);
        this.inventory = inventory;
    }

    /**
     * Get the inventory which is closed
     *
     * @return inventory which used
     */
    public Inventory<?> inventory() {
        return this.inventory;
    }

    @Override
    public String toString() {
        return "InventoryOpenEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", inventory=" + this.inventory +
            '}';
    }

}
