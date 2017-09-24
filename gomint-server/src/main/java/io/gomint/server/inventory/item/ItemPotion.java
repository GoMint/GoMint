package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 373 )
 public class ItemPotion extends ItemStack implements io.gomint.inventory.item.ItemPotion {

    // CHECKSTYLE:OFF
    public ItemPotion( short data, int amount ) {
        super( 373, data, amount );
    }

    public ItemPotion( short data, int amount, NBTTagCompound nbt ) {
        super( 373, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
