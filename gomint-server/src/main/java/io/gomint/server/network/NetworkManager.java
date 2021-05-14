/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.network;

import io.gomint.event.network.PingEvent;
import io.gomint.event.player.PlayerPreLoginEvent;
import io.gomint.jraknet.Connection;
import io.gomint.jraknet.EventLoops;
import io.gomint.jraknet.PacketBuffer;
import io.gomint.jraknet.ServerSocket;
import io.gomint.jraknet.SocketEvent;
import io.gomint.server.GoMintServer;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.maintenance.ReportUploader;
import io.gomint.server.network.handler.PacketAdventureSettingsHandler;
import io.gomint.server.network.handler.PacketAnimateHandler;
import io.gomint.server.network.handler.PacketBlockPickRequestHandler;
import io.gomint.server.network.handler.PacketBookEditHandler;
import io.gomint.server.network.handler.PacketBossBarHandler;
import io.gomint.server.network.handler.PacketClientCacheBlobStatusHandler;
import io.gomint.server.network.handler.PacketClientCacheStatusHandler;
import io.gomint.server.network.handler.PacketCommandRequestHandler;
import io.gomint.server.network.handler.PacketContainerCloseHandler;
import io.gomint.server.network.handler.PacketEmoteListHandler;
import io.gomint.server.network.handler.PacketEncryptionResponseHandler;
import io.gomint.server.network.handler.PacketEntityEventHandler;
import io.gomint.server.network.handler.PacketEntityFallHandler;
import io.gomint.server.network.handler.PacketHandler;
import io.gomint.server.network.handler.PacketHotbarHandler;
import io.gomint.server.network.handler.PacketInteractHandler;
import io.gomint.server.network.handler.PacketInventoryTransactionHandler;
import io.gomint.server.network.handler.PacketItemStackRequestHandler;
import io.gomint.server.network.handler.PacketLoginHandler;
import io.gomint.server.network.handler.PacketMobArmorEquipmentHandler;
import io.gomint.server.network.handler.PacketMobEquipmentHandler;
import io.gomint.server.network.handler.PacketModalResponseHandler;
import io.gomint.server.network.handler.PacketMovePlayerHandler;
import io.gomint.server.network.handler.PacketPlayerActionHandler;
import io.gomint.server.network.handler.PacketRequestChunkRadiusHandler;
import io.gomint.server.network.handler.PacketResourcePackResponseHandler;
import io.gomint.server.network.handler.PacketRespawnPositionHandler;
import io.gomint.server.network.handler.PacketServerSettingsRequestHandler;
import io.gomint.server.network.handler.PacketSetLocalPlayerAsInitializedHandler;
import io.gomint.server.network.handler.PacketTextHandler;
import io.gomint.server.network.handler.PacketTickSyncHandler;
import io.gomint.server.network.handler.PacketTileEntityDataHandler;
import io.gomint.server.network.handler.PacketViolationWarningHandler;
import io.gomint.server.network.handler.PacketWorldSoundEventHandler;
import io.gomint.server.network.packet.Packet;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.GlobalEventExecutor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author BlackyPaw
 * @author geNAZt
 * @author Janmm14
 * @version 1.0
 */
