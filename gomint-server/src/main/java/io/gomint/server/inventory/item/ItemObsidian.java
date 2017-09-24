package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 49 )
 public class ItemObsidian extends ItemStack implements io.gomint.inventory.item.ItemObsidian {

    // CHECKSTYLE:OFF
    public ItemObsidian( short data, int amount ) {
        super( 49, data, amount );
    }

    public ItemObsidian( short data, int amount, NBTTagCompound nbt ) {
        super( 49, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
