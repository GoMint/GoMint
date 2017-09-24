package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 206 )
 public class ItemEndBricks extends ItemStack implements io.gomint.inventory.item.ItemEndBricks {

    // CHECKSTYLE:OFF
    public ItemEndBricks( short data, int amount ) {
        super( 206, data, amount );
    }

    public ItemEndBricks( short data, int amount, NBTTagCompound nbt ) {
        super( 206, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
