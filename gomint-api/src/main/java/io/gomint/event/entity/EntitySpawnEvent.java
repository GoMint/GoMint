/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity;

import io.gomint.entity.Entity;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class EntitySpawnEvent extends CancellableEntityEvent<EntitySpawnEvent> {

    /**
     * Create a new event for announcing an entity spawn
     *
     * @param entity entity which is about to spawn
     */
    public EntitySpawnEvent(Entity<?> entity) {
        super(entity);
    }

    @Override
    public String toString() {
        return "EntitySpawnEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            '}';
    }

}
