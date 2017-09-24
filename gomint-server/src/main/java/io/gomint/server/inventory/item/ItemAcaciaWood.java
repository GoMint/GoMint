package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 162 )
 public class ItemAcaciaWood extends ItemStack implements io.gomint.inventory.item.ItemAcaciaWood {

    // CHECKSTYLE:OFF
    public ItemAcaciaWood( short data, int amount ) {
        super( 162, data, amount );
    }

    public ItemAcaciaWood( short data, int amount, NBTTagCompound nbt ) {
        super( 162, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
