package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.Block;
import io.gomint.world.block.data.Facing;

/**
 * @author KingAli
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:netherite_chestplate", id = 749 )
public class ItemNetheriteChestplate extends ItemNetheriteArmor<io.gomint.inventory.item.ItemNetheriteChestplate> implements io.gomint.inventory.item.ItemNetheriteChestplate {

    @Override
    public float getReductionValue() {
        return 8;
    }

    @Override
    public boolean interact(EntityPlayer entity, Facing face, Vector clickPosition, Block clickedBlock ) {
        if ( clickedBlock == null ) {
            if ( isBetter( (ItemStack<?>) entity.getArmorInventory().chestplate() ) ) {
                ItemStack<?> old = (ItemStack<?>) entity.getArmorInventory().chestplate();
                entity.getArmorInventory().chestplate( this );
                entity.getInventory().item( entity.getInventory().itemInHandSlot(), old );
            }
        }

        return false;
    }

    @Override
    public ItemType itemType() {
        return ItemType.NETHERITE_CHESTPLATE;
    }
}
