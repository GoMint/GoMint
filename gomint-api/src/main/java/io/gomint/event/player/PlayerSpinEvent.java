package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerSpinEvent extends CancellablePlayerEvent<PlayerSpinEvent> {

    private final boolean newStatus;

    public PlayerSpinEvent(EntityPlayer player, boolean newStatus) {
        super(player);
        this.newStatus = newStatus;
    }

    /**
     * Get the status the client wants to set.
     *
     * @return true when the client wants to start spinning, false otherwise
     */
    public boolean newStatus() {
        return this.newStatus;
    }

    @Override
    public String toString() {
        return "PlayerSpinEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", newStatus=" + this.newStatus +
            '}';
    }

}
