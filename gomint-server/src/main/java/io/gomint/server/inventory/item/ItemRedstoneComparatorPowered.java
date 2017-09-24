package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 150 )
 public class ItemRedstoneComparatorPowered extends ItemStack implements io.gomint.inventory.item.ItemRedstoneComparatorPowered {

    // CHECKSTYLE:OFF
    public ItemRedstoneComparatorPowered( short data, int amount ) {
        super( 150, data, amount );
    }

    public ItemRedstoneComparatorPowered( short data, int amount, NBTTagCompound nbt ) {
        super( 150, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
