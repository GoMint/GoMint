package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;

import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerChatEvent extends CancellablePlayerEvent<PlayerChatEvent> {

    private String text;
    private final List<EntityPlayer> recipients;
    private String sender;

    /**
     * Create a new chat event
     *
     * @param player which chatted
     * @param text   which has been sent
     */
    public PlayerChatEvent(EntityPlayer player, String sender, String text, List<EntityPlayer> recipients) {
        super(player);
        this.text = text;
        this.recipients = recipients;
        this.sender = sender;
    }

    /**
     * Get the text which has been sent
     *
     * @return text of this chat event
     */
    public String text() {
        return this.text;
    }

    /**
     * Set the text of this chat event
     *
     * @param text to display as a result of this chat event
     */
    public PlayerChatEvent text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get a list of players which will see this chat
     *
     * @return list of players which will see this
     */
    public List<EntityPlayer> recipients() {
        return this.recipients;
    }

    /**
     * Name of the sender which will be passed to the client for display
     *
     * @return sender of the message
     */
    public String sender() {
        return this.sender;
    }

    /**
     * Set the name of the sender
     *
     * @param sender which will be used to display in the client
     */
    public PlayerChatEvent sender(String sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public String toString() {
        return "PlayerChatEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", text='" + this.text + '\'' +
            ", recipients=" + this.recipients +
            ", sender='" + this.sender + '\'' +
            '}';
    }

}
