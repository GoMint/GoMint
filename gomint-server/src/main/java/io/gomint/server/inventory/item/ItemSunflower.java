package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 175 )
 public class ItemSunflower extends ItemStack implements io.gomint.inventory.item.ItemSunflower {

    // CHECKSTYLE:OFF
    public ItemSunflower( short data, int amount ) {
        super( 175, data, amount );
    }

    public ItemSunflower( short data, int amount, NBTTagCompound nbt ) {
        super( 175, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
