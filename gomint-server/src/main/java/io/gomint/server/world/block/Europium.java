package io.gomint.server.world.block;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockEuropium;
import io.gomint.world.block.BlockType;

/**
 * @author Kaooot
 * @version 1.0
 */
@RegisterInfo( id = 329 )
public class Europium extends BlockAtom implements BlockEuropium {

    @Override
    public int getBlockId() {
        return 329;
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
        return BlockType.EUROPIUM;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public int getProtons() {
        return 63;
    }

    @Override
    public int getElectrons() {
        return 63;
    }

    @Override
    public int getNeutrons() {
        return 88;
    }

    @Override
    public int getAtomicWeight() {
        return 152;
    }
}
