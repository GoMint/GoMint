package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * This event gets fired after the inital login stage has been completed and the player is ready to be added to the world
 * to be sent to other players (become visible). If you cancel this event the player will never be spawned but it has loaded
 * world chunks and got all resource pack data.
 * <br><br>
 * This event is <b>not</b> called in player's world thread and is called before {@linkplain PlayerWorldJoinEvent}.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerJoinEvent extends CancellablePlayerEvent<PlayerJoinEvent> {

    private String kickReason;
    private String joinMessage;

    public PlayerJoinEvent(EntityPlayer player, String joinMessage) {
        super(player);
        this.joinMessage = joinMessage;
    }

    /**
     * Set the message that will be displayed when the player joins the server
     *
     * @param joinMessage the message to display
     */
    public PlayerJoinEvent joinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
        return this;
    }

    /**
     * Get the message that will be displayed when the player joins the server
     *
     * @return the message that will be displayed
     */
    public String joinMessage() {
        return this.joinMessage;
    }

    /**
     * Set the reason which will be used to disconnect the player when this event has been cancelled
     *
     * @param kickReason which is used to kick the player
     */
    public PlayerJoinEvent kickReason(String kickReason) {
        this.kickReason = kickReason;
        return this;
    }

    /**
     * Get the reason which the player will get when this event has been cancelled
     *
     * @return reason for kick
     */
    public String kickReason() {
        return this.kickReason;
    }

    @Override
    public String toString() {
        return "PlayerJoinEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", kickReason='" + this.kickReason + '\'' +
            ", joinMessage='" + this.joinMessage + '\'' +
            '}';
    }

}
