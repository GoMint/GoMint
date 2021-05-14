/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.interfaces;

import io.gomint.entity.Entity;
import io.gomint.world.World;

/**
 * Marks an event that takes place in relation to an entity
 *
 * @author Janmm14
 * @version 1.0
 * @stability 1
 */
public interface EntityEvent extends WorldEvent {

    /**
     * Get the entity which is affected by this event
     *
     * @return the entity which is affected by this event
     */
    Entity<?> entity();

    @Override
    default World world() {
        return entity().world();
    }

}
