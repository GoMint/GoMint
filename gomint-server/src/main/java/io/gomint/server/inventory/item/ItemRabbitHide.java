package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 415 )
 public class ItemRabbitHide extends ItemStack implements io.gomint.inventory.item.ItemRabbitHide {

    // CHECKSTYLE:OFF
    public ItemRabbitHide( short data, int amount ) {
        super( 415, data, amount );
    }

    public ItemRabbitHide( short data, int amount, NBTTagCompound nbt ) {
        super( 415, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
