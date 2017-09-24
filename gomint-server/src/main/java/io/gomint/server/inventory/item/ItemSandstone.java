package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 24 )
 public class ItemSandstone extends ItemStack implements io.gomint.inventory.item.ItemSandstone {

    // CHECKSTYLE:OFF
    public ItemSandstone( short data, int amount ) {
        super( 24, data, amount );
    }

    public ItemSandstone( short data, int amount, NBTTagCompound nbt ) {
        super( 24, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
