package io.gomint.event.entity;

import io.gomint.entity.Entity;
import io.gomint.math.Location;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class EntityTeleportEvent extends CancellableEntityEvent<EntityTeleportEvent> {

    private final Location from;
    private final Location to;

    private final Cause cause;

    /**
     * Create a new event for announcing an entity teleportation
     *
     * @param entity for which this event is
     * @param from   which location the entity will be teleported
     * @param to     which location this entity will be transfered
     * @param cause  of the teleport
     */
    public EntityTeleportEvent(Entity<?> entity, Location from, Location to, Cause cause) {
        super(entity);

        this.from = from;
        this.to = to;
        this.cause = cause;
    }

    /**
     * Get the location from where the entity will be teleported
     *
     * @return the from location
     */
    public Location from() {
        return this.from;
    }

    /**
     * Get the location to which the entity should be teleported
     *
     * @return the to location
     */
    public Location to() {
        return this.to;
    }

    /**
     * Get the cause of this event
     *
     * @return the cause why this teleport happens
     */
    public Cause cause() {
        return this.cause;
    }

    @Override
    public String toString() {
        return "EntityTeleportEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", from=" + this.from +
            ", to=" + this.to +
            ", cause=" + this.cause +
            '}';
    }

    public enum Cause {

        /**
         * A player has thrown a enderpearl and it landed
         */
        ENDERPEARL,

        /**
         * A plugin issued a teleport
         */
        CUSTOM

    }

}
