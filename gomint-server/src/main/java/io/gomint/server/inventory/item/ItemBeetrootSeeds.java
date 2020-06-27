package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:beetroot", id = 458)
public class ItemBeetrootSeeds extends ItemStack implements io.gomint.inventory.item.ItemBeetrootSeeds {

    @Override
    public ItemType getType() {
        return ItemType.BEETROOT_SEEDS;
    }

}
