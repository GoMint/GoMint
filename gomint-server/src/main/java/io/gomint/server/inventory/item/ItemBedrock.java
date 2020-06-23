package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 7 )
 public class ItemBedrock extends ItemStack implements io.gomint.inventory.item.ItemBedrock {

    @Override
    public String getBlockId() {
        return "minecraft:bedrock";
    }

    @Override
    public ItemType getType() {
        return ItemType.BEDROCK;
    }

}
