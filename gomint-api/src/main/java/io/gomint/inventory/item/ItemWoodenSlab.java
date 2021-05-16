package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public interface ItemWoodenSlab extends ItemSlab<ItemWoodenSlab>, ItemBurnable {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemWoodenSlab create( int amount ) {
        return GoMint.instance().createItemStack( ItemWoodenSlab.class, amount );
    }

    /**
     * Get the type of wooden this slab has
     *
     * @return type of wooden
     */
    LogType type();

    /**
     * Set the type of wooden for this block
     *
     * @param type for this block
     * @return block for chaining
     */
    ItemWoodenSlab type(LogType type);
}
