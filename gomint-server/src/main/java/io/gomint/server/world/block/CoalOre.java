package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemCoal;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.util.random.FastRandom;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 16 )
public class CoalOre extends Block implements io.gomint.world.block.BlockCoalOre {

    @Override
    public int getBlockId() {
        return 16;
    }

    @Override
    public long getBreakTime() {
        return 4500;
    }

    @Override
    public float getBlastResistance() {
        return 15.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.COAL_ORE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public List<ItemStack> getDrops( ItemStack itemInHand ) {
        if( isCorrectTool( itemInHand ) ) {
            ((WorldAdapter) this.location.getWorld()).createExpOrb( this.location, FastRandom.current().nextInt( 3 ) );
            return new ArrayList<ItemStack>(){{
                add( ItemCoal.create( 1 ) );
            }};
        }

        return new ArrayList<>();
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.PICKAXE;
    }

}
