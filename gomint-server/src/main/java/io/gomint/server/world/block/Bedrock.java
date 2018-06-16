package io.gomint.server.world.block;

import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 7 )
public class Bedrock extends Block implements io.gomint.world.block.BlockBedrock {

    @Override
    public int getBlockId() {
        return 7;
    }

    @Override
    public long getBreakTime() {
        return -1;
    }

    @Override
    public boolean onBreak( boolean creative ) {
        return creative;
    }

    @Override
    public float getBlastResistance() {
        return 1.8E7f;
    }

    @Override
    public BlockType getType() {
        return BlockType.BEDROCK;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return false;
    }

}
