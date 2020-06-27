package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:hopper", id = 154)
public class ItemHopperBlock extends ItemStack {

    @Override
    public ItemType getType() {
        return ItemType.HOPPER_BLOCK;
    }

}
