package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 114 )
 public class ItemNetherBrickStairs extends ItemStack implements io.gomint.inventory.item.ItemNetherBrickStairs {

    // CHECKSTYLE:OFF
    public ItemNetherBrickStairs( short data, int amount ) {
        super( 114, data, amount );
    }

    public ItemNetherBrickStairs( short data, int amount, NBTTagCompound nbt ) {
        super( 114, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
