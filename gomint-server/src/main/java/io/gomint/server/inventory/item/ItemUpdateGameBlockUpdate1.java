package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 248 )
 public class ItemUpdateGameBlockUpdate1 extends ItemStack implements io.gomint.inventory.item.ItemUpdateGameBlockUpdate1 {

    // CHECKSTYLE:OFF
    public ItemUpdateGameBlockUpdate1( short data, int amount ) {
        super( 248, data, amount );
    }

    public ItemUpdateGameBlockUpdate1( short data, int amount, NBTTagCompound nbt ) {
        super( 248, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
