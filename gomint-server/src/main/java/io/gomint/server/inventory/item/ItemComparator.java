package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 404 )
 public class ItemComparator extends ItemStack implements io.gomint.inventory.item.ItemComparator {

    // CHECKSTYLE:OFF
    public ItemComparator( short data, int amount ) {
        super( 404, data, amount );
    }

    public ItemComparator( short data, int amount, NBTTagCompound nbt ) {
        super( 404, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
