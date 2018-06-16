package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemDiamondSword;
import io.gomint.inventory.item.ItemIronSword;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 129 )
public class EmeraldOre extends Block implements io.gomint.world.block.BlockEmeraldOre {

    @Override
    public int getBlockId() {
        return 129;
    }

    @Override
    public long getBreakTime() {
        return 4500;
    }

    @Override
    public float getBlastResistance() {
        return 15.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.EMERALD_ORE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.PICKAXE;
    }

}
