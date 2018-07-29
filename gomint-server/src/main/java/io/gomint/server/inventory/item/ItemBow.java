package io.gomint.server.inventory.item;

import io.gomint.event.entity.projectile.ProjectileLaunchEvent;
import io.gomint.inventory.item.ItemAir;
import io.gomint.inventory.item.ItemType;
import io.gomint.enchant.EnchantmentFlame;
import io.gomint.enchant.EnchantmentInfinity;
import io.gomint.enchant.EnchantmentPower;
import io.gomint.enchant.EnchantmentPunch;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.projectile.EntityArrow;
import io.gomint.server.inventory.item.annotation.UseDataAsDamage;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Gamemode;

/**
 * @author geNAZt
 * @version 1.0
 */
@UseDataAsDamage
@RegisterInfo( id = 261 )
public class ItemBow extends ItemStack implements io.gomint.inventory.item.ItemBow {

    // CHECKSTYLE:OFF
    public ItemBow( short data, int amount ) {
        super( 261, data, amount );
    }

    public ItemBow( short data, int amount, NBTTagCompound nbt ) {
        super( 261, data, amount, nbt );
    }
    // CHECKSTYLE:ON

    @Override
    public byte getMaximumAmount() {
        return 1;
    }

    @Override
    public short getMaxDamage() {
        return 360;
    }

    /**
     * Shoot this bow
     *
     * @param player which will shoot this bow
     */
    public void shoot( EntityPlayer player ) {
        // Check if player did start
        if ( player.getActionStart() == -1 ) {
            return;
        }

        // Check for force calculation
        float force = calculateForce( player );
        if ( force == -1f ) {
            return;
        }

        player.setUsingItem( false );

        // Check for arrows in inventory
        boolean foundArrow = false;
        for ( int i = 0; i < player.getInventory().size(); i++ ) {
            ItemStack itemStack = (ItemStack) player.getInventory().getItem( i );
            if ( itemStack instanceof ItemArrow ) {
                foundArrow = true;

                if ( this.getEnchantment( EnchantmentInfinity.class ) == null ) {
                    itemStack.afterPlacement();
                }
            }
        }

        // Check offhand if not found
        if ( !foundArrow ) {
            for ( int i = 0; i < player.getOffhandInventory().size(); i++ ) {
                ItemStack itemStack = (ItemStack) player.getInventory().getItem( i );
                if ( itemStack instanceof ItemArrow ) {
                    foundArrow = true;

                    if ( this.getEnchantment( EnchantmentInfinity.class ) == null ) {
                        itemStack.afterPlacement();
                    }
                }
            }
        }

        // Don't shoot without arrow
        if ( !foundArrow && player.getGamemode() != Gamemode.CREATIVE ) {
            return;
        }

        // Get bow enchantments
        int powerModifier = 0;
        EnchantmentPower power = this.getEnchantment( EnchantmentPower.class );
        if ( power != null ) {
            powerModifier = power.getLevel();
        }

        int punchModifier = 0;
        EnchantmentPunch punch = this.getEnchantment( EnchantmentPunch.class );
        if ( punch != null ) {
            punchModifier = punch.getLevel();
        }

        int flameModifier = 0;
        EnchantmentFlame flame = this.getEnchantment( EnchantmentFlame.class );
        if ( flame != null ) {
            flameModifier = flame.getLevel();
        }

        // Create arrow
        EntityArrow arrow = new EntityArrow( player, player.getWorld(), force, powerModifier, punchModifier, flameModifier );
        ProjectileLaunchEvent event = new ProjectileLaunchEvent( arrow, ProjectileLaunchEvent.Cause.BOW_SHOT );
        player.getWorld().getServer().getPluginManager().callEvent( event );
        if ( !event.isCancelled() ) {
            // Use the bow
            this.calculateUsageAndUpdate( 1 );
            player.getWorld().spawnEntityAt( arrow, arrow.getPositionX(), arrow.getPositionY(), arrow.getPositionZ(), arrow.getYaw(), arrow.getPitch() );
        }
    }

    private float calculateForce( EntityPlayer player ) {
        long currentDraw = player.getWorld().getServer().getCurrentTickTime() - player.getActionStart();
        float force = (float) currentDraw / 1000f;
        if ( force < 0.1f ) {
            return -1f;
        }

        if ( force > 1.0f ) {
            force = 1.0f;
        }

        return force;
    }

    @Override
    public ItemType getType() {
        return ItemType.BOW;
    }

}
