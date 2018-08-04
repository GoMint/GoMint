package io.gomint.server.world.block;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockHydrogen;
import io.gomint.world.block.BlockType;

/**
 * @author Kaooot
 * @version 1.0
 */
@RegisterInfo( id = 267 )
public class Hydrogen extends BlockAtom implements BlockHydrogen {

    @Override
    public int getBlockId() {
        return 267;
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
        return BlockType.HYDROGEN;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public int getProtons() {
        return 1;
    }

    @Override
    public int getElectrons() {
        return 1;
    }

    @Override
    public int getNeutrons() {
        return 0;
    }

    @Override
    public int getAtomicWeight() {
        return 1;
    }
}
