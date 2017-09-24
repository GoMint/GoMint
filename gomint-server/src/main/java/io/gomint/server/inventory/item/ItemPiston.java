package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 33 )
 public class ItemPiston extends ItemStack implements io.gomint.inventory.item.ItemPiston {

    // CHECKSTYLE:OFF
    public ItemPiston( short data, int amount ) {
        super( 33, data, amount );
    }

    public ItemPiston( short data, int amount, NBTTagCompound nbt ) {
        super( 33, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
