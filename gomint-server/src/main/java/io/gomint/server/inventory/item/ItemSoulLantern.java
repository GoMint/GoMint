package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author KingAli
 * @version 1.0
 */
@RegisterInfo(id = 524, sId = "minecraft:soul_lantern")
public class ItemSoulLantern extends ItemStack implements io.gomint.inventory.item.ItemSoulLantern {

    @Override
    public String getBlockId() {
        return "minecraft:soul_lantern";
    }

    @Override
    public ItemType getType() {
        return ItemType.SOUL_LANTERN;
    }
}
