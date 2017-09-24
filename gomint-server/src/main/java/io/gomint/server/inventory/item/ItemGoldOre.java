package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 14 )
 public class ItemGoldOre extends ItemStack implements io.gomint.inventory.item.ItemGoldOre {

    // CHECKSTYLE:OFF
    public ItemGoldOre( short data, int amount ) {
        super( 14, data, amount );
    }

    public ItemGoldOre( short data, int amount, NBTTagCompound nbt ) {
        super( 14, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
