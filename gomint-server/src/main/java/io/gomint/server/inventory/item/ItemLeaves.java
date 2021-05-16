package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.Leaves;
import io.gomint.world.block.Block;
import io.gomint.world.block.data.LogType;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:leaves")
@RegisterInfo(sId = "minecraft:leaves2")
public class ItemLeaves extends ItemStack<io.gomint.inventory.item.ItemLeaves> implements io.gomint.inventory.item.ItemLeaves {
    private enum LeavesType {
        OAK("minecraft:leaves", (short) 0),
        SPRUCE("minecraft:leaves", (short) 1),
        BIRCH("minecraft:leaves", (short) 2),
        JUNGLE("minecraft:leaves", (short) 3),
        ACACIA("minecraft:leaves2", (short) 0),
        DARK_OAK("minecraft:leaves2", (short) 1),
        CRIMSON("", (short) 0),
        WARPED("", (short) 0);

        public String id;
        public short data;

        LeavesType(String id, short data) {
            this.data = data;
            this.id = id;
        }
    }

    @Override
    public ItemType itemType() {
        return ItemType.LEAVES;
    }

    @Override
    public io.gomint.inventory.item.ItemLeaves type(LogType type) {
        LeavesType type1 = LeavesType.valueOf(type.name());
        this.material(type.name());
        this.data((short) (this.data() & (~3) | type1.data));
        return this;
    }

    @Override
    public LogType type() {
        for (ItemLeaves.LeavesType value : ItemLeaves.LeavesType.values()) {
            if ((value.id.equals(this.material()) && (this.data() & 3) == value.data)) {
                return LogType.valueOf(value.name());
            }
        }
        return null;
    }

    @Override
    public io.gomint.inventory.item.ItemLeaves decay(boolean decay) {
        if (decay) {
            this.data((short) (this.data() & 7));
        } else {
            this.data((short) (this.data() & 3));
        }
        return this;
    }

    @Override
    public boolean decay() {
        return (this.data() & 4) != 0;
    }

    @Override
    public io.gomint.inventory.item.ItemLeaves persistent(boolean persistent) {
        if (persistent) {
            this.data((short) (this.data() & 15));
        } else {
            this.data((short) (this.data() & 7));
        }
        return this;
    }

    @Override
    public boolean persistent() {
        return (this.data() & 8) != 0;
    }

    @Override
    public Block block() {
        Leaves block = (Leaves) super.block();
        block.type(this.type());
        block.persistent(this.persistent());
        block.decay(this.decay());
        return block;
    }
}
