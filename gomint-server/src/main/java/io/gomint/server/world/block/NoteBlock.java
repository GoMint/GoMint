package io.gomint.server.world.block;

import io.gomint.server.entity.Entity;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Vector;
import io.gomint.server.entity.tileentity.NoteblockTileEntity;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 25 )
public class NoteBlock extends Block {

    @Override
    public int getBlockId() {
        return 25;
    }

    @Override
    public long getBreakTime() {
        return 1200;
    }

    @Override
    public boolean interact( Entity entity, int face, Vector facePos, ItemStack item ) {
        NoteblockTileEntity tileEntity = getTileEntity();
        if ( tileEntity != null ) {
            tileEntity.interact( entity, face, facePos, item );
        }

        return true;
    }

}
