package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 395 )
 public class ItemMap extends ItemStack implements io.gomint.inventory.item.ItemMap {

    // CHECKSTYLE:OFF
    public ItemMap( short data, int amount ) {
        super( 395, data, amount );
    }

    public ItemMap( short data, int amount, NBTTagCompound nbt ) {
        super( 395, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
