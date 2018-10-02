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
@RegisterInfo( id = 4 )
public class EnchantmentProjectileProtection extends Enchantment implements io.gomint.enchant.EnchantmentProjectileProtection {

    /**
     * Create new enchantment projectile protection
     */
    public EnchantmentProjectileProtection() {
        super( (short) 4 );
    }

    @Override
    public byte getMinEnchantAbility( short level ) {
        return (byte) ( 3 + ( level - 1 ) * 6 );
    }

    @Override
    public byte getMaxEnchantAbility( short level ) {
        return (byte) ( getMinEnchantAbility( level ) + 15 );
    }

    @Override
    public boolean canBeApplied( ItemStack itemStack ) {
        return itemStack.getType() == ItemType.CHAIN_HELMET ||
            itemStack.getType() == ItemType.DIAMOND_HELMET ||
            itemStack.getType() == ItemType.GOLDEN_HELMET ||
            itemStack.getType() == ItemType.IRON_HELMET ||
            itemStack.getType() == ItemType.LEATHER_HELMET ||
            itemStack.getType() == ItemType.CHAIN_LEGGINGS ||
            itemStack.getType() == ItemType.DIAMOND_LEGGINGS ||
            itemStack.getType() == ItemType.GOLDEN_LEGGINGS ||
            itemStack.getType() == ItemType.IRON_LEGGINGS ||
            itemStack.getType() == ItemType.LEATHER_LEGGINGS ||
            itemStack.getType() == ItemType.CHAIN_CHESTPLATE ||
            itemStack.getType() == ItemType.DIAMOND_CHESTPLATE ||
            itemStack.getType() == ItemType.GOLDEN_CHESTPLATE ||
            itemStack.getType() == ItemType.IRON_CHESTPLATE ||
            itemStack.getType() == ItemType.LEATHER_CHESTPLATE ||
            itemStack.getType() == ItemType.CHAIN_BOOTS ||
            itemStack.getType() == ItemType.DIAMOND_BOOTS ||
            itemStack.getType() == ItemType.GOLDEN_BOOTS ||
            itemStack.getType() == ItemType.IRON_BOOTS ||
            itemStack.getType() == ItemType.LEATHER_BOOTS;
    }

}
