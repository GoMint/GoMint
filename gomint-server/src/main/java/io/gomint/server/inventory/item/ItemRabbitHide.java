package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 415 )
 public class ItemRabbitHide extends ItemStack implements io.gomint.inventory.item.ItemRabbitHide {



    @Override
    public ItemType getType() {
        return ItemType.RABBIT_HIDE;
    }

}
