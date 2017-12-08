package io.gomint.server.world.block;

import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 153 )
public class NetherQuartzOre extends Block implements io.gomint.world.block.BlockNetherQuartzOre {

    @Override
    public int getBlockId() {
        return 153;
    }

    @Override
    public long getBreakTime() {
        return 4500;
    }

    @Override
    public float getBlastResistance() {
        return 5.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.NETHER_QUARTZ_ORE;
    }

}