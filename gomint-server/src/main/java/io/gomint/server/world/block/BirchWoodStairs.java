package io.gomint.server.world.block;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 135 )
public class BirchWoodStairs extends Stairs {

    @Override
    public int getBlockId() {
        return 135;
    }

    @Override
    public long getBreakTime() {
        return 3000;
    }

}
