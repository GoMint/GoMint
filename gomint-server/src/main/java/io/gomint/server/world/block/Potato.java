package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.util.random.FastRandom;
import io.gomint.world.block.BlockPotato;
import io.gomint.world.block.BlockType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 142 )
public class Potato extends Growable implements BlockPotato {

    @Override
    public int getBlockId() {
        return 142;
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
    public List<ItemStack> getDrops( ItemStack itemInHand ) {
        if ( getBlockData() >= 0x07 ) {
            List<ItemStack> drops = new ArrayList<ItemStack>() {{
                add( world.getServer().getItems().create( 392, (short) 0, (byte) ( 1 + SEED_RANDOMIZER.next().byteValue() ), null ) ); // Potato
            }};

            if ( FastRandom.current().nextDouble() > 0.98 ) {
                drops.add( world.getServer().getItems().create( 394, (short) 0, (byte) 1, null ) ); // Poison potato on top!
            }

            return drops;
        } else {
            return new ArrayList<ItemStack>() {{
                add( world.getServer().getItems().create( 392, (short) 0, (byte) 1, null ) ); // Potato
            }};
        }
    }

    @Override
    public float getBlastResistance() {
        return 0.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.POTATO;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

}
