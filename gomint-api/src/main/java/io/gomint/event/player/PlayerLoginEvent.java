package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

/**
 * This event is fired when the login stage begins, way before any data will be sent to the client. If you cancel
 * this event to kick someone the player will have no impact on performance, chunk loading, etc.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerLoginEvent extends CancellablePlayerEvent<PlayerLoginEvent> {

    private String kickMessage;

    public PlayerLoginEvent(EntityPlayer player) {
        super(player);
    }

    /**
     * If this event is cancelled you have the chance to set a custom kick
     * message here.
     *
     * @return custom kick message
     */
    public String kickMessage() {
        return this.kickMessage;
    }

    /**
     * Set the custom kick message. This is only used when the event is cancelled and
     * the player is going to be kicked
     *
     * @param kickMessage The custom kick message
     */
    public PlayerLoginEvent kickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
        return this;
    }

    @Override
    public String toString() {
        return "PlayerLoginEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", kickMessage='" + this.kickMessage + '\'' +
            '}';
    }

}
