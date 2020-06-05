package io.gomint.server.entity.passive;

import io.gomint.server.entity.Attribute;
import io.gomint.server.entity.EntityAgeable;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.EntityType;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.WorldAdapter;

@RegisterInfo( sId = "minecraft:mooshroom" )
public class EntityMooshroom extends EntityAgeable implements io.gomint.entity.passive.EntityMooshroom {
    /**
     * Constructs a new EntityLiving
     *
     * @param world The world in which this entity is in
     */
    public EntityMooshroom( WorldAdapter world ) {
        super( EntityType.MUSHROOM_COW, world );
        this.initEntity();
    }

    /**
     * Create new entity mooshroom for API
     */
    public EntityMooshroom() {
        super( EntityType.MUSHROOM_COW, null );
        this.initEntity();
    }

    private void initEntity() {
        this.addAttribute(Attribute.HEALTH);
        this.setMaxHealth(20);
        this.setHealth(20);
        if(this.isBaby()) {
            this.setSize(0.45f, 0.7f);
        }else{
            this.setSize(0.9f, 1.4f);
        }
    }

    @Override
    public void update( long currentTimeMS, float dT ) {
        super.update( currentTimeMS, dT );
    }
}
