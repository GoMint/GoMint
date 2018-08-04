package io.gomint.server.world.block;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockTitanium;
import io.gomint.world.block.BlockType;

/**
 * @author Kaooot
 * @version 1.0
 */
@RegisterInfo( id = 288 )
public class Titanium extends BlockAtom implements BlockTitanium {

    @Override
    public int getBlockId() {
        return 288;
    }

    @Override
    public long getBreakTime() {
        return 0;
    }

    @Override
    public float getBlastResistance() {
        return 0;
    }

    @Override
    public BlockType getType() {
        return BlockType.TITANIUM;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public int getProtons() {
        return 22;
    }

    @Override
    public int getElectrons() {
        return 22;
    }

    @Override
    public int getNeutrons() {
        return 24;
    }

    @Override
    public int getAtomicWeight() {
        return 48;
    }
}
