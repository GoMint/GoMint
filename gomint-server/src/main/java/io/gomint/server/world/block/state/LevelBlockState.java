package io.gomint.server.world.block.state;

import io.gomint.inventory.item.ItemStack;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.world.block.Block;
import io.gomint.world.block.data.Facing;

import java.util.function.Consumer;
import java.util.function.Supplier;
/**
 * @author jihuayu
 * @version 1.0
 */
public class LevelBlockState  extends BlockState<Integer, Integer> {

    private Consumer<Block> maxedProgressConsumer;
    private int max;

    public LevelBlockState(Supplier<String[]> key, int max, Consumer<Block> maxedProgressConsumer) {
        super(v -> key.get());
        this.maxedProgressConsumer = maxedProgressConsumer;
        this.max = max;
    }

    public boolean up(Block block) {
        this.state(block, this.state(block) + 1);
        if (1f - this.state(block) <= MathUtils.EPSILON) {
            this.maxedProgressConsumer.accept(block);
            return false;
        }

        return true;
    }

    @Override
    protected void calculateValueFromState(Block block, Integer state) {
        this.value(block, Math.round(state * this.max));
    }

    @Override
    public void detectFromPlacement(Block newBlock, EntityLiving<?> player, ItemStack<?> placedItem, Facing face, Vector clickVector) {
        this.state(newBlock, 0);
    }

    @Override
    public Integer state(Block block) {
        return this.value(block);
    }
}
