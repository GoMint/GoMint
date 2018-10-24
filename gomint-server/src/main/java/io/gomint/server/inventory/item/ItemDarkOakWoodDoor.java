package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 431 )
public class ItemDarkOakWoodDoor extends ItemStack implements io.gomint.inventory.item.ItemDarkOakWoodDoor {

    @Override
    public long getBurnTime() {
        return 10000;
    }

    @Override
    public String getBlockId() {
        return "minecraft:dark_oak_door";
    }

    @Override
    public ItemType getType() {
        return ItemType.DARK_OAK_DOOR;
    }

}
