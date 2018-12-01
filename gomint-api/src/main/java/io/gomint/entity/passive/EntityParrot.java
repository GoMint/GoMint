package io.gomint.entity.passive;

import io.gomint.GoMint;
import io.gomint.entity.EntityLiving;

public interface EntityParrot extends EntityLiving {

    /**
     * Create new entity parrot with no config
     *
     * @return empty, fresh parrot
     */
    static EntityParrot create() {
        return GoMint.instance().createEntity( EntityParrot.class );
    }

    /**
     * Set this parrot dancing
     *
     * @param value true if this parrot should be dancing, false if not
     */
    void setDancing( boolean value );

    /**
     * Is the parrot dancing?
     *
     * @return true if this parrot is dancing, false if not
     */
    boolean isDancing();

}
