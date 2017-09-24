package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 347 )
 public class ItemClock extends ItemStack implements io.gomint.inventory.item.ItemClock {

    // CHECKSTYLE:OFF
    public ItemClock( short data, int amount ) {
        super( 347, data, amount );
    }

    public ItemClock( short data, int amount, NBTTagCompound nbt ) {
        super( 347, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
