/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity.projectile;

import io.gomint.entity.projectile.EntityProjectile;
import io.gomint.event.entity.CancellableEntityEvent;
import io.gomint.world.block.Block;

import java.util.Set;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public class ProjectileHitBlocksEvent extends CancellableEntityEvent<ProjectileHitBlocksEvent> {

    private final Set<Block> blocks;

    /**
     * Create a new event to announce a projectile entity hitting blocks
     *
     * @param blocks     which got hit by the projectile
     * @param projectile which hit the entity
     */
    public ProjectileHitBlocksEvent(Set<Block> blocks, EntityProjectile<?> projectile) {
        super(projectile);
        this.blocks = blocks;
    }

    /**
     * Get the projectile which hit the blocks
     *
     * @return projectile which hit the blocks
     */
    @Override
    public EntityProjectile<?> entity() {
        return (EntityProjectile<?>) super.entity();
    }

    /**
     * Get the blocks which got hit by the projectile
     *
     * @return blocks which got hit
     */
    public Set<Block> blocks() {
        return this.blocks;
    }

    @Override
    public String toString() {
        return "ProjectileHitBlocksEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", blocks=" + this.blocks +
            '}';
    }

}
