package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 113 )
 public class ItemNetherBrickFence extends ItemStack implements io.gomint.inventory.item.ItemNetherBrickFence {

    // CHECKSTYLE:OFF
    public ItemNetherBrickFence( short data, int amount ) {
        super( 113, data, amount );
    }

    public ItemNetherBrickFence( short data, int amount, NBTTagCompound nbt ) {
        super( 113, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
