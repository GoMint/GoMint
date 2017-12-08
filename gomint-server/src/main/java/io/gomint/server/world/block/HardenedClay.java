package io.gomint.server.world.block;

import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 172 )
public class HardenedClay extends Block implements io.gomint.world.block.BlockHardenedClay {

    @Override
    public int getBlockId() {
        return 172;
    }

    @Override
    public long getBreakTime() {
        return 1875;
    }

    @Override
    public float getBlastResistance() {
        return 7.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.HARDENED_CLAY;
    }

}