package io.gomint.server.world.block;

import io.gomint.inventory.item.*;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.world.block.BlockType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 173 )
public class BlockOfCoal extends Block implements io.gomint.world.block.BlockBlockOfCoal {

    @Override
    public int getBlockId() {
        return 173;
    }

    @Override
    public long getBreakTime() {
        return 7500;
    }

    @Override
    public float getBlastResistance() {
        return 30.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.BLOCK_OF_COAL;
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.PICKAXE;
    }

    @Override
    public List<ItemStack> getDrops( ItemStack itemInHand ) {
        if ( isCorrectTool( itemInHand ) ) {
            return new ArrayList<ItemStack>() {{
                add( ItemBlockOfCoal.create( 1 ) );
            }};
        }

        return new ArrayList<>();
    }

}
