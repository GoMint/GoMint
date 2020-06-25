package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author KingAli
 * @version 1.0
 */
@RegisterInfo(id = 492, sId = "minecraft:soul_fire")
public class ItemSoulFire extends ItemStack implements io.gomint.inventory.item.ItemSoulFire {

    @Override
    public String getBlockId() {
        return "minecraft:soul_fire";
    }

    @Override
    public ItemType getType() {
        return ItemType.SOUL_FIRE;
    }
}
