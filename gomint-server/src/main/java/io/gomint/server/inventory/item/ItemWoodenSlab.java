package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.util.BlockIdentifier;
import io.gomint.server.world.BlockRuntimeIDs;
import io.gomint.world.block.Block;
import io.gomint.world.block.BlockWoodenSlab;
import io.gomint.world.block.data.LogType;

import java.time.Duration;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:wooden_slab")
public class ItemWoodenSlab extends ItemSlab< io.gomint.inventory.item.ItemWoodenSlab> implements io.gomint.inventory.item.ItemWoodenSlab {

    @Override
    public Duration burnTime() {
        return Duration.ofMillis(15000);
    }

    @Override
    public ItemType itemType() {
        return ItemType.WOODEN_SLAB;
    }
    private static final String WOODEN_SLAB_ID = "minecraft:wooden_slab";

    public enum WoodenTypeMagic {
        OAK(WOODEN_SLAB_ID,(short) 0),
        SPRUCE(WOODEN_SLAB_ID,(short) 1),
        BIRCH(WOODEN_SLAB_ID,(short) 2),
        JUNGLE(WOODEN_SLAB_ID,(short) 3),
        ACACIA(WOODEN_SLAB_ID,(short) 4),
        DARK_OAK(WOODEN_SLAB_ID,(short) 5),
        CRIMSON("minecraft:crimson_slab", (short) -1),
        WARPED("minecraft:warped_slab", (short) -1);

        private final String id;
        private final short data;

        WoodenTypeMagic(String id,short data) {
            this.id = id;
            this.data = data;
        }
    }
    @Override
    public Block block() {
        BlockIdentifier identifier = BlockRuntimeIDs.toBlockIdentifier(this.material().replace("double_", ""), null);
        BlockWoodenSlab slab = (BlockWoodenSlab) this.blocks.get(identifier);
        slab.type(this.type());
        slab.top(this.top());
        return slab;
    }

    @Override
    public LogType type() {
        for (WoodenTypeMagic value : WoodenTypeMagic.values()) {
            if (value.id.equals(this.material()) && value.data == this.data() || this.data() == -1) {
                return LogType.valueOf(value.name());
            }
        }
        return LogType.OAK;
    }

    @Override
    public io.gomint.inventory.item.ItemWoodenSlab type(LogType type) {
        WoodenTypeMagic state = WoodenTypeMagic.valueOf(type.name());
        this.material(state.id);
        this.data(state.data);
        return this;
    }
}
