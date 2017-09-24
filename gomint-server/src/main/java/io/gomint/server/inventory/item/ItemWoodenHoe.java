package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 290 )
 public class ItemWoodenHoe extends ItemStack implements io.gomint.inventory.item.ItemWoodenHoe {

    // CHECKSTYLE:OFF
    public ItemWoodenHoe( short data, int amount ) {
        super( 290, data, amount );
    }

    public ItemWoodenHoe( short data, int amount, NBTTagCompound nbt ) {
        super( 290, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
