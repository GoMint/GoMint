package io.gomint.server.world.block;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockScandium;
import io.gomint.world.block.BlockType;

/**
 * @author Kaooot
 * @version 1.0
 */
@RegisterInfo( id = 287 )
public class Scandium extends BlockAtom implements BlockScandium {

    @Override
    public int getBlockId() {
        return 287;
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
        return BlockType.SCANDIUM;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public int getProtons() {
        return 21;
    }

    @Override
    public int getElectrons() {
        return 21;
    }

    @Override
    public int getNeutrons() {
        return 24;
    }

    @Override
    public int getAtomicWeight() {
        return 45;
    }
}
