package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.Block;
import io.gomint.world.block.BlockStainedGlass;
import io.gomint.world.block.data.GlassColor;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:stained_glass")
@RegisterInfo(sId = "minecraft:glass")
public class ItemStainedGlass extends ItemStack< io.gomint.inventory.item.ItemStainedGlass> implements io.gomint.inventory.item.ItemStainedGlass {

    @Override
    public ItemType itemType() {
        if (this.color()==GlassColor.TRANSPARENT)
            return ItemType.GLASS;
        return ItemType.STAINED_GLASS;
    }

    @Override
    public GlassColor color() {
        return GlassColor.values()[this.data()];
    }

    @Override
    public io.gomint.inventory.item.ItemStainedGlass color(GlassColor color) {
        this.data((short) color.ordinal());
        return this;
    }

    @Override
    public Block block() {
        BlockStainedGlass block = (BlockStainedGlass) super.block();
        block.color(this.color());
        return block;
    }
}
