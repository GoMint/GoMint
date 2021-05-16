package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.state.EnumBlockState;
import io.gomint.world.block.BlockStainedGlass;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.GlassColor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:stained_glass")
@RegisterInfo(sId = "minecraft:glass")
public class StainedGlass extends Block implements BlockStainedGlass {

    private static final EnumBlockState<GlassColor, String> COLOR = new EnumBlockState<>(v -> new String[]{"color"}, GlassColor.values(), e -> e.name().toLowerCase(), v -> GlassColor.valueOf(v.toUpperCase()));

//    @Override
//    public String blockId() {
//        return "minecraft:stained_glass";
//    }

    @Override
    public long breakTime() {
        return 450;
    }

    @Override
    public boolean transparent() {
        return true;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public List<ItemStack<?>> drops(ItemStack<?> itemInHand) {
        return new ArrayList<>();
    }

    @Override
    public float blastResistance() {
        return 1.5f;
    }

    @Override
    public BlockType blockType() {
        if (this.color()==GlassColor.TRANSPARENT)
            return BlockType.GLASS;
        return BlockType.STAINED_GLASS;
    }

    @Override
    public GlassColor color() {
        return COLOR.state(this);
    }

    @Override
    public BlockStainedGlass color(GlassColor color) {
        COLOR.state(this, color);
        return this;
    }

}
