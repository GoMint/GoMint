/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity.projectile;

import io.gomint.entity.projectile.EntityProjectile;
import io.gomint.event.entity.CancellableEntityEvent;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class ProjectileLaunchEvent extends CancellableEntityEvent<ProjectileLaunchEvent> {

    private final Cause cause;

    /**
     * Create a new event to announce a projectile entity is launching
     *
     * @param entity for which this event is
     * @param cause  why this projectile will get launched
     */
    public ProjectileLaunchEvent(EntityProjectile<?> entity, Cause cause) {
        super(entity);
        this.cause = cause;
    }

    /**
     * Get the cause why this projectile will get launched
     *
     * @return cause of launching
     */
    public Cause cause() {
        return this.cause;
    }

    @Override
    public EntityProjectile<?> entity() {
        return (EntityProjectile<?>) super.entity();
    }

    @Override
    public String toString() {
        return "ProjectileLaunchEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", cause=" + this.cause +
            '}';
    }

    public enum Cause {

        /**
         * Entity shot bow
         */
        BOW_SHOT,

        /**
         * Throwing a exp bottle
         */
        THROWING_EXP_BOTTLE,

        /**
         * When someone throws the fishing rod
         */
        FISHING_ROD,

        /**
         * Throwing ender pearls
         */
        THROWING_ENDER_PEARL,

        /**
         * Throwing snowballs
         */
        THROWING_SNOWBALL

    }

}
