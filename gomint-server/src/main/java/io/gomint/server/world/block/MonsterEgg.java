package io.gomint.server.world.block;

import io.gomint.world.block.BlockMonsterEgg;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:monster_egg" )
public class MonsterEgg extends Block implements BlockMonsterEgg {

    @Override
    public long breakTime() {
        return 1125;
    }

    @Override
    public float getBlastResistance() {
        return 3.75f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.MONSTER_EGG;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

}
