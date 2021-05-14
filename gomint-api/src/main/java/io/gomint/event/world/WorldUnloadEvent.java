/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.event.world;

import io.gomint.world.World;

/**
 * Event is called when a world is about to be unloaded.
 * 
 * Event is called in world's thread.
 * 
 * @author Janmm14
 * @version 1.0
 * @stability 2
 */
public class WorldUnloadEvent extends SimpleWorldEvent {

    /**
     * Create an event for announcing world loading
     *
     * @param world for which this event is
     */
    public WorldUnloadEvent(World world) {
        super(world);
    }

    @Override
    public String toString() {
        return "WorldUnloadEvent{" +
            "world=" + this.world() +
            '}';
    }

}
