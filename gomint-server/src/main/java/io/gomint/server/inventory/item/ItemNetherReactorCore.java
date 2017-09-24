package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 247 )
 public class ItemNetherReactorCore extends ItemStack implements io.gomint.inventory.item.ItemNetherReactorCore {

    // CHECKSTYLE:OFF
    public ItemNetherReactorCore( short data, int amount ) {
        super( 247, data, amount );
    }

    public ItemNetherReactorCore( short data, int amount, NBTTagCompound nbt ) {
        super( 247, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
