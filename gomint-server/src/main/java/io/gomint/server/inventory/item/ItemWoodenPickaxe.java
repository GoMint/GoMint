package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 270 )
public class ItemWoodenPickaxe extends ItemTool implements io.gomint.inventory.item.ItemWoodenPickaxe {

    // CHECKSTYLE:OFF
    public ItemWoodenPickaxe( short data, int amount ) {
        super( 270, data, amount );
    }

    public ItemWoodenPickaxe( short data, int amount, NBTTagCompound nbt ) {
        super( 270, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
