package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
public class ItemAcaciaLeaves extends ItemStack< io.gomint.inventory.item.ItemAcaciaLeaves> implements io.gomint.inventory.item.ItemAcaciaLeaves {

    @Override
    public ItemType itemType() {
        return ItemType.ACACIA_LEAVES;
    }

}
