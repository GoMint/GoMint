/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.event.CancellableEvent;
import io.gomint.event.interfaces.PlayerEvent;

/**
 * Represents a cancellable event with a player involved
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class CancellablePlayerEvent<E> extends CancellableEvent<E> implements PlayerEvent {

    private final EntityPlayer player;

    public CancellablePlayerEvent(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public EntityPlayer player() {
        return this.player;
    }

    @Override
    public String toString() {
        return "CancellablePlayerEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player +
            '}';
    }

}
