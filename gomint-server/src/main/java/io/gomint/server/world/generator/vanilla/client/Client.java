/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.generator.vanilla.client;

import io.gomint.crypto.Processor;
import io.gomint.jraknet.ClientSocket;
import io.gomint.jraknet.Connection;
import io.gomint.jraknet.EncapsulatedPacket;
import io.gomint.jraknet.PacketBuffer;
import io.gomint.jraknet.PacketReliability;
import io.gomint.jraknet.SocketEvent;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.scheduler.Task;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.jwt.JwtSignatureException;
import io.gomint.server.jwt.JwtToken;
import io.gomint.server.network.ConnectionWithState;
import io.gomint.server.network.EncryptionHandler;
import io.gomint.server.network.EncryptionKeyFactory;
import io.gomint.server.network.NetworkManager;
import io.gomint.server.network.PlayerConnectionState;
import io.gomint.server.network.PostProcessExecutor;
import io.gomint.server.network.Protocol;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketAdventureSettings;
import io.gomint.server.network.packet.PacketBatch;
import io.gomint.server.network.packet.PacketClientCacheStatus;
import io.gomint.server.network.packet.PacketDisconnect;
import io.gomint.server.network.packet.PacketEncryptionRequest;
import io.gomint.server.network.packet.PacketEncryptionResponse;
import io.gomint.server.network.packet.PacketLogin;
import io.gomint.server.network.packet.PacketMovePlayer;
import io.gomint.server.network.packet.PacketPlayState;
import io.gomint.server.network.packet.PacketRequestChunkRadius;
import io.gomint.server.network.packet.PacketResourcePackResponse;
import io.gomint.server.network.packet.PacketSetLocalPlayerAsInitialized;
import io.gomint.server.network.packet.PacketStartGame;
import io.gomint.server.network.packet.PacketWorldChunk;
import io.gomint.server.resource.ResourceResponseStatus;
import io.gomint.server.util.Palette;
import io.gomint.server.util.Values;
import io.gomint.server.world.ChunkAdapter;
import io.gomint.server.world.ChunkSlice;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.generator.vanilla.chunk.ChunkRequest;
import io.gomint.server.world.generator.vanilla.debug.UI;
import io.gomint.taglib.NBTReader;
import io.gomint.taglib.NBTTagCompound;
import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static io.gomint.server.network.Protocol.*;

/**
 * @author geNAZt
 * @version 1.0
 */
public class Client implements ConnectionWithState {

