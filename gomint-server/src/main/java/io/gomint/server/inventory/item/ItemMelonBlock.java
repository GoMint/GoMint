package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:melon_block")
public class ItemMelonBlock extends ItemStack< io.gomint.inventory.item.ItemMelonBlock> implements io.gomint.inventory.item.ItemMelonBlock {

    @Override
    public ItemType itemType() {
        return ItemType.MELON_BLOCK;
    }

}
