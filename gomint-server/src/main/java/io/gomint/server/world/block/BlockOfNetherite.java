package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemDiamondPickaxe;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockBlockOfNetherite;
import io.gomint.world.block.BlockType;

/**
 * @author KingAli
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:netherite_block")
public class BlockOfNetherite extends Block implements BlockBlockOfNetherite {

    @Override
    public String blockId() {
        return "minecraft:netherite_block";
    }

    @Override
    public long breakTime() {
        return 75200;
    }

    @Override
    public Class<? extends ItemStack<?>>[] toolInterfaces() {
        return new Class[]{
            ItemDiamondPickaxe.class
        };
    }

    @Override
    public float blastResistance() {
        return 6000.0f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.BLACKSTONE;
    }
}
