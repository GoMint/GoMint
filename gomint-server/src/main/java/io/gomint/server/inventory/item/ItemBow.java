package io.gomint.server.inventory.item;

import io.gomint.enchant.EnchantmentFlame;
import io.gomint.enchant.EnchantmentInfinity;
import io.gomint.enchant.EnchantmentPower;
import io.gomint.enchant.EnchantmentPunch;
import io.gomint.event.entity.projectile.ProjectileLaunchEvent;
import io.gomint.inventory.item.ItemType;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.projectile.EntityArrow;
import io.gomint.server.inventory.item.annotation.CanBeDamaged;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.Gamemode;
import io.gomint.world.block.Block;
import io.gomint.world.block.data.Facing;

import java.time.Duration;

/**
 * @author geNAZt
 * @version 1.0
 */
@CanBeDamaged
@RegisterInfo( sId = "minecraft:bow" )
public class ItemBow extends ItemStack< io.gomint.inventory.item.ItemBow> implements io.gomint.inventory.item.ItemBow {

    @Override
    public Duration burnTime() {
        return Duration.ofMillis(10000);
    }

    @Override
    public byte maximumAmount() {
        return 1;
    }

    @Override
    public short maxDamage() {
        return 360;
    }

    @Override
    public boolean interact(EntityPlayer entity, Facing face, Vector clickPosition, Block clickedBlock) {
        entity.setUsingItem(true);
        return super.interact(entity, face, clickPosition, clickedBlock);
    }

    /**
     * Shoot this bow
     *
     * @param player which will shoot this bow
     */
    public void shoot( EntityPlayer player ) {
        // Check if player did start
        if ( player.actionStart() == -1 ) {
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
        for (int i = 0; i < player.inventory().size(); i++ ) {
            ItemStack<?> itemStack = (ItemStack<?>) player.inventory().item( i );
            if ( itemStack instanceof ItemArrow ) {
                foundArrow = true;

                if ( this.enchantment( EnchantmentInfinity.class ) == null ) {
                    itemStack.afterPlacement();
                }
            }
        }

        // Check offhand if not found
        if ( !foundArrow ) {
            for ( int i = 0; i < player.getOffhandInventory().size(); i++ ) {
                ItemStack<?> itemStack = (ItemStack<?>) player.inventory().item( i );
                if ( itemStack instanceof ItemArrow ) {
                    foundArrow = true;

                    if ( this.enchantment( EnchantmentInfinity.class ) == null ) {
                        itemStack.afterPlacement();
                    }
                }
            }
        }

        // Don't shoot without arrow
        if ( !foundArrow && player.gamemode() != Gamemode.CREATIVE ) {
            return;
        }

        // Get bow enchantments
        int powerModifier = 0;
        EnchantmentPower power = this.enchantment( EnchantmentPower.class );
        if ( power != null ) {
            powerModifier = power.level();
        }

        int punchModifier = 0;
        EnchantmentPunch punch = this.enchantment( EnchantmentPunch.class );
        if ( punch != null ) {
            punchModifier = punch.level();
        }

        int flameModifier = 0;
        EnchantmentFlame flame = this.enchantment( EnchantmentFlame.class );
        if ( flame != null ) {
            flameModifier = flame.level();
        }

        // Create arrow
        EntityArrow arrow = new EntityArrow( player, player.world(), force, powerModifier, punchModifier, flameModifier );
        ProjectileLaunchEvent event = new ProjectileLaunchEvent( arrow, ProjectileLaunchEvent.Cause.BOW_SHOT );
        player.world().server().pluginManager().callEvent( event );
        if ( !event.cancelled() ) {
            // Use the bow
            this.calculateUsageAndUpdate( 1 );
            player.world().spawnEntityAt( arrow, arrow.positionX(), arrow.positionY(), arrow.positionZ(), arrow.yaw(), arrow.pitch() );
        }
    }

    private float calculateForce( EntityPlayer player ) {
        long currentDraw = player.world().currentTickTime() - player.actionStart();
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
    public ItemType itemType() {
        return ItemType.BOW;
    }

    @Override
    public int enchantAbility() {
        return 1;
    }

}
