package io.gomint.server.world.block;

import io.gomint.world.block.BlockType;

import io.gomint.server.entity.EntityLiving;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockFlowingWater;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:flowing_water" )
public class FlowingWater extends Liquid implements BlockFlowingWater {

    @Override
    public long getBreakTime() {
        return 150000;
    }

    @Override
    public boolean transparent() {
        return true;
    }

    @Override
    public float getBlastResistance() {
        return 500.0f;
    }

    @Override
    public int getTickDiff() {
        return 250;
    }

    @Override
    public void onEntityStanding( EntityLiving entityLiving ) {
        if ( entityLiving.isOnFire() ) {
            entityLiving.extinguish();
        }
    }

    @Override
    public BlockType blockType() {
        return BlockType.FLOWING_WATER;
    }

    @Override
    public boolean isFlowing() {
        return true;
    }

}
