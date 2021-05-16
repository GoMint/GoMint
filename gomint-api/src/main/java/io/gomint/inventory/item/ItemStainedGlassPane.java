package io.gomint.inventory.item;

import io.gomint.GoMint;
import io.gomint.world.block.data.GlassColor;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 1
 */
public interface ItemStainedGlassPane extends ItemStack<ItemStainedGlassPane> {

    /**
     * Create a new item stack with given class and amount
     *
     * @param amount which is used for the creation
	 * @return freshly generated item
     */
    static ItemStainedGlassPane create( int amount ) {
        return GoMint.instance().createItemStack( ItemStainedGlassPane.class, amount );
    }



    /**
     * Get the color of this glass pane
     *
     * @return color of this glass pane
     */
    GlassColor color();

    /**
     * Set the color of this glass pane
     *
     * @param color which this glass pane should have
     */
    ItemStainedGlassPane color(GlassColor color);
}
