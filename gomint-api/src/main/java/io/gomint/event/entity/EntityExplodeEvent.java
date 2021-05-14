/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity;

import io.gomint.entity.Entity;
import io.gomint.world.block.Block;

import java.util.Set;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class EntityExplodeEvent extends CancellableEntityEvent<EntityExplodeEvent> {

    private final Set<Block> affectedBlocks;
    private float randomDropChance;

    /**
     * Create a new event for announcing an entity is exploding
     *
     * @param entity           for which this event is
     * @param affectedBlocks   which will be destroyed during this explosion
     * @param randomDropChance which will be used to determinate if a affected block will drop
     */
    public EntityExplodeEvent(Entity<?> entity, Set<Block> affectedBlocks, float randomDropChance) {
        super(entity);
        this.affectedBlocks = affectedBlocks;
        this.randomDropChance = randomDropChance;
    }

    /**
     * Get affected blocks
     *
     * @return affected blocks
     */
    public Set<Block> affectedBlocks() {
        return this.affectedBlocks;
    }

    /**
     * Get the random drop chance at which the affected blocks will be dropped
     *
     * @return random drop chance
     */
    public float randomDropChance() {
        return this.randomDropChance;
    }

    /**
     * Set new random drop chance
     *
     * @param randomDropChance which will be used instead of the old value
     */
    public EntityExplodeEvent randomDropChance(float randomDropChance) {
        this.randomDropChance = randomDropChance;
        return this;
    }

    @Override
    public String toString() {
        return "EntityExplodeEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", affectedBlocks=" + this.affectedBlocks +
            ", randomDropChance=" + this.randomDropChance +
            '}';
    }

}
