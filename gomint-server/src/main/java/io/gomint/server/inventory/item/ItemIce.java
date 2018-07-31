package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 79 )
 public class ItemIce extends ItemStack implements io.gomint.inventory.item.ItemIce {



    @Override
    public ItemType getType() {
        return ItemType.ICE;
    }

}
