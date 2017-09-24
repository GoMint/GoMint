package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 47 )
 public class ItemBookshelf extends ItemStack implements io.gomint.inventory.item.ItemBookshelf {

    // CHECKSTYLE:OFF
    public ItemBookshelf( short data, int amount ) {
        super( 47, data, amount );
    }

    public ItemBookshelf( short data, int amount, NBTTagCompound nbt ) {
        super( 47, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