    private static final String SKIN = "gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP+AgID/gICA/4CAgP8=";
    private static final AtomicInteger CLIENT_IDS = new AtomicInteger(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final PostProcessExecutor postProcessExecutor;
    private final EncryptionKeyFactory keyFactory;

    private ClientSocket socket;
    private Connection connection;

    private PlayerConnectionState state = PlayerConnectionState.HANDSHAKE;

    private EncryptionHandler encryptionHandler;

    private Location spawn;
    private Location currentPos;
    private String name;

    private long ownId;
    private long runtimeId;

    private Consumer<BlockPosition> spawnPointConsumer;

    private WorldAdapter world;
    private AtomicBoolean spawned = new AtomicBoolean(false);

    private Consumer<Void> disconnectConsumer;
    private boolean disconnected;

    // Processors
    private Processor inputProcessor = new Processor(false);
    private Processor outputProcessor = new Processor(true);

    // Update thread
    private Task networkUpdater;
    private UI debugUI;

    // Chunk processing
    private final Queue<ChunkRequest> queue;
    private ChunkRequest current;

    public Client(WorldAdapter world, EncryptionKeyFactory encryptionKeyFactory,
                  Queue<ChunkRequest> queue, PostProcessExecutor postProcessExecutor, UI debugUI) {
        this.world = world;
        this.keyFactory = encryptionKeyFactory;
        this.postProcessExecutor = postProcessExecutor;
        this.debugUI = debugUI;
        this.queue = queue;

        this.networkUpdater = this.world.server().asyncScheduler().scheduleAsync(this::update, (int) Values.CLIENT_TICK_MS, (int) Values.CLIENT_TICK_MS, TimeUnit.MILLISECONDS);

        this.socket = new ClientSocket(LoggerFactory.getLogger(NetworkManager.class));

        try {
            this.socket.initialize();
            this.socket.setMojangModificationEnabled(true);
            this.socket.setEventHandler((socket, socketEvent) -> {
                if (socketEvent.getType() == SocketEvent.Type.CONNECTION_ATTEMPT_SUCCEEDED) {
                    Client.this.connection = socketEvent.getConnection();
                    Client.this.connection.addDataProcessor(packetData -> {
                        if (packetData.getPacketData().readableBytes() <= 0) {
                            // Malformed packet:
                            return packetData;
                        }

                        // Check if packet is batched
                        byte packetId = packetData.getPacketData().readByte();
                        if (packetId == Protocol.BATCH_MAGIC) {
                            // Decompress and decrypt
                            ByteBuf pureData = handleBatchPacket(packetData.getPacketData());
                            EncapsulatedPacket newPacket = new EncapsulatedPacket();
                            newPacket.setPacketData(pureData);

                            pureData.release(); // The packet takes over ownership
                            // We don't need to release the input because the batch packet handler releases it
                            return newPacket;
                        }

                        packetData.getPacketData().readerIndex(0);
                        return packetData;
                    });

                    Client.this.login();
                } else if (socketEvent.getType() == SocketEvent.Type.CONNECTION_CLOSED || socketEvent.getType() == SocketEvent.Type.CONNECTION_DISCONNECTED || socketEvent.getType() == SocketEvent.Type.CONNECTION_ATTEMPT_FAILED) {
                    if (!Client.this.disconnected) {
                        Client.this.disconnected = true;
                        Client.this.disconnectConsumer.accept(null);
                    }
                }
            });
        } catch (SocketException e) {
            LOGGER.warn("Exception caught", e);
        }
    }

    public void spawnPointConsumer(Consumer<BlockPosition> spawnPointConsumer) {
        this.spawnPointConsumer = spawnPointConsumer;
    }

    /**
     * Connect to the given server
     *
     * @param ip   of the server
     * @param port of the server
     */
    public void connect(String ip, int port) {
        this.socket.connect(ip, port);
    }

    /**
     * Handles compressed batch packets directly by decoding their payload.
     *
     * @param buffer The buffer containing the batch packet's data (except packet ID)
     * @return decompressed and decrypted data
     */
    private ByteBuf handleBatchPacket(ByteBuf buffer) {
        return this.inputProcessor.process(buffer);
    }

    public void disconnect(String message) {
        if (message != null && message.length() > 0) {
            PacketDisconnect packet = new PacketDisconnect();
            packet.setMessage(message);
            this.send(packet);

            Client.this.internalClose(message);
        } else {
            this.internalClose(message);
        }
    }

    @Override
    public void send(Packet packet) {
        if (this.connection == null) {
            return;
        }

        if (!(packet instanceof PacketBatch)) {
            this.postProcessExecutor.addWork(this, new Packet[]{packet}, null);
        } else {
            try {
                PacketBuffer buffer = new PacketBuffer(8);
                packet.serializeHeader(buffer);
                packet.serialize(buffer, Protocol.MINECRAFT_PE_PROTOCOL_VERSION);

                this.connection.send(PacketReliability.RELIABLE_ORDERED, packet.orderingChannel(), buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        this.updateNetwork();

        if (this.current != null) {
            ChunkAdapter chunkAdapter = this.world.chunkCache().getChunk(this.current.x(), this.current.z());
            if (chunkAdapter != null) {
                LOGGER.debug("Resolving current request {} / {} with a already cached chunk", this.current.x(), this.current.z());

                this.current.future().resolve(chunkAdapter);
                this.current = null;
            }
        }

        // Check if we can consume another chunk request
        if (this.spawned.get() && this.current == null) {
            this.current = this.queue.poll();
            if (this.current != null) {
                ChunkAdapter adapter = this.world.chunkCache().getChunk(this.current.x(), this.current.z());
                if (adapter != null) {
                    LOGGER.debug("Resolving current request {} / {} with a already cached chunk", this.current.x(), this.current.z());

                    this.current.future().resolve(adapter);
                    this.current = null;
                } else {
                    LOGGER.debug("Moving to chunk request {} / {}", this.current.x(), this.current.z());
                    this.moveToChunk(this.current);
                }
            }
        }
    }

    private void updateNetwork() {
        // It seems that movement is sent last, but we need it first to check if player position of other packets align
        List<PacketBuffer> packetBuffers = null;

        // Receive all waiting packets:
        EncapsulatedPacket packetData;
        while ((packetData = this.connection.receive()) != null) {
            if (packetBuffers == null) {
                packetBuffers = new ArrayList<>();
            }

            packetBuffers.add(new PacketBuffer(packetData.getPacketData()));
            packetData.release(); // The internal buffer took over
        }

        if (packetBuffers != null) {
            for (PacketBuffer buffer : packetBuffers) {
                // CHECKSTYLE:OFF
                try {
                    this.handleSocketData(buffer);
                } catch (Exception e) {
                    LOGGER.error("Error whilst processing packet: ", e);
                }
                // CHECKSTYLE:ON

                buffer.release();
            }
        }
    }

    @Override
    public PlayerConnectionState state() {
        return this.state;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public int protocolID() {
        return Protocol.MINECRAFT_PE_PROTOCOL_VERSION;
    }

    @Override
    public Processor outputProcessor() {
        return this.outputProcessor;
    }

    private void internalClose(String message) {
        if (this.connection != null) {
            if (this.connection.isConnected() && !this.connection.isDisconnecting()) {
                this.connection.disconnect(message);
            }

            this.networkUpdater.cancel();
            this.networkUpdater = null;

            this.connection = null;
        }
    }

    private void login() {
        if (this.state == PlayerConnectionState.HANDSHAKE) {
            this.state = PlayerConnectionState.LOGIN;

            PacketLogin login = new PacketLogin();
            login.setProtocol(Protocol.MINECRAFT_PE_PROTOCOL_VERSION);

            // Send our handshake to the server -> this will trigger it to respond with a 0x03 ServerHandshake packet:
            MojangLoginForger mojangLoginForger = new MojangLoginForger();
            mojangLoginForger.publicKey(this.keyFactory.keyPair().getPublic());
            mojangLoginForger.username(this.name = "GoBot_" + CLIENT_IDS.incrementAndGet());
            mojangLoginForger.uuid(UUID.randomUUID());
            mojangLoginForger.skinData(new JSONObject(new HashMap<>() {{
                put("ServerAddress", "127.0.0.1:" + Client.this.connection.getAddress().getPort());
                put("CurrentInputMode", 1);
                put("DefaultInputMode", 1);
                put("ClientRandomId", ThreadLocalRandom.current().nextInt());
                put("GuiScale", 0);
                put("GameVersion", "1.16.100");
                put("ThirdPartyName", Client.this.name);
                put("DeviceModel", "");
                put("DeviceOS", 1);
                put("CapeData", "");
                put("SkinId", mojangLoginForger.uuid().toString() + "_Alex");
                put("SkinGeometry", "CnsKICAgImZvcm1hdF92ZXJzaW9uIiA6ICIxLjEyLjAiLAogICAibWluZWNyYWZ0Omdlb21ldHJ5IiA6IFsKICAgICAgewogICAgICAgICAiYm9uZXMiIDogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJuYW1lIiA6ICJib2R5IiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAid2Fpc3QiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAwLjAsIDI0LjAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgIm5hbWUiIDogIndhaXN0IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMC4wLCAxMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC01LjAsIDguMCwgMy4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgMTAsIDE2LCAxIF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDAsIDAgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJjYXBlIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAiYm9keSIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDAuMCwgMjQuMCwgMy4wIF0sCiAgICAgICAgICAgICAgICJyb3RhdGlvbiIgOiBbIDAuMCwgMTgwLjAsIDAuMCBdCiAgICAgICAgICAgIH0KICAgICAgICAgXSwKICAgICAgICAgImRlc2NyaXB0aW9uIiA6IHsKICAgICAgICAgICAgImlkZW50aWZpZXIiIDogImdlb21ldHJ5LmNhcGUiLAogICAgICAgICAgICAidGV4dHVyZV9oZWlnaHQiIDogMzIsCiAgICAgICAgICAgICJ0ZXh0dXJlX3dpZHRoIiA6IDY0CiAgICAgICAgIH0KICAgICAgfSwKICAgICAgewogICAgICAgICAiYm9uZXMiIDogWwogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJuYW1lIiA6ICJyb290IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMC4wLCAwLjAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImN1YmVzIiA6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTQuMCwgMTIuMCwgLTIuMCBdLAogICAgICAgICAgICAgICAgICAgICAic2l6ZSIgOiBbIDgsIDEyLCA0IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDE2LCAxNiBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm5hbWUiIDogImJvZHkiLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJ3YWlzdCIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDAuMCwgMjQuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAibmFtZSIgOiAid2Fpc3QiLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJyb290IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMC4wLCAxMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC00LjAsIDI0LjAsIC00LjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA4LCA4LCA4IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDAsIDAgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJoZWFkIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAiYm9keSIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDAuMCwgMjQuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAibmFtZSIgOiAiY2FwZSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImJvZHkiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAwLjAsIDI0LCAzLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgImluZmxhdGUiIDogMC41MCwKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC00LjAsIDI0LjAsIC00LjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA4LCA4LCA4IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDMyLCAwIF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAiaGF0IiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAiaGVhZCIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDAuMCwgMjQuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyA0LjAsIDEyLjAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA0LCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyAzMiwgNDggXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJsZWZ0QXJtIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAiYm9keSIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDUuMCwgMjIuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJpbmZsYXRlIiA6IDAuMjUwLAogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgNC4wLCAxMi4wLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgNCwgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgNDgsIDQ4IF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAibGVmdFNsZWV2ZSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImxlZnRBcm0iLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyA1LjAsIDIyLjAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgIm5hbWUiIDogImxlZnRJdGVtIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAibGVmdEFybSIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDYuMCwgMTUuMCwgMS4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyAtOC4wLCAxMi4wLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgNCwgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgNDAsIDE2IF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAicmlnaHRBcm0iLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJib2R5IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgLTUuMCwgMjIuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJpbmZsYXRlIiA6IDAuMjUwLAogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTguMCwgMTIuMCwgLTIuMCBdLAogICAgICAgICAgICAgICAgICAgICAic2l6ZSIgOiBbIDQsIDEyLCA0IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDQwLCAzMiBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm5hbWUiIDogInJpZ2h0U2xlZXZlIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAicmlnaHRBcm0iLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAtNS4wLCAyMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJsb2NhdG9ycyIgOiB7CiAgICAgICAgICAgICAgICAgICJsZWFkX2hvbGQiIDogWyAtNiwgMTUsIDEgXQogICAgICAgICAgICAgICB9LAogICAgICAgICAgICAgICAibmFtZSIgOiAicmlnaHRJdGVtIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAicmlnaHRBcm0iLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAtNiwgMTUsIDEgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC0wLjEwLCAwLjAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA0LCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyAxNiwgNDggXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJsZWZ0TGVnIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAicm9vdCIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDEuOTAsIDEyLjAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImN1YmVzIiA6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAiaW5mbGF0ZSIgOiAwLjI1MCwKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC0wLjEwLCAwLjAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA0LCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyAwLCA0OCBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm5hbWUiIDogImxlZnRQYW50cyIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImxlZnRMZWciLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAxLjkwLCAxMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC0zLjkwLCAwLjAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA0LCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyAwLCAxNiBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm5hbWUiIDogInJpZ2h0TGVnIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAicm9vdCIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIC0xLjkwLCAxMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgImluZmxhdGUiIDogMC4yNTAsCiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyAtMy45MCwgMC4wLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgNCwgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgMCwgMzIgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJyaWdodFBhbnRzIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAicmlnaHRMZWciLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAtMS45MCwgMTIuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJpbmZsYXRlIiA6IDAuMjUwLAogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTQuMCwgMTIuMCwgLTIuMCBdLAogICAgICAgICAgICAgICAgICAgICAic2l6ZSIgOiBbIDgsIDEyLCA0IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDE2LCAzMiBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm5hbWUiIDogImphY2tldCIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImJvZHkiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAwLjAsIDI0LjAsIDAuMCBdCiAgICAgICAgICAgIH0KICAgICAgICAgXSwKICAgICAgICAgImRlc2NyaXB0aW9uIiA6IHsKICAgICAgICAgICAgImlkZW50aWZpZXIiIDogImdlb21ldHJ5Lmh1bWFub2lkLmN1c3RvbSIsCiAgICAgICAgICAgICJ0ZXh0dXJlX2hlaWdodCIgOiA2NCwKICAgICAgICAgICAgInRleHR1cmVfd2lkdGgiIDogNjQsCiAgICAgICAgICAgICJ2aXNpYmxlX2JvdW5kc19oZWlnaHQiIDogMiwKICAgICAgICAgICAgInZpc2libGVfYm91bmRzX29mZnNldCIgOiBbIDAsIDEsIDAgXSwKICAgICAgICAgICAgInZpc2libGVfYm91bmRzX3dpZHRoIiA6IDEKICAgICAgICAgfQogICAgICB9LAogICAgICB7CiAgICAgICAgICJib25lcyIgOiBbCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgIm5hbWUiIDogInJvb3QiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAwLjAsIDAuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAibmFtZSIgOiAid2Fpc3QiLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJyb290IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMC4wLCAxMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC00LjAsIDEyLjAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA4LCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyAxNiwgMTYgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJib2R5IiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAid2Fpc3QiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAwLjAsIDI0LjAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImN1YmVzIiA6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTQuMCwgMjQuMCwgLTQuMCBdLAogICAgICAgICAgICAgICAgICAgICAic2l6ZSIgOiBbIDgsIDgsIDggXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgMCwgMCBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm5hbWUiIDogImhlYWQiLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJib2R5IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMC4wLCAyNC4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgImluZmxhdGUiIDogMC41MCwKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC00LjAsIDI0LjAsIC00LjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA4LCA4LCA4IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDMyLCAwIF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAiaGF0IiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAiaGVhZCIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDAuMCwgMjQuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyAtMy45MCwgMC4wLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgNCwgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgMCwgMTYgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJyaWdodExlZyIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogInJvb3QiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAtMS45MCwgMTIuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJpbmZsYXRlIiA6IDAuMjUwLAogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTMuOTAsIDAuMCwgLTIuMCBdLAogICAgICAgICAgICAgICAgICAgICAic2l6ZSIgOiBbIDQsIDEyLCA0IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDAsIDMyIF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAicmlnaHRQYW50cyIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogInJpZ2h0TGVnIiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgLTEuOTAsIDEyLjAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImN1YmVzIiA6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTAuMTAsIDAuMCwgLTIuMCBdLAogICAgICAgICAgICAgICAgICAgICAic2l6ZSIgOiBbIDQsIDEyLCA0IF0sCiAgICAgICAgICAgICAgICAgICAgICJ1diIgOiBbIDE2LCA0OCBdCiAgICAgICAgICAgICAgICAgIH0KICAgICAgICAgICAgICAgXSwKICAgICAgICAgICAgICAgIm1pcnJvciIgOiB0cnVlLAogICAgICAgICAgICAgICAibmFtZSIgOiAibGVmdExlZyIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogInJvb3QiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAxLjkwLCAxMi4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgImluZmxhdGUiIDogMC4yNTAsCiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyAtMC4xMCwgMC4wLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgNCwgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgMCwgNDggXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJsZWZ0UGFudHMiLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJsZWZ0TGVnIiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMS45MCwgMTIuMCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyA0LjAsIDExLjUwLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgMywgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgMzIsIDQ4IF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAibGVmdEFybSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImJvZHkiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyA1LjAsIDIxLjUwLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJjdWJlcyIgOiBbCiAgICAgICAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgICAgICAgImluZmxhdGUiIDogMC4yNTAsCiAgICAgICAgICAgICAgICAgICAgICJvcmlnaW4iIDogWyA0LjAsIDExLjUwLCAtMi4wIF0sCiAgICAgICAgICAgICAgICAgICAgICJzaXplIiA6IFsgMywgMTIsIDQgXSwKICAgICAgICAgICAgICAgICAgICAgInV2IiA6IFsgNDgsIDQ4IF0KICAgICAgICAgICAgICAgICAgfQogICAgICAgICAgICAgICBdLAogICAgICAgICAgICAgICAibmFtZSIgOiAibGVmdFNsZWV2ZSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImxlZnRBcm0iLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyA1LjAsIDIxLjUwLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJuYW1lIiA6ICJsZWZ0SXRlbSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImxlZnRBcm0iLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyA2LCAxNC41MCwgMSBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImN1YmVzIiA6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTcuMCwgMTEuNTAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyAzLCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyA0MCwgMTYgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJyaWdodEFybSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogImJvZHkiLAogICAgICAgICAgICAgICAicGl2b3QiIDogWyAtNS4wLCAyMS41MCwgMC4wIF0KICAgICAgICAgICAgfSwKICAgICAgICAgICAgewogICAgICAgICAgICAgICAiY3ViZXMiIDogWwogICAgICAgICAgICAgICAgICB7CiAgICAgICAgICAgICAgICAgICAgICJpbmZsYXRlIiA6IDAuMjUwLAogICAgICAgICAgICAgICAgICAgICAib3JpZ2luIiA6IFsgLTcuMCwgMTEuNTAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyAzLCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyA0MCwgMzIgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJyaWdodFNsZWV2ZSIsCiAgICAgICAgICAgICAgICJwYXJlbnQiIDogInJpZ2h0QXJtIiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgLTUuMCwgMjEuNTAsIDAuMCBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImxvY2F0b3JzIiA6IHsKICAgICAgICAgICAgICAgICAgImxlYWRfaG9sZCIgOiBbIC02LCAxNC41MCwgMSBdCiAgICAgICAgICAgICAgIH0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJyaWdodEl0ZW0iLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJyaWdodEFybSIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIC02LCAxNC41MCwgMSBdCiAgICAgICAgICAgIH0sCiAgICAgICAgICAgIHsKICAgICAgICAgICAgICAgImN1YmVzIiA6IFsKICAgICAgICAgICAgICAgICAgewogICAgICAgICAgICAgICAgICAgICAiaW5mbGF0ZSIgOiAwLjI1MCwKICAgICAgICAgICAgICAgICAgICAgIm9yaWdpbiIgOiBbIC00LjAsIDEyLjAsIC0yLjAgXSwKICAgICAgICAgICAgICAgICAgICAgInNpemUiIDogWyA4LCAxMiwgNCBdLAogICAgICAgICAgICAgICAgICAgICAidXYiIDogWyAxNiwgMzIgXQogICAgICAgICAgICAgICAgICB9CiAgICAgICAgICAgICAgIF0sCiAgICAgICAgICAgICAgICJuYW1lIiA6ICJqYWNrZXQiLAogICAgICAgICAgICAgICAicGFyZW50IiA6ICJib2R5IiwKICAgICAgICAgICAgICAgInBpdm90IiA6IFsgMC4wLCAyNC4wLCAwLjAgXQogICAgICAgICAgICB9LAogICAgICAgICAgICB7CiAgICAgICAgICAgICAgICJuYW1lIiA6ICJjYXBlIiwKICAgICAgICAgICAgICAgInBhcmVudCIgOiAiYm9keSIsCiAgICAgICAgICAgICAgICJwaXZvdCIgOiBbIDAuMCwgMjQsIC0zLjAgXQogICAgICAgICAgICB9CiAgICAgICAgIF0sCiAgICAgICAgICJkZXNjcmlwdGlvbiIgOiB7CiAgICAgICAgICAgICJpZGVudGlmaWVyIiA6ICJnZW9tZXRyeS5odW1hbm9pZC5jdXN0b21TbGltIiwKICAgICAgICAgICAgInRleHR1cmVfaGVpZ2h0IiA6IDY0LAogICAgICAgICAgICAidGV4dHVyZV93aWR0aCIgOiA2NCwKICAgICAgICAgICAgInZpc2libGVfYm91bmRzX2hlaWdodCIgOiAyLAogICAgICAgICAgICAidmlzaWJsZV9ib3VuZHNfb2Zmc2V0IiA6IFsgMCwgMSwgMCBdLAogICAgICAgICAgICAidmlzaWJsZV9ib3VuZHNfd2lkdGgiIDogMQogICAgICAgICB9CiAgICAgIH0KICAgXQp9Cg==");
                put("SkinData", SKIN);
                put("SkinResourcePatch", "ewogICAiZ2VvbWV0cnkiIDogewogICAgICAiZGVmYXVsdCIgOiAiZ2VvbWV0cnkuaHVtYW5vaWQuY3VzdG9tIgogICB9Cn0K");
                put("SkinImageHeight", 32);
                put("SkinImageWidth", 64);
                put("SelfSignedId", UuidUtil.getTimeBasedUuid().toString());
                put("PlatformOnlineId", "");
                put("PlatformOfflineId", "");
                put("LanguageCode", "en_US");
                put("ADRole", 0);
                put("UIProfile", 0);
            }}));
            mojangLoginForger.xuid(String.valueOf(CLIENT_IDS.get()));

            String jwt = "{\"chain\":[\"" + mojangLoginForger.forge(this.keyFactory.keyPair().getPrivate()) + "\"]}";
            String skin = mojangLoginForger.forgeSkin(this.keyFactory.keyPair().getPrivate());

            // More data please
            ByteBuffer byteBuffer = ByteBuffer.allocate(jwt.length() + skin.length() + 8);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putInt(jwt.length());
            byteBuffer.put(jwt.getBytes());

            // We need the skin
            byteBuffer.putInt(skin.length());
            byteBuffer.put(skin.getBytes());

            login.setPayload(byteBuffer.array());
            this.send(login);

            // Check for termination if server did not respond
            this.world.server().asyncScheduler().scheduleAsync(() -> {
                if (this.state == PlayerConnectionState.LOGIN) {
                    this.disconnect("Not logged in");
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    /**
     * Handles data received directly from the player's connection.
     *
     * @param buffer The buffer containing the received data
     */
    private void handleSocketData(PacketBuffer buffer) {
        if (buffer.getRemaining() <= 0) {
            // Malformed packet:
            return;
        }

        while (buffer.getRemaining() > 0) {
            int packetLength = buffer.readUnsignedVarInt();

            int currentIndex = buffer.getReadPosition();
            int packetID = this.handleBufferData(buffer, currentIndex + packetLength);

            int consumedByPacket = buffer.getReadPosition() - currentIndex;
            if (consumedByPacket != packetLength) {
                int remaining = packetLength - consumedByPacket;
                LOGGER.error("Malformed batch packet payload: Could not read enclosed packet data correctly: 0x{} remaining {} bytes", Integer.toHexString(packetID), remaining);
                return;
            }
        }
    }

    private int handleBufferData(PacketBuffer buffer, int skippablePosition) {
        // Grab the packet ID from the packet's data
        int rawId = buffer.readUnsignedVarInt();
        int packetId = rawId & 0x3FF;

        // There is some data behind the packet id when non batched packets (2 bytes)
        Packet packet = Protocol.createPacket(packetId);
        if (packet == null) {
            packet = AdditionalProtocol.createPacket(packetId);
            if (packet == null) {
                // Got to skip
                buffer.setReadPosition(skippablePosition);
                return packetId;
            }
        }

        try {
            packet.deserialize(buffer, Protocol.MINECRAFT_PE_PROTOCOL_VERSION);
            this.handlePacket(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packetId;
    }

    private void sendChunkRadius(int radius) {
        // Send the wanted chunk radius
        PacketRequestChunkRadius chunkRadius = new PacketRequestChunkRadius();
        chunkRadius.setChunkRadius(radius);
        this.send(chunkRadius);
    }

    private void handlePacket(Packet packet) {
        if (packet.getId() == PACKET_DISCONNECT) {
            PacketDisconnect disconnect = (PacketDisconnect) packet;
            LOGGER.warn("Disconnect: {}", disconnect.getMessage());
            return;
        }

        if (packet.getId() == PACKET_WORLD_CHUNK) {
            PacketWorldChunk chunk = (PacketWorldChunk) packet;

            int x = chunk.getX();
            int z = chunk.getZ();

            ChunkAdapter chunkAdapter = this.world.chunkCache().getChunk(x, z);

            // Check if generator needs the chunk
            if (chunkAdapter == null) {
                chunkAdapter = (ChunkAdapter) this.world.generateEmptyChunk(x, z);

                PacketBuffer chunkBuffer = new PacketBuffer(chunk.getData());

                int amountOfSubchunks = chunk.getSubChunkCount();

                LOGGER.debug("Got {} sub chunks for {} / {}", amountOfSubchunks, chunkAdapter.x(), chunkAdapter.z());

                for (int i = 0; i < amountOfSubchunks; i++) {
                    ChunkSlice slice = chunkAdapter.ensureSlice(i);

                    byte subchunkVersion = chunkBuffer.readByte();
                    byte layers = subchunkVersion != 8 ? 1 : chunkBuffer.readByte();

                    for (byte b = 0; b < layers; b++) {
                        byte paletteWord = chunkBuffer.readByte();
                        byte wordTemplate = (byte) (paletteWord >>> 1); // Get rid of the last bit (which seems to be the isPresent state)

                        Palette palette = new Palette(chunkBuffer.getBuffer(), wordTemplate, true);
                        short[] indexes = palette.getIndexes();

                        int amountOfRuntimeIds = chunkBuffer.readSignedVarInt();
                        int[] localRuntimes = new int[amountOfRuntimeIds];
                        for (int i1 = 0; i1 < amountOfRuntimeIds; i1++) {
                            localRuntimes[i1] = chunkBuffer.readSignedVarInt();
                        }

                        short blockCounter = 0;
                        for (short index : indexes) {
                            slice.setRuntimeIdInternal(blockCounter, b, localRuntimes[index]);
                            blockCounter++;
                        }
                    }
                }

                // Read biomes
                byte[] biomes = new byte[256];
                chunkBuffer.readBytes(biomes);
                chunkAdapter.setBiomes(biomes);

                // Border blocks
                chunkBuffer.readSignedVarInt();

                // Read tiles
                if (chunkBuffer.getRemaining() > 0) {
                    NBTReader reader = new NBTReader(chunkBuffer.getBuffer(), ByteOrder.LITTLE_ENDIAN);
                    reader.setUseVarint(true);

                    loop:
                    while (true) {
                        try {
                            NBTTagCompound compound = reader.parse();
                            TileEntity tileEntity = this.world.server().tileEntities().construct(compound, chunkAdapter.blockAt(compound.getInteger("x", 0) & 0xF, compound.getInteger("y", 0), compound.getInteger("z", 0) & 0xF));
                            if (tileEntity != null) {
                                chunkAdapter.setTileEntity(compound.getInteger("x", 0) & 0xF, compound.getInteger("y", 0), compound.getInteger("z", 0) & 0xF, tileEntity);
                            }
                        } catch (Exception e) {
                            break loop;
                        }
                    }
                }

                LOGGER.debug("Adding chunk {} / {} to cache", chunkAdapter.x(), chunkAdapter.z());

                chunkAdapter.populated(true);
                chunkAdapter.calculateHeightmap(240);
                this.world.chunkCache().putChunk(chunkAdapter);

                chunk.release();

                if (this.debugUI != null) {
                    this.debugUI.loadedChunk(chunkAdapter);
                }
            }
        }

        // If we are still in handshake we only accept certain packets:
        if (this.state == PlayerConnectionState.LOGIN) {
            if (packet.getId() == PACKET_PLAY_STATE) {
                PacketPlayState packetPlayState = (PacketPlayState) packet;

                if (packetPlayState.getState() != PacketPlayState.PlayState.LOGIN_SUCCESS) {
                    this.destroy("Server responded with " + packetPlayState.getState().name());
                }

                this.state = PlayerConnectionState.RESOURCE_PACK;
                return;
            } else if (packet.getId() == PACKET_ENCRYPTION_REQUEST) {
                PacketEncryptionRequest packetEncryptionRequest = (PacketEncryptionRequest) packet;

                // We need to verify the JWT request
                JwtToken token = JwtToken.parse(packetEncryptionRequest.getJwt());
                String keyDataBase64 = (String) token.getHeader().getProperty("x5u");
                PublicKey key = this.keyFactory.createPublicKey(keyDataBase64);

                try {
                    if (token.validateSignature(key)) {
                        LOGGER.debug("For server: Valid encryption start JWT");
                    }
                } catch (JwtSignatureException e) {
                    LOGGER.error("Invalid JWT signature from server: ", e);
                }

                this.encryptionHandler = new EncryptionHandler(this.keyFactory);
                this.encryptionHandler.serverPublicKey(key);
                if (this.encryptionHandler.beginServersideEncryption(Base64.getDecoder().decode((String) token.getClaim("salt")))) {
                    // We need every packet to be encrypted from now on
                    this.outputProcessor.enableCrypto(this.encryptionHandler.serverKey(), this.encryptionHandler.serverIv());
                    this.inputProcessor.enableCrypto(this.encryptionHandler.serverKey(), this.encryptionHandler.serverIv());
                }

                // Tell the server that we are ready to receive encrypted packets from now on:
                PacketEncryptionResponse response = new PacketEncryptionResponse();
                this.send(response);

                PacketClientCacheStatus cacheStatus = new PacketClientCacheStatus();
                cacheStatus.setEnabled(false);
                this.send(cacheStatus);
            } else if (packet.getId() == PACKET_RESOURCEPACK_INFO) {
                this.state = PlayerConnectionState.RESOURCE_PACK;
            }
        }

        // When we are in resource pack state
        if (this.state == PlayerConnectionState.RESOURCE_PACK) {
            if (packet.getId() == PACKET_RESOURCEPACK_INFO) {
                PacketResourcePackResponse packetResourcePackResponse = new PacketResourcePackResponse();
                packetResourcePackResponse.setStatus(ResourceResponseStatus.HAVE_ALL_PACKS);
                this.send(packetResourcePackResponse);
            } else if (packet.getId() == PACKET_RESOURCEPACK_STACK) {
                PacketResourcePackResponse packetResourcePackResponse = new PacketResourcePackResponse();
                packetResourcePackResponse.setStatus(ResourceResponseStatus.COMPLETED);
                this.send(packetResourcePackResponse);

                this.state = PlayerConnectionState.PLAYING;
                this.sendChunkRadius(6);

                // Send set local player init
                PacketSetLocalPlayerAsInitialized packetSetLocalPlayerAsInitialized = new PacketSetLocalPlayerAsInitialized();
                packetSetLocalPlayerAsInitialized.setEntityId(this.ownId);
                this.send(packetSetLocalPlayerAsInitialized);
            }
        }

        if (packet.getId() == PACKET_START_GAME) {
            PacketStartGame startGame = ((PacketStartGame) packet);

            this.spawn = startGame.getSpawn();
            this.ownId = startGame.getEntityId();
            this.runtimeId = startGame.getRuntimeEntityId();

            this.spawnPointConsumer.accept(this.spawn.toBlockPosition());
        } else if (packet.getId() == PACKET_PLAY_STATE) {
            PacketPlayState.PlayState playState = ((PacketPlayState) packet).getState();
            if (playState == PacketPlayState.PlayState.SPAWN) {
                // Send first movement
                this.move(this.spawn);

                // Send flying
                PacketAdventureSettings packetAdventureSettings = new PacketAdventureSettings();
                packetAdventureSettings.setFlags(0x40 | 0x200);
                packetAdventureSettings.setEntityId(this.runtimeId);
                this.send(packetAdventureSettings);

                // Send movement to the top of the map
                Location target = new Location(null, this.currentPos.x(), 255, this.currentPos.z(), 0f, 0f);
                this.move(target);

                // Only allow movements now
                this.spawned.set(true);
            }
        } else if (packet.getId() == PACKET_MOVE_PLAYER) {
            PacketMovePlayer player = (PacketMovePlayer) packet;
            if (player.getEntityId() == this.ownId) {
                LOGGER.warn("Server moved us to {}", player);

                Location location = new Location(null, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                this.move(location);
            }
        }
    }

    private void move(Location target) {
        // Send movement to server
        PacketMovePlayer movePlayer = new PacketMovePlayer();
        movePlayer.setEntityId(this.runtimeId);
        movePlayer.setX(target.x());
        movePlayer.setY(target.y());
        movePlayer.setZ(target.z());
        movePlayer.setYaw(target.yaw());
        movePlayer.setPitch(target.pitch());
        movePlayer.setMode(PacketMovePlayer.MovePlayerMode.TELEPORT);
        movePlayer.setTick(0);
        this.send(movePlayer);

        // Store our current position
        this.currentPos = target;
    }

    private void destroy(String message) {
        LOGGER.error("CLIENT DESTROY: {}", message);
        this.socket.close();
    }

    private void moveToChunk(ChunkRequest chunkSquare) {
        BlockPosition targetPos = chunkSquare.getCenterPosition();
        Location target = new Location(null, targetPos.x(), targetPos.y(), targetPos.z(), 0f, 0f);
        this.move(target);
    }

    public void onDisconnect(Consumer<Void> disconnectConsumer) {
        this.disconnectConsumer = disconnectConsumer;
    }

    public ChunkRequest currentRequest() {
        return this.current;
    }

}
