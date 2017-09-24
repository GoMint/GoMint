package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 336 )
 public class ItemBrick extends ItemStack implements io.gomint.inventory.item.ItemBrick {

    // CHECKSTYLE:OFF
    public ItemBrick( short data, int amount ) {
        super( 336, data, amount );
    }

    public ItemBrick( short data, int amount, NBTTagCompound nbt ) {
        super( 336, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
