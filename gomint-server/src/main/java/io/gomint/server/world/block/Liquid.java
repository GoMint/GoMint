package io.gomint.server.world.block;

import io.gomint.world.block.BlockLiquid;

/**
 * @author geNAZt
 * @version 1.0
 */
public abstract class Liquid extends Block implements BlockLiquid {

    @Override
    public float getFillHeight() {
        short data = this.getBlockData();
        if ( data > 8 ) {
            data = 8;
        }

        return ( ( data + 1 ) / 9f );
    }

}
