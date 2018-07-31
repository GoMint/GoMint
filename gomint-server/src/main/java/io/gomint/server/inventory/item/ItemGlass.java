package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 20 )
 public class ItemGlass extends ItemStack implements io.gomint.inventory.item.ItemGlass {



    @Override
    public ItemType getType() {
        return ItemType.GLASS;
    }

}
