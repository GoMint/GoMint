package io.gomint.server.world.block;

import io.gomint.world.block.BlockHayBale;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:hay_block" )
public class HayBale extends Block implements BlockHayBale {

    @Override
    public String getBlockId() {
        return "minecraft:hay_block";
    }

    @Override
    public long breakTime() {
        return 750;
    }

    @Override
    public float getBlastResistance() {
        return 2.5f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.HAY_BALE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

}
