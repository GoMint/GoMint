package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 43 )
 public class ItemDoubleStoneSlab extends ItemStack implements io.gomint.inventory.item.ItemDoubleStoneSlab {

    // CHECKSTYLE:OFF
    public ItemDoubleStoneSlab( short data, int amount ) {
        super( 43, data, amount );
    }

    public ItemDoubleStoneSlab( short data, int amount, NBTTagCompound nbt ) {
        super( 43, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
