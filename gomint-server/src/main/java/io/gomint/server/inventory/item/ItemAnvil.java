package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 145 )
 public class ItemAnvil extends ItemStack implements io.gomint.inventory.item.ItemAnvil {

    // CHECKSTYLE:OFF
    public ItemAnvil( short data, int amount ) {
        super( 145, data, amount );
    }

    public ItemAnvil( short data, int amount, NBTTagCompound nbt ) {
        super( 145, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
