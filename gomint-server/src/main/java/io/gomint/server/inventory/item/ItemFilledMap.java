package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 358 )
 public class ItemFilledMap extends ItemStack implements io.gomint.inventory.item.ItemFilledMap {



    @Override
    public ItemType getType() {
        return ItemType.FILLED_MAP;
    }

}
