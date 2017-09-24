package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 4 )
 public class ItemCobblestone extends ItemStack implements io.gomint.inventory.item.ItemCobblestone {

    // CHECKSTYLE:OFF
    public ItemCobblestone( short data, int amount ) {
        super( 4, data, amount );
    }

    public ItemCobblestone( short data, int amount, NBTTagCompound nbt ) {
        super( 4, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
