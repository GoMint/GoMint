package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 361 )
 public class ItemPumpkinSeeds extends ItemStack implements io.gomint.inventory.item.ItemPumpkinSeeds {

    // CHECKSTYLE:OFF
    public ItemPumpkinSeeds( short data, int amount ) {
        super( 361, data, amount );
    }

    public ItemPumpkinSeeds( short data, int amount, NBTTagCompound nbt ) {
        super( 361, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
