package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:brewing_stand", id = 379)
public class ItemBrewingStand extends ItemStack< io.gomint.inventory.item.ItemBrewingStand> implements io.gomint.inventory.item.ItemBrewingStand {

    @Override
    public ItemType itemType() {
        return ItemType.BREWING_STAND;
    }

}
