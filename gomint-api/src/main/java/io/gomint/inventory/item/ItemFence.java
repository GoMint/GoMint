package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public interface ItemFence extends ItemStack<ItemFence>, ItemBurnable {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemFence create( int amount ) {	
        return GoMint.instance().createItemStack( ItemFence.class, amount );
    }

    /**
     * Set the type of fence
     *
     * @param type of fence
     */
    ItemFence type(LogType type);

    /**
     * Get the type of this fence
     *
     * @return type of fence
     */
    LogType type();
}
