package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.Fence;
import io.gomint.server.world.block.FenceGate;
import io.gomint.world.block.Block;
import io.gomint.world.block.data.LogType;

import java.time.Duration;

/**
 * @author geNAZt
 * @author jihuayu
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:fence_gate")
@RegisterInfo(sId = "minecraft:acacia_fence_gate")
@RegisterInfo(sId = "minecraft:jungle_fence_gate")
@RegisterInfo(sId = "minecraft:spruce_fence_gate")
@RegisterInfo(sId = "minecraft:birch_fence_gate")
@RegisterInfo(sId = "minecraft:dark_oak_fence_gate")
@RegisterInfo(sId = "minecraft:warped_fence_gate")
@RegisterInfo(sId = "minecraft:crimson_fence_gate")

public class ItemFenceGate extends ItemStack<io.gomint.inventory.item.ItemFenceGate> implements io.gomint.inventory.item.ItemFenceGate {
    private enum FenceType {
        OAK("minecraft:fence_gate"),
        SPRUCE("minecraft:spruce_fence_gate"),
        BIRCH("minecraft:birch_fence_gate"),
        JUNGLE("minecraft:jungle_fence_gate"),
        ACACIA("minecraft:acacia_fence_gate"),
        DARK_OAK("minecraft:dark_oak_fence_gate"),
        CRIMSON("minecraft:crimson_fence_gate"),
        WARPED("minecraft:warped_fence_gate");

        public String id;

        FenceType(String id) {
            this.id = id;
        }
    }

    @Override
    public ItemType itemType() {
        return ItemType.FENCE_GATE;
    }

    @Override
    public Duration burnTime() {
        return Duration.ofMillis(15000);
    }

    @Override
    public io.gomint.inventory.item.ItemFenceGate type(LogType type) {
        FenceType type1 = FenceType.valueOf(type.name());
        this.material(type1.name());
        return this;
    }

    @Override
    public LogType type() {
        for (FenceType value : FenceType.values()) {
            if ((value.id.equals(this.material()))) {
                return LogType.valueOf(value.name());
            }
        }
        return null;
    }

    @Override
    public Block block() {
        FenceGate block = (FenceGate) super.block();
        block.type(this.type());
        return block;
    }
}
