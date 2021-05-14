/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity;

import io.gomint.entity.Entity;
import io.gomint.event.Event;
import io.gomint.event.interfaces.EntityEvent;

/**
 * Represents a not cancellable event with an entity involved
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class SimpleEntityEvent extends Event implements EntityEvent {

    private final Entity<?> entity;

    /**
     * Create a new entity based event
     *
     * @param entity for which this event is
     */
    public SimpleEntityEvent(Entity<?> entity) {
        this.entity = entity;
    }

    @Override
    public Entity<?> entity() {
        return this.entity;
    }

    @Override
    public String toString() {
        return "SimpleEntityEvent{" +
            "entity=" + this.entity +
            '}';
    }

}
