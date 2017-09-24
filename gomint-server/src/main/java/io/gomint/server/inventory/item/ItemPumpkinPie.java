package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 400 )
 public class ItemPumpkinPie extends ItemStack implements io.gomint.inventory.item.ItemPumpkinPie {

    // CHECKSTYLE:OFF
    public ItemPumpkinPie( short data, int amount ) {
        super( 400, data, amount );
    }

    public ItemPumpkinPie( short data, int amount, NBTTagCompound nbt ) {
        super( 400, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
