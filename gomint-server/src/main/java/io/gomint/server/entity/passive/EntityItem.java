package io.gomint.server.entity.passive;

import io.gomint.GoMint;
import io.gomint.entity.passive.EntityItemDrop;
import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.event.player.PlayerPickupItemEvent;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.server.GoMintServer;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.EntityTags;
import io.gomint.server.entity.EntityType;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketAddItemEntity;
import io.gomint.server.network.packet.PacketPickupItemEntity;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.util.Values;
import io.gomint.server.world.WorldAdapter;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Gamemode;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "item" )
public class EntityItem extends Entity<EntityItemDrop> implements EntityItemDrop {

    private ItemStack<?> itemStack;
    private long pickupTime;
    private boolean isReset;

    private float lastUpdateDT;

    /**
     * Construct a new Entity
     *
     * @param itemStack The itemstack which should be dropped
     * @param world     The world in which this entity is in
     */
    public EntityItem( ItemStack<?> itemStack, WorldAdapter world ) {
        super( EntityType.ITEM_DROP, world );
        this.itemStack = itemStack;
        this.initEntity();
    }

    /**
     * Create new entity item for API
     */
    public EntityItem() {
        super( EntityType.ITEM_DROP, null );
        this.initEntity();
    }

    private void initEntity() {
        this.size( 0.25f, 0.25f );
        pickupDelay( 500, TimeUnit.MILLISECONDS );

        this.gravity = 0.04f;
        this.offsetY = 0.125f;
    }

    @Override
    public <T extends ItemStack<T>> T itemStack() {
        return (T) ( (io.gomint.server.inventory.item.ItemStack<T>) this.itemStack ).clone();
    }

    @Override
    public <T extends ItemStack<T>> EntityItem itemStack(T itemStack ) {
        if ( this.world == null ) {
            this.itemStack = itemStack.clone();
        }

        return this;
    }

    @Override
    public EntityItem pickupDelay(long duration, TimeUnit timeUnit ) {
        this.pickupTime = this.world.currentTickTime() + timeUnit.toMillis( duration );
        return this;
    }

    @Override
    public void update( long currentTimeMS, float dT ) {
        // Entity base tick (movement)
        super.update( currentTimeMS, dT );

        this.lastUpdateDT += dT;
        if ( Values.CLIENT_TICK_RATE - this.lastUpdateDT < MathUtils.EPSILON ) {
            if ( this.isCollided && !this.isReset && this.velocity().length() < 0.01f ) {
                this.velocity( Vector.ZERO ); // Reset velocity
                this.isReset = true;
            }

            if ( this.age > 6000 ) { // 5 Minutes
                this.despawn();
            }

            this.lastUpdateDT = 0;
        }
    }

    @Override
    protected EntityItem fall() {
        return this;
    }

    @Override
    public boolean damage( EntityDamageEvent damageEvent ) {
        if ( damageEvent.damageSource() == EntityDamageEvent.DamageSource.FALL ||
            damageEvent.damageSource() == EntityDamageEvent.DamageSource.FIRE ||
            damageEvent.damageSource() == EntityDamageEvent.DamageSource.ENTITY_EXPLODE ) {
            this.despawn();
            return true;
        }

        return false;
    }

    @Override
    public Packet createSpawnPacket( EntityPlayer receiver ) {
        PacketAddItemEntity packetAddItemEntity = new PacketAddItemEntity();
        packetAddItemEntity.setEntityId( this.id() );
        packetAddItemEntity.setItemStack( this.itemStack );
        packetAddItemEntity.setX( this.positionX() );
        packetAddItemEntity.setY( this.positionY() );
        packetAddItemEntity.setZ( this.positionZ() );
        packetAddItemEntity.setMotionX( this.getMotionX() );
        packetAddItemEntity.setMotionY( this.getMotionY() );
        packetAddItemEntity.setMotionZ( this.getMotionZ() );
        packetAddItemEntity.setMetadata( this.metadata() );
        return packetAddItemEntity;
    }

    @Override
    public void onCollideWithPlayer( EntityPlayer player ) {
        // Check if we can pick it up
        if ( this.world.currentTickTime() > this.pickupTime() && !this.dead() ) {
            // Check if we have place in out inventory to store this item
            if ( !player.inventory().hasPlaceFor( this.itemStack() ) ) {
                return;
            }

            // Ask the API is we can pickup
            PlayerPickupItemEvent event = new PlayerPickupItemEvent( player, this, this.itemStack() );
            if ( player.gamemode() == Gamemode.SPECTATOR ) {
                event.cancelled( true );
            }

            this.world.server().pluginManager().callEvent( event );

            if ( !event.cancelled() ) {
                // Consume the item
                PacketPickupItemEntity packet = new PacketPickupItemEntity();
                packet.setItemEntityId( this.id() );
                packet.setPlayerEntityId( player.id() );

                for ( io.gomint.entity.EntityPlayer announcePlayer : this.world.onlinePlayers() ) {
                    if ( announcePlayer instanceof EntityPlayer ) {
                        ( (EntityPlayer) announcePlayer ).connection().addToSendQueue( packet );
                    }
                }

                // Manipulate inventory
                player.inventory().addItem( event.itemStack() );
                this.despawn();
            }
        }
    }

    @Override
    public boolean isMotionSendingEnabled() {
        return true;
    }

    @Override
    public void initFromNBT( NBTTagCompound compound ) {
        super.initFromNBT( compound );


        // DumpUtil.dumpNBTCompund( compound );
    }

    @Override
    public long pickupTime() {
        return this.pickupTime;
    }

    @Override
    public String toString() {
        return "EntityItem{" +
            "itemStack=" + this.itemStack +
            ", pickupTime=" + this.pickupTime +
            ", isReset=" + this.isReset +
            ", lastUpdateDT=" + this.lastUpdateDT +
            '}';
    }

    @Override
    public Set<String> tags() {
        return EntityTags.PASSIVE;
    }

}
