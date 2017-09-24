package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 107 )
 public class ItemFenceGate extends ItemStack implements io.gomint.inventory.item.ItemFenceGate {

    // CHECKSTYLE:OFF
    public ItemFenceGate( short data, int amount ) {
        super( 107, data, amount );
    }

    public ItemFenceGate( short data, int amount, NBTTagCompound nbt ) {
        super( 107, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
