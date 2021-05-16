package io.gomint.server.world.block;

import com.google.common.collect.Lists;
import io.gomint.inventory.item.ItemFence;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.server.world.block.state.EnumBlockState;
import io.gomint.world.block.BlockFence;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.data.LogType;

import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:fence")
@RegisterInfo(sId = "minecraft:crimson_fence")
@RegisterInfo(sId = "minecraft:warped_fence")
public class Fence extends Block implements BlockFence {

    private static final String OLD_WOOD_ID = "minecraft:fence";
    private static final String OLD_WOOD_TYPE = "wood_type";

    private enum LogTypeMagic {
        OAK(OLD_WOOD_ID, "oak"),
        SPRUCE(OLD_WOOD_ID, "spruce"),
        BIRCH(OLD_WOOD_ID, "birch"),
        JUNGLE(OLD_WOOD_ID, "jungle"),
        ACACIA(OLD_WOOD_ID, "acacia"),
        DARK_OAK(OLD_WOOD_ID, "dark_oak"),
        CRIMSON("minecraft:crimson_fence", ""),
        WARPED("minecraft:warped_fence", "");

        private final String blockId;
        private final String value;

        LogTypeMagic(String blockId, String value) {
            this.blockId = blockId;
            this.value = value;
        }
    }

    private static final EnumBlockState<LogTypeMagic, String> VARIANT = new EnumBlockState<>(v -> {
        return new String[]{OLD_WOOD_TYPE};
    }, LogTypeMagic.values(), v -> v.value, v -> {
        for (LogTypeMagic value : LogTypeMagic.values()) {
            if (value.value.equals(v)) {
                return value;
            }
        }

        return null;
    });

    @Override
    public long breakTime() {
        return 3000;
    }

    @Override
    public boolean transparent() {
        return true;
    }

    @Override
    public float blastResistance() {
        return 15.0f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.FENCE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public Class<? extends ItemStack<?>>[] toolInterfaces() {
        return ToolPresets.AXE;
    }

    @Override
    public LogType type() {
        switch (this.blockId()) {
            case "minecraft:crimson_fence":
                return LogType.CRIMSON;
            case "minecraft:warped_fence":
                return LogType.WARPED;
        }

        return LogType.valueOf(VARIANT.state(this).name());
    }

    @Override
    public BlockFence type(LogType logType) {
        LogTypeMagic newState = LogTypeMagic.valueOf(logType.name());

        if (!newState.value.isEmpty()) {
            VARIANT.state(this, newState);
        }

        if (!this.blockId().equals(newState.blockId)) {
            this.blockId(newState.blockId);
        }

        return this;
    }

    @Override
    public List<ItemStack<?>> drops(ItemStack<?> itemInHand) {
        ItemFence item = ItemFence.create(1).type(this.type());
        return Lists.newArrayList(item);
    }
}
