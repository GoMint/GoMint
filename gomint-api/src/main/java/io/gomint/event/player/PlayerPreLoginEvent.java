package io.gomint.event.player;

import io.gomint.event.CancellableEvent;

import java.net.InetSocketAddress;

/**
 * This event is fired when RakNet (the networking) decided to accept a connection. This is before any MC:PE packets
 * will be handled (event before login). This can be used to create IP Bans / Proxy detections. Please notice: Since this
 * is a network level operation you can't provide a reason which is printed in the client. If this is needed please use
 * the {@link PlayerLoginEvent}.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerPreLoginEvent extends CancellableEvent<PlayerPreLoginEvent> {

    private final InetSocketAddress clientAddress;

    /**
     * Construct a new player pre login event. This is fired before the login payload is going to be verified.
     *
     * @param clientAddress The address of the client which wants to login
     */
    public PlayerPreLoginEvent(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }

    /**
     * Get the clients internet address
     *
     * @return the internet address of the client
     */
    public InetSocketAddress clientAddress() {
        return this.clientAddress;
    }

    @Override
    public String toString() {
        return "PlayerPreLoginEvent{" +
            "cancelled=" + this.cancelled() +
            ", clientAddress=" + this.clientAddress +
            '}';
    }

}
