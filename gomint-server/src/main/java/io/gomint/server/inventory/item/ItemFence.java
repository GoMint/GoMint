package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.Fence;
import io.gomint.world.block.Block;
import io.gomint.world.block.data.LogType;

import java.time.Duration;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:fence")
@RegisterInfo(sId = "minecraft:crimson_fence")
@RegisterInfo(sId = "minecraft:warped_fence")

public class ItemFence extends ItemStack<io.gomint.inventory.item.ItemFence> implements io.gomint.inventory.item.ItemFence {
    private enum FenceType {
        OAK("minecraft:fence", (short) 0),
        SPRUCE("minecraft:fence", (short) 1),
        BIRCH("minecraft:fence", (short) 2),
        JUNGLE("minecraft:fence", (short) 3),
        ACACIA("minecraft:fence", (short) 4),
        DARK_OAK("minecraft:fence", (short) 5),
        CRIMSON("minecraft:crimson_fence", (short) -1),
        WARPED("minecraft:warped_fence", (short) -1);

        public String id;
        public short data;

        FenceType(String id, short data) {
            this.data = data;
            this.id = id;
        }
    }

    @Override
    public ItemType itemType() {
        return ItemType.FENCE;
    }

    @Override
    public Duration burnTime() {
        if (this.data() != -1)
            return Duration.ofMillis(15000);
        return null;
    }

    @Override
    public io.gomint.inventory.item.ItemFence type(LogType type) {
        FenceType type1 = FenceType.valueOf(type.name());
        this.material(type1.name());
        if (type1.data != -1) {
            this.data(type1.data);
        } else {
            this.data((short) 0);
        }
        return this;
    }

    @Override
    public LogType type() {
        for (FenceType value : FenceType.values()) {
            if ((value.id.equals(this.material()) && (this.data() == value.data || value.data == -1))) {
                return LogType.valueOf(value.name());
            }
        }
        return null;
    }

    @Override
    public Block block() {
        Fence block = (Fence) super.block();
        block.type(this.type());
        return block;
    }
}
