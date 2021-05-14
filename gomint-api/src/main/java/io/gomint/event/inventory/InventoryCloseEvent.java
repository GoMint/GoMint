package io.gomint.event.inventory;

import io.gomint.entity.EntityPlayer;
import io.gomint.event.player.SimplePlayerEvent;
import io.gomint.inventory.Inventory;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class InventoryCloseEvent extends SimplePlayerEvent {

    private Inventory<?> inventory;

    /**
     * Create a new inventory close event
     *
     * @param player    which closed the inventory
     * @param inventory which has been closed
     */
    public InventoryCloseEvent(EntityPlayer player, Inventory<?> inventory) {
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
        return "InventoryCloseEvent{" +
            "player=" + this.player() +
            ", inventory=" + this.inventory +
            '}';
    }

}
