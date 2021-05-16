package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemShears;
import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemWool;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.state.BlockColorBlockState;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.BlockWool;
import io.gomint.world.block.data.BlockColor;

import java.util.Collections;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:wool" )
public class Wool extends Block implements BlockWool {

    private static final BlockColorBlockState COLOR = new BlockColorBlockState(() -> new String[]{"color"});

    @Override
    public String blockId() {
        return "minecraft:wool";
    }

    @Override
    public long breakTime() {
        return 1200;
    }

    @Override
    public float blastResistance() {
        return 4.0f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.WOOL;
    }

    @Override
    public BlockColor color() {
        return COLOR.state(this);
    }

    @Override
    public BlockWool color(BlockColor color ) {
        COLOR.state( this, color );
        return this;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public Class<? extends ItemStack<?>>[] toolInterfaces() {
        return new Class[]{
            ItemShears.class
        };
    }

    @Override
    public List<ItemStack<?>> drops(ItemStack<?> itemInHand) {
        ItemWool item = ItemWool.create(1);
        item.color(this.color());
        return Collections.singletonList(item);
    }
}
