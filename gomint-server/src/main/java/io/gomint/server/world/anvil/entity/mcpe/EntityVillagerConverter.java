package io.gomint.server.world.anvil.entity.mcpe;

import io.gomint.server.entity.metadata.MetadataContainer;
import io.gomint.server.entity.passive.EntityVillager;
import io.gomint.server.world.WorldAdapter;
import io.gomint.taglib.NBTTagCompound;
import lombok.RequiredArgsConstructor;

/**
 * @author geNAZt
 * @version 1.0
 */
@RequiredArgsConstructor
public class EntityVillagerConverter extends BaseConverter<EntityVillager> {

    private final WorldAdapter world;

    @Override
    public EntityVillager create() {
        return this.world.getServer().createEntity( EntityVillager.class );
    }

    @Override
    public EntityVillager readFrom( NBTTagCompound compound ) {
        EntityVillager entityVillager = super.readFrom( compound );

        // Set profession
        int profession = compound.getInteger( "Profession", 0 );
        if ( profession < 0 || profession > 4 ) {
            profession = 0;
        }

        entityVillager.getMetadata().putInt( MetadataContainer.DATA_VARIANT, profession );

        return entityVillager;
    }

}
