package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 208 )
 public class ItemEndRod extends ItemStack implements io.gomint.inventory.item.ItemEndRod {

    // CHECKSTYLE:OFF
    public ItemEndRod( short data, int amount ) {
        super( 208, data, amount );
    }

    public ItemEndRod( short data, int amount, NBTTagCompound nbt ) {
        super( 208, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
