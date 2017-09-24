package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 414 )
 public class ItemRabbitFoot extends ItemStack implements io.gomint.inventory.item.ItemRabbitFoot {

    // CHECKSTYLE:OFF
    public ItemRabbitFoot( short data, int amount ) {
        super( 414, data, amount );
    }

    public ItemRabbitFoot( short data, int amount, NBTTagCompound nbt ) {
        super( 414, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
