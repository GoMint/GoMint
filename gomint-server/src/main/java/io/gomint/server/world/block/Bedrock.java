package io.gomint.server.world.block;

import io.gomint.server.world.block.state.BooleanBlockState;
import io.gomint.world.block.BlockBedrock;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:bedrock" )
public class Bedrock extends Block implements BlockBedrock {

    private static final BooleanBlockState INFINI_BURN = new BooleanBlockState(() -> new String[]{"infiniburn_bit"});

    @Override
    public String getBlockId() {
        return "minecraft:bedrock";
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
    public BlockType blockType() {
        return BlockType.BEDROCK;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return false;
    }

}
