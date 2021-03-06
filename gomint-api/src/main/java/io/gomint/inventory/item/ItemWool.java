package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.BlockColor;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface ItemWool extends ItemStack<ItemWool> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemWool create( int amount ) {
        return GoMint.instance().createItemStack( ItemWool.class, amount );
    }

    /**
     * Get the color of this wool
     *
     * @return color of this wool
     */
    BlockColor color();

    /**
     * Set the color of this wool
     *
     * @param color which this wool should have
     */
    ItemWool color(BlockColor color);

}