public class NetworkManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkManager.class);
    private final GoMintServer server;

    private final PacketHandler<? extends Packet>[] packetHandlers = new PacketHandler[256];

    // Connections which were closed and should be removed during next tick:
    private final Queue<Long> closedConnections = new ConcurrentLinkedQueue<>();
    private ServerSocket socket;
    private Long2ObjectMap<PlayerConnection> playersByGuid = new Long2ObjectOpenHashMap<>();

    private Long2ObjectMap<PlayerConnection> tickingPlayers = new Long2ObjectOpenHashMap<>();
    private final List<Long> untickQueue = new ArrayList<>();

    // Incoming connections to be added to the player map during next tick:
    private Queue<PlayerConnection> incomingConnections = new ConcurrentLinkedQueue<>();

    // Packet Dumping
    private boolean dump;
    private File dumpDirectory;

    // Motd
    private String motd;

    // Post process service
    private PostProcessExecutorService postProcessService;

    /**
     * Init a new NetworkManager for accepting new connections and reading incoming data
     *
     * @param server server instance which should be used
     */
    public NetworkManager(GoMintServer server) {
        this.server = server;
        this.postProcessService = new PostProcessExecutorService(server.asyncScheduler());
        this.initPacketHandlers();
    }

    public PostProcessExecutorService postProcessService() {
        return this.postProcessService;
    }

    public void motd(String motd) {
        this.motd = motd;
    }

    public String motd() {
        return this.motd;
    }

    private void initPacketHandlers() {
        // Register all packet handlers we need
        this.packetHandlers[Protocol.PACKET_MOVE_PLAYER & 0xff] = new PacketMovePlayerHandler();
        this.packetHandlers[Protocol.PACKET_REQUEST_CHUNK_RADIUS & 0xff] = new PacketRequestChunkRadiusHandler();
        this.packetHandlers[Protocol.PACKET_PLAYER_ACTION & 0xff] = new PacketPlayerActionHandler();
        this.packetHandlers[Protocol.PACKET_MOB_ARMOR_EQUIPMENT & 0xff] = new PacketMobArmorEquipmentHandler();
        this.packetHandlers[Protocol.PACKET_ADVENTURE_SETTINGS & 0xff] = new PacketAdventureSettingsHandler();
        this.packetHandlers[Protocol.PACKET_RESOURCEPACK_RESPONSE & 0xff] = new PacketResourcePackResponseHandler();
        this.packetHandlers[Protocol.PACKET_LOGIN & 0xff] = new PacketLoginHandler();
        this.packetHandlers[Protocol.PACKET_MOB_EQUIPMENT & 0xff] = new PacketMobEquipmentHandler();
        this.packetHandlers[Protocol.PACKET_INTERACT & 0xff] = new PacketInteractHandler();
        this.packetHandlers[Protocol.PACKET_BLOCK_PICK_REQUEST & 0xff] = new PacketBlockPickRequestHandler();
        this.packetHandlers[Protocol.PACKET_ENCRYPTION_RESPONSE & 0xff] = new PacketEncryptionResponseHandler();
        this.packetHandlers[Protocol.PACKET_INVENTORY_TRANSACTION & 0xff] = new PacketInventoryTransactionHandler();
        this.packetHandlers[Protocol.PACKET_CONTAINER_CLOSE & 0xff] = new PacketContainerCloseHandler();
        this.packetHandlers[Protocol.PACKET_HOTBAR & 0xff] = new PacketHotbarHandler();
        this.packetHandlers[Protocol.PACKET_TEXT & 0xff] = new PacketTextHandler();
        this.packetHandlers[Protocol.PACKET_COMMAND_REQUEST & 0xff] = new PacketCommandRequestHandler();
        this.packetHandlers[Protocol.PACKET_WORLD_SOUND_EVENT_V1 & 0xff] = new PacketWorldSoundEventHandler();
        this.packetHandlers[Protocol.PACKET_ANIMATE & 0xff] = new PacketAnimateHandler();
        this.packetHandlers[Protocol.PACKET_ENTITY_EVENT & 0xff] = new PacketEntityEventHandler();
        this.packetHandlers[Protocol.PACKET_MODAL_RESPONSE & 0xFF] = new PacketModalResponseHandler();
        this.packetHandlers[Protocol.PACKET_SERVER_SETTINGS_REQUEST & 0xFF] = new PacketServerSettingsRequestHandler();
        this.packetHandlers[Protocol.PACKET_ENTITY_FALL & 0xFF] = new PacketEntityFallHandler();
        this.packetHandlers[Protocol.PACKET_BOOK_EDIT & 0xFF] = new PacketBookEditHandler();
        this.packetHandlers[Protocol.PACKET_SET_LOCAL_PLAYER_INITIALIZED & 0xff] = new PacketSetLocalPlayerAsInitializedHandler();
        this.packetHandlers[Protocol.PACKET_TILE_ENTITY_DATA & 0xff] = new PacketTileEntityDataHandler();
        this.packetHandlers[Protocol.PACKET_BOSS_BAR & 0xff] = new PacketBossBarHandler();
        this.packetHandlers[Protocol.PACKET_RESPAWN_POSITION & 0xff] = new PacketRespawnPositionHandler();
        this.packetHandlers[Protocol.PACKET_WORLD_SOUND_EVENT & 0xff] = new PacketWorldSoundEventHandler();
        this.packetHandlers[Protocol.PACKET_TICK_SYNC & 0xff] = new PacketTickSyncHandler();
        this.packetHandlers[Protocol.PACKET_CLIENT_CACHE_STATUS & 0xff] = new PacketClientCacheStatusHandler();
        this.packetHandlers[Protocol.PACKET_EMOTE_LIST & 0xff] = new PacketEmoteListHandler();
        this.packetHandlers[Protocol.PACKET_VIOLATION_WARNING & 0xff] = new PacketViolationWarningHandler();
        this.packetHandlers[Protocol.PACKET_CLIENT_CACHE_BLOB_STATUS & 0xff] = new PacketClientCacheBlobStatusHandler();
        this.packetHandlers[Protocol.PACKET_ITEM_STACK_REQUEST & 0xff] = new PacketItemStackRequestHandler();
    }

    // ======================================= PUBLIC API ======================================= //

    /**
     * Initializes the network manager and its underlying server socket.
     *
     * @param maxConnections The maximum number of players expected to join the server
     * @param host           The hostname the internal socket should be bound to
     * @param port           The port the internal socket should be bound to
     * @throws SocketException Thrown if any the internal socket could not be bound
     */
    public void initialize(int maxConnections, String host, int port) throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");               // We currently don't use ipv6
        System.setProperty("io.netty.selectorAutoRebuildThreshold", "0");     // Never rebuild selectors
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);   // Eats performance

        if (this.socket != null) {
            throw new IllegalStateException("Cannot re-initialize network manager");
        }

        this.socket = new ServerSocket(LOGGER, maxConnections);
        this.socket.setMojangModificationEnabled(true);
        this.socket.setEventHandler((eventSocket, socketEvent) -> NetworkManager.this.handleSocketEvent(socketEvent));
        this.socket.bind(host, port);
    }

    /**
     * Sets whether or not unknown packets should be dumped.
     *
     * @param dump Whether or not to enable packet dumping
     */
    public void dumpingEnabled(boolean dump) {
        this.dump = dump;
    }

    /**
     * Sets the directory where packet dump should be written to if dumping is enabled.
     *
     * @param dumpDirectory The directory to write packet dumps into
     */
    public void dumpDirectory(File dumpDirectory) {
        this.dumpDirectory = dumpDirectory;
    }

    /**
     * Ticks the network manager, i.e. updates all player connections and handles all incoming
     * data packets.
     *
     * @param currentMillis The current time in milliseconds. Used to reduce the number of calls to System#currentTimeMillis()
     * @param lastTickTime  The delta from the full second which has been calculated in the last tick
     */
    public void update(long currentMillis, float lastTickTime) {
        // Handle updates to player map:
        while (!this.incomingConnections.isEmpty()) {
            PlayerConnection connection = this.incomingConnections.poll();
            if (connection != null) {
                LOGGER.debug("Adding new connection to the server: {}", connection);
                this.playersByGuid.put(connection.id(), connection);
                this.tickingPlayers.put(connection.id(), connection);
            }
        }

        if (!this.closedConnections.isEmpty()) {
            for (long guid : this.closedConnections) {
                PlayerConnection connection = this.playersByGuid.remove(guid);
                this.tickingPlayers.remove(guid);
                if (connection != null) {
                    final EntityPlayer entity = connection.entity();
                    if (entity != null && entity.world() != null) {
                        entity.world().syncScheduler().execute(connection::close);
                    } else {
                        connection.close();
                    }
                }
            }

            this.closedConnections.clear();
        }

        if (!this.untickQueue.isEmpty()) {
            for (long guid : this.untickQueue) {
                this.tickingPlayers.remove(guid);
            }
            this.untickQueue.clear();
        }

        // Tick all player connections in order to receive all incoming packets:
        for (PlayerConnection connection : this.tickingPlayers.values()) {
            connection.update(currentMillis, lastTickTime);
        }
    }

    /**
     * Closes the network handling
     */
    public void close() {
        LOGGER.info("Shutting down networking - socket and event loops");

        if (this.socket != null) {
            this.socket.close();
            this.socket = null;
        }
        // Close the jRaknet EventLoops, we don't need them anymore
        try {
            EventLoops.cleanup();

            GlobalEventExecutor.INSTANCE.awaitInactivity(5, TimeUnit.SECONDS);
            ThreadDeathWatcher.awaitInactivity(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Could not shutdown netty loops", e);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Shutdown of network completed");
    }

    /**
     * Removes player connection from {@code GoMint Main Thread} ticking.<br>
     * Should only be called from {@code GoMint Main Thread}.
     *
     * @param guid player id (guid)
     */
    public void untickPlayer(long guid) {
        this.untickQueue.add(guid);
    }

    // ======================================= INTERNALS ======================================= //

    /**
     * Gets the GoMint server instance that created this network manager.
     *
     * @return The GoMint server instance that created this network manager
     */
    public GoMintServer server() {
        return this.server;
    }

    /**
     * Invoked by a player connection whenever it encounters a packet it may not decompose.
     *
     * @param packetId The ID of the packet
     * @param buffer   The packet's contents without its ID
     */
    void notifyUnknownPacket(int packetId, PacketBuffer buffer) {
        ReportUploader.create().property("network.unknown_packet", "0x" + Integer.toHexString(packetId & 0xFF)).upload("Unknown packet 0x" + Integer.toHexString(((int) packetId) & 0xFF));

        if (this.dump) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Received unknown packet 0x{}", Integer.toHexString(packetId & 0xFF));
            }

            this.dumpPacket(packetId, buffer);
        }
    }

    // ======================================== SOCKET HANDLERS ======================================== //

    /**
     * Handles the given socket event.
     *
     * @param event The event that was received
     */
    private void handleSocketEvent(SocketEvent event) {
        switch (event.getType()) {
            case NEW_INCOMING_CONNECTION:
                if (!this.server.isRunning()) {
                    event.getConnection().disconnect(null);
                    return;
                }
                PlayerPreLoginEvent playerPreLoginEvent = this.server().pluginManager().callEvent(
                    new PlayerPreLoginEvent(event.getConnection().getAddress())
                );

                if (playerPreLoginEvent.cancelled()) {
                    // Since the user has not gotten any packets we are not able to be sure if we can send him a disconnect notification
                    // so we decide to close the raknet connection without any notice
                    event.getConnection().disconnect(null);
                    return;
                }

                this.handleNewConnection(event.getConnection());
                break;

            case CONNECTION_CLOSED:
            case CONNECTION_DISCONNECTED:
                this.handleConnectionClosed(event.getConnection().getGuid());
                break;

            case UNCONNECTED_PING:
                if (!this.server.isRunning()) {
                    event.getConnection().disconnect(null);
                    return;
                }
                this.handleUnconnectedPing(event);
                break;

            default:
                break;
        }
    }

    private void handleUnconnectedPing(SocketEvent event) {
        // Fire ping event so plugins can modify the motd and player amounts
        PingEvent pingEvent = this.server.pluginManager().callEvent(
            new PingEvent(
                this.server.motd(),
                this.server.currentPlayerCount(),
                this.server.serverConfig().maxPlayers()
            )
        );

        event.getPingPongInfo().setMotd("MCPE;" + pingEvent.motd() + ";" + Protocol.MINECRAFT_PE_PROTOCOL_VERSION +
            ";" + Protocol.MINECRAFT_PE_NETWORK_VERSION + ";" + pingEvent.onlinePlayers() + ";" + pingEvent.maxPlayers() + ";" + this.socket.getGuid());
    }

    /**
     * Handles a new incoming connection.
     *
     * @param newConnection The new incoming connection
     */
    private void handleNewConnection(Connection newConnection) {
        PlayerConnection playerConnection = new PlayerConnection(this, newConnection);
        this.incomingConnections.add(playerConnection);
    }

    /**
     * Handles a connection that just got closed.
     *
     * @param id of the connection being closed
     */
    private void handleConnectionClosed(long id) {
        this.closedConnections.add(id);
    }

    private void dumpPacket(int packetId, PacketBuffer buffer) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Dumping packet {}", Integer.toHexString(packetId));
        }

        StringBuilder filename = new StringBuilder(Integer.toHexString(packetId));
        while (filename.length() < 2) {
            filename.insert(0, "0");
        }

        filename.append("_").append(System.currentTimeMillis());
        filename.append(".dump");

        File dumpFile = new File(this.dumpDirectory, filename.toString());

        // Dump buffer contents:
        try (OutputStream out = new FileOutputStream(dumpFile)) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                writer.write("# Packet dump of 0x" + Integer.toHexString(packetId) + "\n");
                writer.write("-------------------------------------\n");
                writer.write("# Textual payload\n");
                StringBuilder lineBuilder = new StringBuilder();

                int pos = buffer.getReadPosition();
                while (buffer.getRemaining() > 0) {
                    for (int i = 0; i < 16 && buffer.getRemaining() > 0; ++i) {
                        String hex = Integer.toHexString(((int) buffer.readByte()) & 0xFF);
                        if (hex.length() < 2) {
                            hex = "0" + hex;
                        }
                        lineBuilder.append(hex);
                        if (i + 1 < 16 && buffer.getRemaining() > 0) {
                            lineBuilder.append(" ");
                        }
                    }
                    lineBuilder.append("\n");

                    writer.write(lineBuilder.toString());
                    lineBuilder = new StringBuilder();
                }
                writer.write("-------------------------------------\n");
                writer.write("# Binary payload\n");
                writer.flush();

                buffer.setReadPosition(pos);
                byte[] data = new byte[buffer.getRemaining()];
                buffer.readBytes(data);
                out.write(data);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to dump packet " + filename);
        }
    }

    /**
     * Get the port this server has bound to
     *
     * @return bound port
     */
    public int port() {
        return this.socket.getBindAddress().getPort();
    }

    public void stopNewConnections() {
        LOGGER.info("Shutting down new connections");

        for (Long2ObjectMap.Entry<PlayerConnection> entry : this.tickingPlayers.long2ObjectEntrySet()) {
            entry.getValue().close();
        }

        LOGGER.info("Shutdown of new connections");
    }

    public <T extends Packet> PacketHandler<T> getPacketHandler(int packetId) {
        return (PacketHandler<T>) this.packetHandlers[packetId];
    }

}
