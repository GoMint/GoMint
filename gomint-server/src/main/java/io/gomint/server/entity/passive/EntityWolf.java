package io.gomint.server.entity.passive;

import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.server.entity.Attribute;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.EntityType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.WorldAdapter;

@RegisterInfo( sId = "minecraft:wolf" )
public class EntityWolf extends EntityLiving implements io.gomint.entity.passive.EntityWolf {

    /**
     * Constructs a new EntityLiving
     *
     * @param world The world in which this entity is in
     */
    public EntityWolf( WorldAdapter world ) {
        super( EntityType.WOLF, world );
        this.initEntity();
    }

    /**
     * Create new entity wolf for API
     */
    public EntityWolf() {
        super( EntityType.WOLF, null );
        this.initEntity();
    }

    private void initEntity() {
        this.setSize( 0.6f, 0.85f );
        this.addAttribute( Attribute.HEALTH );
        this.setMaxHealth( 16 );
        this.setHealth( 16 );
    }

    @Override
    public void onCollideWithPlayer( EntityPlayer player ) {
        super.onCollideWithPlayer( player );

        switch( this.getWorld().getDifficulty() ) {
            case EASY:
                player.attack( 3, EntityDamageEvent.DamageSource.ENTITY_ATTACK );
                break;
            case NORMAL:
                player.attack( 4, EntityDamageEvent.DamageSource.ENTITY_ATTACK );
                break;
            case HARD:
                player.attack( 6, EntityDamageEvent.DamageSource.ENTITY_ATTACK );
                break;
        }
    }

    @Override
    public void update( long currentTimeMS, float dT ) {
        super.update( currentTimeMS, dT );
    }
}
