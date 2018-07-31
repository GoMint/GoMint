package io.gomint.server.inventory.item;
import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 249 )
public class ItemUpdateGameBlockUpdate2 extends ItemStack {



    @Override
    public ItemType getType() {
        return ItemType.UPDATE_GAME_BLOCK_UPDATE2;
    }

}
