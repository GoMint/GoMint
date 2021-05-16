package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public interface ItemLeaves extends ItemStack<ItemLeaves> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemLeaves create( int amount ) {
        return GoMint.instance().createItemStack( ItemLeaves.class, amount );
    }


    /**
     * Set the type of log
     *
     * @param type of log
     */
    ItemLeaves type(LogType type);

    /**
     * Get the type of this log
     *
     * @return type of log
     */
    LogType type();

    // TODO: docs
    ItemLeaves decay(boolean decay);

    // TODO: docs
    boolean decay();

    // TODO: docs
    ItemLeaves persistent(boolean persistent);

    // TODO: docs
    boolean persistent();


}
