package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 7 )
 public class ItemBedrock extends ItemStack implements io.gomint.inventory.item.ItemBedrock {

    // CHECKSTYLE:OFF
    public ItemBedrock( short data, int amount ) {
        super( 7, data, amount );
    }

    public ItemBedrock( short data, int amount, NBTTagCompound nbt ) {
        super( 7, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
