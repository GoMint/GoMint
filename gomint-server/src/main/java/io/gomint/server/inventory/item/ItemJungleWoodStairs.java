package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 136 )
 public class ItemJungleWoodStairs extends ItemStack implements io.gomint.inventory.item.ItemJungleWoodStairs {

    // CHECKSTYLE:OFF
    public ItemJungleWoodStairs( short data, int amount ) {
        super( 136, data, amount );
    }

    public ItemJungleWoodStairs( short data, int amount, NBTTagCompound nbt ) {
        super( 136, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
