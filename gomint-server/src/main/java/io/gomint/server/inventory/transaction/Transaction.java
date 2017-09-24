package io.gomint.server.inventory.transaction;


import io.gomint.inventory.item.ItemStack;
import io.gomint.server.inventory.Inventory;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface Transaction {

    /**
     * Does this transaction have a source inventory?
     *
     * @return true if source item has a inventory to base on, false if not
     */
    boolean hasInventory();

    /**
     * Get the source item from the transaction. Mostly this is the old itemstack from the source inventory.
     * Can also be null when there is no source (like crafting)
     *
     * @return the item source or null if there is none
     */
    ItemStack getSourceItem();

    /**
     * Get the target (consuming) item of this transaction
     *
     * @return the target of this transaction
     */
    ItemStack getTargetItem();

    /**
     * Get the source inventory. This is null when {@link #hasInventory()} is false
     *
     * @return the source inventory or null
     */
    Inventory getInventory();

    /**
     * Get the source slot from the source inventory. This will be -1 if {@link #hasInventory()} is false
     *
     * @return the slot number or -1
     */
    int getSlot();

    /**
     * Called when the transaction has been a success
     */
    void commit();

    /**
     * Called when a transaction failed
     */
    void revert();

}
