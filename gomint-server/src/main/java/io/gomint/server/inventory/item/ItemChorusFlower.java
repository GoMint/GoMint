package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 200 )
 public class ItemChorusFlower extends ItemStack implements io.gomint.inventory.item.ItemChorusFlower {

    // CHECKSTYLE:OFF
    public ItemChorusFlower( short data, int amount ) {
        super( 200, data, amount );
    }

    public ItemChorusFlower( short data, int amount, NBTTagCompound nbt ) {
        super( 200, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
