package io.gomint.event.world;

import io.gomint.event.Event;
import io.gomint.event.interfaces.WorldEvent;
import io.gomint.world.World;

/**
 * Represents a not cancellable event with a world mainly involved
 *
 * @author geNAZt
 * @version 1.0
 * @stability 2
 */
public class SimpleWorldEvent extends Event implements WorldEvent {

    private final World world;

    /**
     * Create a new world based event
     *
     * @param world for which this event is
     */

    public SimpleWorldEvent(World world) {
        this.world = world;
    }

    @Override
    public World world() {
        return this.world;
    }

    @Override
    public String toString() {
        return "SimpleWorldEvent{" +
            "world=" + this.world +
            '}';
    }

}
