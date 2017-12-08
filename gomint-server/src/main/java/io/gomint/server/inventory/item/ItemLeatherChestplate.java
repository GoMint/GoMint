package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.inventory.item.ItemAir;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.Block;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 299 )
public class ItemLeatherChestplate extends ItemArmor implements io.gomint.inventory.item.ItemLeatherChestplate {

    // CHECKSTYLE:OFF
    public ItemLeatherChestplate( short data, int amount ) {
        super( 299, data, amount );
    }

    public ItemLeatherChestplate( short data, int amount, NBTTagCompound nbt ) {
        super( 299, data, amount, nbt );
    }
    // CHECKSTYLE:ON

    @Override
    public float getReductionValue() {
        return 3;
    }

    @Override
    public boolean interact( EntityPlayer entity, int face, Vector clickPosition, Block clickedBlock ) {
        if ( clickedBlock == null ) {
            if ( isBetter( (ItemStack) entity.getArmorInventory().getChestplate() ) ) {
                entity.getArmorInventory().setChestplate( this );
                entity.getInventory().setItem( entity.getInventory().getItemInHandSlot(), ItemAir.create( 0 ) );
            }
        }

        return false;
    }

    @Override
    public ItemType getType() {
        return ItemType.LEATHER_CHESTPLATE;
    }

}