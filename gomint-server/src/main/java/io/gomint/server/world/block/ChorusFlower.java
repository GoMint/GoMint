package io.gomint.server.world.block;

import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.world.block.BlockType;

import io.gomint.inventory.item.*;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 200 )
public class ChorusFlower extends Block implements io.gomint.world.block.BlockChorusFlower {

    @Override
    public int getBlockId() {
        return 200;
    }

    @Override
    public long getBreakTime() {
        return 600;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public float getBlastResistance() {
        return 2f;
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.AXE;
    }

    @Override
    public BlockType getType() {
        return BlockType.CHORUS_FLOWER;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

}
