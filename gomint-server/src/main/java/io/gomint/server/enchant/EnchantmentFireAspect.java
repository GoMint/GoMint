/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.enchant;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.inventory.item.ItemStack;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 13 )
public class EnchantmentFireAspect extends Enchantment implements io.gomint.enchant.EnchantmentFireAspect {

    /**
     * Create new enchantment smite
     *
     * @param level of this enchantment
     */
    public EnchantmentFireAspect() {
        super( (short) 2 );
    }

    @Override
    public byte getMinEnchantAbility( short level ) {
        return (byte) ( 10 + ( level - 1 ) * 20 );
    }

    @Override
    public byte getMaxEnchantAbility( short level ) {
        return (byte) ( getMinEnchantAbility( level ) + 50 );
    }

    @Override
    public boolean canBeApplied( ItemStack itemStack ) {
        return itemStack.getType() == ItemType.DIAMOND_SWORD ||
            itemStack.getType() == ItemType.STONE_SWORD ||
            itemStack.getType() == ItemType.GOLDEN_SWORD ||
            itemStack.getType() == ItemType.IRON_SWORD ||
            itemStack.getType() == ItemType.WOODEN_SWORD;
    }

}
