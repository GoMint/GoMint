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
public class EntityHealEvent extends CancellableEntityEvent<EntityHealEvent> {

    private final Cause cause;
    private double healAmount;

    /**
     * Create a new event for announcing an entity is healing
     *
     * @param entity     for which this event is
     * @param healAmount for which the entity should be healed
     * @param cause      of this heal
     */
    public EntityHealEvent(Entity<?> entity, double healAmount, Cause cause) {
        super(entity);
        this.cause = cause;
        this.healAmount = healAmount;
    }

    /**
     * Get the cause of this healing
     *
     * @return cause of this healing
     */
    public Cause cause() {
        return this.cause;
    }

    /**
     * Get the amount of health this entity would get due to this event
     *
     * @return amount of heal
     */
    public double healAmount() {
        return this.healAmount;
    }

    /**
     * Set the amount of health this entity gets due to this event
     *
     * @param healAmount which should be added to the entity due to this event
     */
    public EntityHealEvent healAmount(double healAmount) {
        this.healAmount = healAmount;
        return this;
    }

    @Override
    public String toString() {
        return "EntityHealEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", cause=" + this.cause +
            ", healAmount=" + this.healAmount +
            '}';
    }

    public enum Cause {

        /**
         * Heal based on hunger
         */
        SATURATION,

        /**
         * Heal based on regeneration effect
         */
        REGENERATION_EFFECT,

        /**
         * Heal based on instant healing effect
         */
        HEALING_EFFECT,

    }

}
