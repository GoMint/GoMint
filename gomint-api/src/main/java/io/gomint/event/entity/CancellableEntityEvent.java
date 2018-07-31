/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.entity;

import io.gomint.entity.Entity;
import io.gomint.event.CancellableEvent;
import lombok.ToString;

/**
 * @author geNAZt
 * @version 1.0
 */
@ToString( callSuper = true )
public class CancellableEntityEvent extends CancellableEvent {

    private final Entity entity;

    /**
     * Create a new entity based cancellable event
     *
     * @param entity for which this event is
     */
    public CancellableEntityEvent( Entity entity ) {
        this.entity = entity;
    }

    /**
     * Get the player which is affected by this event
     *
     * @return the player which is affected by this event
     */
    public Entity getEntity() {
        return this.entity;
    }

}
