package io.gomint.server.world.block;

import io.gomint.world.block.BlockType;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockAir;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 0 )
public class Air extends Block implements BlockAir {

    @Override
    public int getBlockId() {
        return 0;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public boolean onBreak( boolean creative ) {
        return false;
    }

    @Override
    public boolean canBeReplaced( ItemStack item ) {
        return true;
    }

    @Override
    public float getBlastResistance() {
        return 0.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.AIR;
    }

    @Override
    public boolean canBeFlowedInto() {
        return true;
    }

}
