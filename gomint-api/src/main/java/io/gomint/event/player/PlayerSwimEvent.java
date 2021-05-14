package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * @author Luke (@lukeeey)
 * @version 1.0
 * @stability 3
 */
public class PlayerSwimEvent extends CancellablePlayerEvent<PlayerSwimEvent> {

    private final boolean newStatus;

    public PlayerSwimEvent(EntityPlayer player, boolean newStatus) {
        super(player);
        this.newStatus = newStatus;
    }

    /**
     * Get the status the client wants to set.
     *
     * @return true when the client wants to start swimming, false otherwise
     */
    public boolean newStatus() {
        return this.newStatus;
    }

    @Override
    public String toString() {
        return "PlayerSwimEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", newStatus=" + this.newStatus +
            '}';
    }

}
