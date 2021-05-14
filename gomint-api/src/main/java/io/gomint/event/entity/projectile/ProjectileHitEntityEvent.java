/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity.projectile;

import io.gomint.entity.Entity;
import io.gomint.entity.projectile.EntityProjectile;
import io.gomint.event.entity.CancellableEntityEvent;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class ProjectileHitEntityEvent extends CancellableEntityEvent<ProjectileHitEntityEvent> {

    private final EntityProjectile<?> projectile;

    /**
     * Create a new event to announce a projectile entity hitting another entity
     *
     * @param entity     for which this event is
     * @param projectile which hit the entity
     */
    public ProjectileHitEntityEvent(Entity<?> entity, EntityProjectile<?> projectile) {
        super(entity);
        this.projectile = projectile;
    }

    /**
     * Get the projectile which hit the entity
     *
     * @return projectile which hit the entity
     */
    public EntityProjectile<?> projectile() {
        return this.projectile;
    }

    @Override
    public String toString() {
        return "ProjectileHitEntityEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", projectile=" + this.projectile +
            '}';
    }

}
