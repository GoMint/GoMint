package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.BlockCarrots;
import io.gomint.world.block.BlockType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:carrots")
public class Carrots extends Growable implements BlockCarrots {

    @Override
    public String getBlockId() {
        return "minecraft:carrots";
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public long getBreakTime() {
        return 0;
    }

    @Override
    public List<ItemStack> getDrops(ItemStack itemInHand) {
        if (GROWTH.maxed(this)) {
            return new ArrayList<>() {{
                add(world.getServer().items().create(391, (short) 0, (byte) (1 + SEED_RANDOMIZER.next().byteValue()), null)); // Carrot
            }};
        } else {
            return new ArrayList<>() {{
                add(world.getServer().items().create(391, (short) 0, (byte) 1, null)); // Carrot
            }};
        }
    }

    @Override
    public float getBlastResistance() {
        return 0.0f;
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.CARROTS;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

}
