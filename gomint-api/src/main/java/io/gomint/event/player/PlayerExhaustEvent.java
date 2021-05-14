/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerExhaustEvent extends CancellablePlayerEvent<PlayerExhaustEvent> {

    private final Cause cause;
    private float additionalAmount;

    /**
     * Create a new event
     *
     * @param player           for which this event is for
     * @param additionalAmount which gets added to the exhaust amount when this event is not cancelled
     * @param cause            of this exhaustion
     */
    public PlayerExhaustEvent(EntityPlayer player, float additionalAmount, Cause cause) {
        super(player);
        this.additionalAmount = additionalAmount;
        this.cause = cause;
    }

    /**
     * Get the amount of value which gets added to the exhaustion level
     *
     * @return additional exhaustion level
     */
    public float additionalAmount() {
        return this.additionalAmount;
    }

    /**
     * Set the amount of additional exhaustion
     *
     * @param additionalAmount which should be added to the exhaustion
     */
    public PlayerExhaustEvent additionalAmount(float additionalAmount) {
        this.additionalAmount = additionalAmount;
        return this;
    }

    /**
     * Get the cause of this exhausting
     *
     * @return cause of exhausting
     */
    public Cause cause() {
        return this.cause;
    }

    @Override
    public String toString() {
        return "PlayerExhaustEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", cause=" + this.cause +
            ", additionalAmount=" + this.additionalAmount +
            '}';
    }

    public enum Cause {
        /**
         * This exhaustion is 0.01 * distance travelled and is called when a player walks
         */
        WALKING,

        /**
         * This exhaustion is 0.1 * distance travelled and is called when a player sprints
         */
        SPRINTING,

        /**
         * This exhaustion is 0.025 for each block a player has broken
         */
        MINING,

        /**
         * This exhaustion is 0.3 for each hit this player did
         */
        ATTACK,

        /**
         * This exhaustion is 0.2 per jump
         */
        JUMP,

        /**
         * This exhaustion is 0.8 per jump whilst sprinting
         */
        SPRINT_JUMP,

        /**
         * This exhaustion is 3.0 per health regenerated
         */
        REGENERATION,
    }

}
