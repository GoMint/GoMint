package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 6 )
 public class ItemSapling extends ItemStack implements io.gomint.inventory.item.ItemSapling {

    // CHECKSTYLE:OFF
    public ItemSapling( short data, int amount ) {
        super( 6, data, amount );
    }

    public ItemSapling( short data, int amount, NBTTagCompound nbt ) {
        super( 6, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
