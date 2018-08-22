package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 162 )
 public class ItemAcaciaWood extends ItemStack implements io.gomint.inventory.item.ItemAcaciaWood {



    @Override
    public ItemType getType() {
        return ItemType.ACACIA_WOOD;
    }

}