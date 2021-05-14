package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * @author LucGamesHD
 * @version 1.0
 * @stability 3
 */
public class PlayerToggleSprintEvent extends CancellablePlayerEvent<PlayerToggleSprintEvent> {

    private final boolean newStatus;

    public PlayerToggleSprintEvent(EntityPlayer player, boolean newStatus) {
        super(player);
        this.newStatus = newStatus;
    }

    /**
     * Get the status the client wants to set.
     *
     * @return true when the client wants to start sprinting, false otherwise
     */
    public boolean newStatus() {
        return this.newStatus;
    }

    @Override
    public String toString() {
        return "PlayerToggleSprintEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", newStatus=" + this.newStatus +
            '}';
    }

}
