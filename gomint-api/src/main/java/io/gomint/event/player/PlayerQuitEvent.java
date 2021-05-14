package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * This event is fired when a client has disconnected. Its called before entity cleanup so you can access all data loaded
 * associated with the entity or alter data in it before its persisted.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerQuitEvent extends SimplePlayerEvent {

    private String quitMessage;

    /**
     * Construct a quit event with the player who disconnected
     *
     * @param player The player which disconnected
     */
    public PlayerQuitEvent(EntityPlayer player, String quitMessage) {
        super(player);
        this.quitMessage = quitMessage;
    }

    /**
     * Set the message that will be displayed when the player leaves the server
     *
     * @param quitMessage the message to display
     */
    public PlayerQuitEvent quitMessage(String quitMessage) {
        this.quitMessage = quitMessage;
        return this;
    }

    /**
     * Get the message that will be displayed when the player leaves the server
     *
     * @return the message that will be displayed
     */
    public String quitMessage() {
        return this.quitMessage;
    }

    @Override
    public String toString() {
        return "PlayerQuitEvent{" +
            "player=" + this.player() +
            ", quitMessage='" + this.quitMessage + '\'' +
            '}';
    }

}
