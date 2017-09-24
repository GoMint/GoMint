package io.gomint.server.inventory.item;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 116 )
 public class ItemEnchantmentTable extends ItemStack implements io.gomint.inventory.item.ItemEnchantmentTable {

    // CHECKSTYLE:OFF
    public ItemEnchantmentTable( short data, int amount ) {
        super( 116, data, amount );
    }

    public ItemEnchantmentTable( short data, int amount, NBTTagCompound nbt ) {
        super( 116, data, amount, nbt );
    }
    // CHECKSTYLE:ON

}
