package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 83 )
 public class ItemSugarCane extends ItemStack implements io.gomint.inventory.item.ItemSugarCane {

    // CHECKSTYLE:OFF
    public ItemSugarCane( short data, int amount ) {
        super( 83, data, amount );
    }

    public ItemSugarCane( short data, int amount, NBTTagCompound nbt ) {
        super( 83, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
