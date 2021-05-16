package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.Block;
import io.gomint.world.block.BlockStainedGlassPane;
import io.gomint.world.block.data.GlassColor;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:glass_pane")
@RegisterInfo(sId = "minecraft:stained_glass_pane")
public class ItemStainedGlassPane extends ItemStack< io.gomint.inventory.item.ItemStainedGlassPane> implements io.gomint.inventory.item.ItemStainedGlassPane {

    @Override
    public ItemType itemType() {
        return ItemType.STAINED_GLASS_PANE;
    }
    @Override
    public GlassColor color() {
        return GlassColor.values()[this.data()];
    }

    @Override
    public io.gomint.inventory.item.ItemStainedGlassPane color(GlassColor color) {
        this.data((short) color.ordinal());
        return this;
    }

    @Override
    public Block block() {
        BlockStainedGlassPane block = (BlockStainedGlassPane) super.block();
        block.color(this.color());
        return block;
    }
}
