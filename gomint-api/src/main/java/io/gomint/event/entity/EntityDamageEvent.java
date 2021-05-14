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
public class EntityDamageEvent extends CancellableEntityEvent<EntityDamageEvent> {

    private final DamageSource damageSource;
    private float damage;
    private float finalDamage;

    /**
     * Create a new event for announcing an entity taking damage
     *
     * @param entity       for which this event is
     * @param damageSource where the damage comes from
     * @param damage       which should be dealt
     */
    public EntityDamageEvent(Entity<?> entity, DamageSource damageSource, float damage) {
        super(entity);
        this.damageSource = damageSource;
        this.damage = damage;
    }

    /**
     * Set the final damage which should be applied to the entity.
     *
     * @param damage which should be dealt
     */
    public EntityDamageEvent finalDamage(float damage) {
        this.finalDamage = damage;
        return this;
    }

    /**
     * Get the amount of damage which should be applied to the entity
     *
     * @return damage which should be dealt
     */
    public float finalDamage() {
        return this.finalDamage;
    }

    /**
     * Set the input damage to this event. When the final damage has not been modified this value will be used to
     * calculate the final damage being dealt. There is no way to get the new final damage before it is applied to the
     * entity.
     *
     * @param damage which should be used to calculate the final damage
     */
    public EntityDamageEvent damage(float damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Get the damage which has been input into the calculation. This value represents the damage before any
     * reduction.
     *
     * @return damage which is used in the final calculation
     */
    public float damage() {
        return this.damage;
    }

    /**
     * Get the source of this damage
     *
     * @return source of damage
     */
    public DamageSource damageSource() {
        return this.damageSource;
    }

    @Override
    public String toString() {
        return "EntityDamageEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", damageSource=" + this.damageSource +
            ", damage=" + this.damage +
            ", finalDamage=" + this.finalDamage +
            '}';
    }

    public enum DamageSource {

        /**
         * A entity decided to attack
         */
        ENTITY_ATTACK,

        /**
         * Fall damage when falling more than 3 blocks
         */
        FALL,

        /**
         * Damage dealt by the world when you fall under y -64
         */
        VOID,

        /**
         * Hit by a projectile
         */
        PROJECTILE,

        /**
         * When under liquid and no air left
         */
        DROWNING,

        /**
         * When cuddling with a cactus
         */
        CACTUS,

        /**
         * Trying to swim in lava
         */
        LAVA,

        /**
         * On fire?
         */
        ON_FIRE,

        /**
         * Standing in fire
         */
        FIRE,

        /**
         * Damage which will be dealt when a entity explodes
         */
        ENTITY_EXPLODE,

        /**
         * Damage from harm effects
         */
        HARM_EFFECT,

        /**
         * Damage due to hunger
         */
        STARVE,

        /**
         * Damage due to setHealth
         */
        API,

        /**
         * Damage due to kill command
         */
        COMMAND,

    }

}
