package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 324 )
public class ItemWoodenDoor extends ItemStack implements io.gomint.inventory.item.ItemWoodenDoor {

    // CHECKSTYLE:OFF
    public ItemWoodenDoor( short data, int amount ) {
        super( 324, data, amount );
    }

    public ItemWoodenDoor( short data, int amount, NBTTagCompound nbt ) {
        super( 324, data, amount, nbt );
    }
    // CHECKSTYLE:ON

    @Override
    public int getBlockId() {
        return 64;
    }

}
