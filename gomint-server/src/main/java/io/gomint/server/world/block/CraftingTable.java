package io.gomint.server.world.block;

import io.gomint.server.entity.Entity;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.registry.RegisterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 58 )
public class CraftingTable extends Block {

    private static final Logger LOGGER = LoggerFactory.getLogger( CraftingTable.class );

    @Override
    public int getBlockId() {
        return 58;
    }

    @Override
    public long getBreakTime() {
        return 3750;
    }

    @Override
    public boolean interact( Entity entity, int face, Vector facePos, ItemStack item ) {
        if ( entity instanceof EntityPlayer ) {
            // This should be a container open
            LOGGER.debug( "Changing to 3x3 crafting grid for player " + ( (EntityPlayer) entity ).getName() );
            ( (EntityPlayer) entity ).getCraftingInventory().resizeAndClear( 9 );
            ( (EntityPlayer) entity ).getCraftingInputInventory().resizeAndClear( 9 );
        }

        return true;
    }

}
