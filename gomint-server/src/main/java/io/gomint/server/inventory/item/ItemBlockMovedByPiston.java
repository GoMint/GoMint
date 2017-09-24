package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 250 )
 public class ItemBlockMovedByPiston extends ItemStack implements io.gomint.inventory.item.ItemBlockMovedByPiston {

    // CHECKSTYLE:OFF
    public ItemBlockMovedByPiston( short data, int amount ) {
        super( 250, data, amount );
    }

    public ItemBlockMovedByPiston( short data, int amount, NBTTagCompound nbt ) {
        super( 250, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
