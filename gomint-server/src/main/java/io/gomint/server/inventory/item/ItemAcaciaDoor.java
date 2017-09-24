package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 430 )
 public class ItemAcaciaDoor extends ItemStack implements io.gomint.inventory.item.ItemAcaciaDoor {

    // CHECKSTYLE:OFF
    public ItemAcaciaDoor( short data, int amount ) {
        super( 430, data, amount );
    }

    public ItemAcaciaDoor( short data, int amount, NBTTagCompound nbt ) {
        super( 430, data, amount, nbt );
    }
    // CHECKSTYLE:ON

    @Override
    public int getBlockId() {
        return 196;
    }
}
