package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.GlassColor;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 1
 */
public interface ItemStainedGlass extends ItemStack<ItemStainedGlass> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemStainedGlass create( int amount ) {
        return GoMint.instance().createItemStack( ItemStainedGlass.class, amount );
    }


    /**
     * Get the color of this stained glass
     *
     * @return color of this stained glass
     */
    GlassColor color();

    /**
     * Set the color of this stained glass
     *
     * @param color which this stained glass should have
     */
    ItemStainedGlass color(GlassColor color);
}
