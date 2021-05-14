package io.gomint.event.network;

import io.gomint.event.Event;

/**
 * This event gets fired when a unconnected Client pings the server. This can happen very often so care about putting
 * heavy calculation on this event since it may slow down other connections by a huge amount.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PingEvent extends Event {

    private String motd;
    private int onlinePlayers;
    private int maxPlayers;

    public PingEvent(String motd, int onlinePlayers, int maxPlayers) {
        this.motd = motd;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Get the MOTD of this server
     *
     * @return MOTD of this server
     */
    public String motd() {
        return this.motd;
    }

    /**
     * Set a new MOTD for this ping
     *
     * @param motd to set
     */
    public PingEvent motd(String motd) {
        this.motd = motd;
        return this;
    }

    /**
     * Get the amount of users which should be shown
     *
     * @return amount of users
     */
    public int onlinePlayers() {
        return this.onlinePlayers;
    }

    /**
     * Set the amount of users which should be shown
     *
     * @param onlinePlayers amount which should be shown
     */
    public PingEvent onlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
        return this;
    }

    public int maxPlayers() {
        return this.maxPlayers;
    }

    public PingEvent maxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    @Override
    public String toString() {
        return "PingEvent{" +
            "motd='" + this.motd + '\'' +
            ", onlinePlayers=" + this.onlinePlayers +
            ", maxPlayers=" + this.maxPlayers +
            '}';
    }

}
