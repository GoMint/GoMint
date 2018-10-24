package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 143 )
 public class ItemWoodenButton extends ItemStack implements io.gomint.inventory.item.ItemWoodenButton {

    @Override
    public long getBurnTime() {
        return 15000;
    }

    @Override
    public String getBlockId() {
        return "minecraft:wooden_button";
    }

    @Override
    public ItemType getType() {
        return ItemType.WOODEN_BUTTON;
    }

}
