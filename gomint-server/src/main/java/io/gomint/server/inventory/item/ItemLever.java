package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 69 )
 public class ItemLever extends ItemStack implements io.gomint.inventory.item.ItemLever {

    // CHECKSTYLE:OFF
    public ItemLever( short data, int amount ) {
        super( 69, data, amount );
    }

    public ItemLever( short data, int amount, NBTTagCompound nbt ) {
        super( 69, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
