package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author KingAli
 * @version 1.0
 */
@RegisterInfo( id = 458 )
public class ItemLoom extends ItemStack implements io.gomint.inventory.item.ItemLoom {

    @Override
    public String getBlockId() {
        return "minecraft:loom";
    }

    @Override
    public ItemType getType() {
        return ItemType.LOOM;
    }

}
