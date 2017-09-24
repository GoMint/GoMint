package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 59 )
 public class ItemCrops extends ItemStack implements io.gomint.inventory.item.ItemCrops {

    // CHECKSTYLE:OFF
    public ItemCrops( short data, int amount ) {
        super( 59, data, amount );
    }

    public ItemCrops( short data, int amount, NBTTagCompound nbt ) {
        super( 59, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
