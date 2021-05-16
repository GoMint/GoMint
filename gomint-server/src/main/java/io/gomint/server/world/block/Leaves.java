package io.gomint.server.world.block;

import io.gomint.enchant.EnchantmentFortune;
import io.gomint.inventory.item.ItemApple;
import io.gomint.inventory.item.ItemLeaves;
import io.gomint.inventory.item.ItemSapling;
import io.gomint.inventory.item.ItemShears;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.state.BooleanBlockState;
import io.gomint.server.world.block.state.EnumBlockState;
import io.gomint.world.block.BlockLeaves;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.LogType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:leaves", def = true)
@RegisterInfo(sId = "minecraft:leaves2")
public class Leaves extends Block implements BlockLeaves {

    private static final String OLD_LOG_TYPE = "old_leaf_type";
    private static final String OLD_LOG_ID = "minecraft:leaves";

    private static final String NEW_LOG_TYPE = "new_leaf_type";
    private static final String NEW_LOG_ID = "minecraft:leaves2";

    private enum LeaveTypeMagic {
        OAK(OLD_LOG_ID, OLD_LOG_TYPE, "oak"),
        SPRUCE(OLD_LOG_ID, OLD_LOG_TYPE, "spruce"),
        BIRCH(OLD_LOG_ID, OLD_LOG_TYPE, "birch"),
        JUNGLE(OLD_LOG_ID, OLD_LOG_TYPE, "jungle"),
        ACACIA(NEW_LOG_ID, NEW_LOG_TYPE, "acacia"),
        DARK_OAK(NEW_LOG_ID, NEW_LOG_TYPE, "dark_oak");

        private final String key;
        private final String value;
        private final String blockId;

        LeaveTypeMagic(String blockId, String key, String value) {
            this.key = key;
            this.value = value;
            this.blockId = blockId;
        }
    }

    private static final EnumBlockState<LeaveTypeMagic, String> VARIANT = new EnumBlockState<>(v -> {
        if (v == null) {
            return new String[]{OLD_LOG_TYPE, NEW_LOG_TYPE};
        }

        for (LeaveTypeMagic value : LeaveTypeMagic.values()) {
            if (value.value.equals(v)) {
                return value.key.equals(OLD_LOG_TYPE) ? new String[]{OLD_LOG_TYPE, NEW_LOG_TYPE} : new String[]{NEW_LOG_TYPE, OLD_LOG_TYPE};
            }
        }

        return new String[]{OLD_LOG_TYPE, NEW_LOG_TYPE};
    }, LeaveTypeMagic.values(), v -> v.value, v -> {
        for (LeaveTypeMagic value : LeaveTypeMagic.values()) {
            if (value.value.equals(v)) {
                return value;
            }
        }

        return null;
    });

    private static final BooleanBlockState UPDATE_FOR_DECAY = new BooleanBlockState( () -> new String[]{"update_bit"});
    private static final BooleanBlockState PERSISTENT = new BooleanBlockState( () -> new String[]{"persistent_bit"});

    @Override
    public long breakTime() {
        return 300;
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
    public float blastResistance() {
        return 1.0f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.LEAVES;
    }

    @Override
    public List<ItemStack<?>> drops(ItemStack<?> itemInHand) {
        LogType type = this.type();

        List<ItemStack<?>> items = new ArrayList<>();

        int dropChance = type == LogType.JUNGLE ? 40 : 20;
        EnchantmentFortune fortune = itemInHand.enchantment(EnchantmentFortune.class);
        if (fortune != null && fortune.level() > 0) {
            dropChance -= 2 << fortune.level();
            if (dropChance < 10) {
                dropChance = 10;
            }
        }

        if (ThreadLocalRandom.current().nextInt(dropChance) == 0) {
            ItemSapling sapling = ItemSapling.create(1);
            sapling.type(type);
            items.add(sapling);
        }

        if (type == LogType.OAK || type == LogType.DARK_OAK) {
            dropChance = 100;
            if (fortune != null && fortune.level() > 0) {
                dropChance -= 10 << fortune.level();
                if (dropChance < 40) {
                    dropChance = 40;
                }
            }

            if (ThreadLocalRandom.current().nextInt(dropChance) == 0) {
                ItemApple apple = ItemApple.create(1);
                items.add(apple);
            }
        }

        if (isCorrectTool(itemInHand)) {
            items.add(ItemLeaves.create(1));
        }

        return items;
    }

    @Override
    public Class<? extends ItemStack<?>>[] toolInterfaces() {
        return new Class[]{
            ItemShears.class
        };
    }

    @Override
    public BlockLeaves type(LogType type) {
        if (type == LogType.CRIMSON || type == LogType.WARPED) {
            return this;
        }

        LeaveTypeMagic newState = LeaveTypeMagic.valueOf(type.name());
        this.blockIdOnStateChange(newState.blockId); // We ignore the two keys here to get a known wrong value, the set state below will select the correct runtime id
        VARIANT.state(this, newState);
        return this;
    }

    @Override
    public LogType type() {
        return LogType.valueOf(VARIANT.state(this).name());
    }

    @Override
    public BlockLeaves decay(boolean decay) {
        UPDATE_FOR_DECAY.state(this,decay);
        return this;
    }

    @Override
    public boolean decay() {
        return UPDATE_FOR_DECAY.state(this);
    }

    @Override
    public BlockLeaves persistent(boolean persistent) {
        PERSISTENT.state(this, persistent);
        return this;
    }

    @Override
    public boolean persistent() {
        return PERSISTENT.state(this);
    }
}
