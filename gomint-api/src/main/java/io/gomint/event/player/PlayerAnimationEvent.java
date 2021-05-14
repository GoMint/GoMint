package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerAnimationEvent extends CancellablePlayerEvent<PlayerAnimationEvent> {

    private final Animation animation;

    public PlayerAnimationEvent(EntityPlayer player, Animation animation) {
        super(player);
        this.animation = animation;
    }

    public Animation animation() {
        return this.animation;
    }

    @Override
    public String toString() {
        return "PlayerAnimationEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", animation=" + this.animation +
            '}';
    }

    public enum Animation {

        SWING

    }

}
