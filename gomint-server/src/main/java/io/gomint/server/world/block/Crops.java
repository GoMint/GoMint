package io.gomint.server.world.block;

import io.gomint.world.block.BlockType;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.inventory.item.Items;
import io.gomint.server.registry.RegisterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 59 )
public class Crops extends Growable {

    @Override
    public int getBlockId() {
        return 59;
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
                add( Items.create( 296, (short) 0, (byte) 1, null ) ); // Beetroot
            }};

            // Randomize seeds
            int amountOfSeeds = SEED_RANDOMIZER.next();
            if ( amountOfSeeds > 0 ) {
                drops.add( Items.create( 295, (short) 0, (byte) amountOfSeeds, null ) ); // Seeds
            }

            return drops;
        } else {
            return new ArrayList<ItemStack>() {{
                add( Items.create( 295, (short) 0, (byte) 1, null ) ); // Seeds
            }};
        }
    }

    @Override
    public float getBlastResistance() {
        return 0.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.CROPS;
    }

}