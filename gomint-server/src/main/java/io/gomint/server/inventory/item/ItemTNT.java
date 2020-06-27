package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:tnt", id = 46)
public class ItemTNT extends ItemStack implements io.gomint.inventory.item.ItemTNT {

    @Override
    public ItemType getType() {
        return ItemType.TNT;
    }

}
