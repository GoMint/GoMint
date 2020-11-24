/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.entity.projectile;

import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.EntityType;
import io.gomint.server.world.WorldAdapter;

/**
 * @author geNAZt
 * @version 1.0
 */
public abstract class EntityThrowable extends EntityProjectile {

    /**
     * Construct a new Entity
     *
     * @param shooter of this entity
     * @param type    The type of the Entity
     * @param world   The world in which this entity is in
     */
    protected EntityThrowable(EntityLiving shooter, EntityType type, WorldAdapter world) {
        super(shooter, type, world);

        // Set owning entity
        if (shooter != null) {
            this.metadataContainer.putLong(5, shooter.getEntityId());
        }
    }

    @Override
    protected void applyCustomProperties() {
        super.applyCustomProperties();

        // Gravity
        GRAVITY = 0.03f;
        DRAG = 0.01f;

        // Set size
        this.setSize(0.25f, 0.25f);
    }

    @Override
    public boolean isCritical() {
        return false;
    }

    @Override
    public float getDamage() {
        return 0;
    }

}
