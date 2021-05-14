package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * This event is fired when the server decides to disconnect a player.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerKickEvent extends SimplePlayerEvent {

    private final String message;

    /**
     * Construct a new player kick event
     *
     * @param player  The player which is going to be kicked
     * @param message The message with which the player is going to be kicked
     */
    public PlayerKickEvent(EntityPlayer player, String message) {
        super(player);
        this.message = message;
    }

    /**
     * Get the message with which this user is kicked
     *
     * @return the message with which is the user kicked
     */
    public String message() {
        return this.message;
    }

    @Override
    public String toString() {
        return "PlayerKickEvent{" +
            "player=" + this.player() +
            ", message='" + this.message + '\'' +
            '}';
    }

}
