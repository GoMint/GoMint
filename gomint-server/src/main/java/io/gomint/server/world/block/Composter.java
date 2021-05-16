package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.server.world.block.state.LevelBlockState;
import io.gomint.world.block.BlockComposter;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.Facing;

/**
 * @author KingAli
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:composter")
public class Composter extends Block implements BlockComposter {

    private static final LevelBlockState LEVEL = new LevelBlockState(() -> new String[]{"composter_fill_level"}, 8, aVoid -> {});

    @Override
    public float blastResistance() {
        return 3.5f;
    }

    @Override
    public long breakTime() {
        return 900;
    }

    @Override
    public Class<? extends ItemStack<?>>[] toolInterfaces() {
        return ToolPresets.AXE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return false;
    }

    @Override
    public BlockType blockType() {
        return BlockType.COMPOSTER;
    }

    @Override
    public BlockComposter level(int level) {
        LEVEL.state(this,level);
        return this;
    }

    @Override
    public int level() {
        return LEVEL.state(this);
    }

    @Override
    public boolean beforePlacement(EntityLiving<?> entity, ItemStack<?> item, Facing face, Location location, Vector clickVector) {
        LEVEL.detectFromPlacement(this, entity, item, face, clickVector);
        return super.beforePlacement(entity, item, face, location, clickVector);
    }
}
