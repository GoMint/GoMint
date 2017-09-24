package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 323 )
public class ItemSign extends ItemStack implements io.gomint.inventory.item.ItemSign {

    // CHECKSTYLE:OFF
    public ItemSign( short data, int amount ) {
        super( 323, data, amount );
    }

    public ItemSign( short data, int amount, NBTTagCompound nbt ) {
        super( 323, data, amount, nbt );
    }
    // CHECKSTYLE:ON

    @Override
    public int getBlockId() {
        return 63;
    }

}
