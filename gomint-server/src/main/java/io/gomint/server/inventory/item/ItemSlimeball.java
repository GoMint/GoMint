package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 341 )
 public class ItemSlimeball extends ItemStack implements io.gomint.inventory.item.ItemSlimeball {

    // CHECKSTYLE:OFF
    public ItemSlimeball( short data, int amount ) {
        super( 341, data, amount );
    }

    public ItemSlimeball( short data, int amount, NBTTagCompound nbt ) {
        super( 341, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
