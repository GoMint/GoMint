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
public class EntityDamageByEntityEvent extends EntityDamageEvent {

    private final Entity<?> attacker;

    /**
     * Create a new event for announcing an entity taking damage from another entity
     *
     * @param entity       for which this event is
     * @param attacker     which attacked this entity
     * @param damageSource with which the entity should be attacked
     * @param damage       which should be dealt
     */
    public EntityDamageByEntityEvent(Entity<?> entity, Entity<?> attacker, DamageSource damageSource, float damage) {
        super(entity, damageSource, damage);
        this.attacker = attacker;
    }

    /**
     * Get the entity which attacked
     *
     * @return attacking entity
     */
    public Entity<?> attacker() {
        return this.attacker;
    }

    @Override
    public String toString() {
        return "EntityDamageByEntityEvent{" +
            "cancelled=" + this.cancelled() +
            ", entity=" + this.entity() +
            ", damageSource=" + this.damageSource() +
            ", damage=" + this.damage() +
            ", finalDamage=" + this.finalDamage() +
            ", attacker=" + this.attacker +
            '}';
    }

}
