package io.gomint.server.world.block;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 108 )
public class BrickStairs extends Stairs {

    @Override
    public int getBlockId() {
        return 108;
    }

    @Override
    public long getBreakTime() {
        return 3000;
    }

}
