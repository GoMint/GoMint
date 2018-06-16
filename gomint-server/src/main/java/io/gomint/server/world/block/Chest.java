package io.gomint.server.world.block;

import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.world.block.BlockFace;
import io.gomint.world.block.BlockType;

import io.gomint.inventory.Inventory;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Vector;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.tileentity.ChestTileEntity;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockChest;

import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 54 )
public class Chest extends ContainerBlock implements BlockChest {

    @Override
    public int getBlockId() {
        return 54;
    }

    @Override
    public long getBreakTime() {
        return 3750;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean interact( Entity entity, BlockFace face, Vector facePos, ItemStack item ) {
        ChestTileEntity tileEntity = this.getTileEntity();
        if ( tileEntity != null ) {
            tileEntity.interact( entity, face, facePos, item );
        }

        return true;
    }

    @Override
    public Inventory getInventory() {
        ChestTileEntity tileEntity = this.getTileEntity();
        if ( tileEntity != null ) {
            return tileEntity.getInventory();
        }

        return null;
    }

    @Override
    public boolean needsTileEntity() {
        return true;
    }

    @Override
    TileEntity createTileEntity( NBTTagCompound compound ) {
        return new ChestTileEntity( null, this.location );
    }

    @Override
    public float getBlastResistance() {
        return 12.5f;
    }

    @Override
    public BlockType getType() {
        return BlockType.CHEST;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.AXE;
    }

    @Override
    public List<ItemStack> getDrops( ItemStack itemInHand ) {
        List<ItemStack> items = super.getDrops( itemInHand );

        // We also drop the inventory
        ChestTileEntity chestTileEntity = this.getTileEntity();
        for ( ItemStack itemStack : chestTileEntity.getInventory().getContentsArray() ) {
            if ( itemStack != null ) {
                items.add( itemStack );
            }
        }

        return items;
    }

}
