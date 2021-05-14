/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.generator.vanilla;

import com.google.common.util.concurrent.SettableFuture;
import io.gomint.math.BlockPosition;
import io.gomint.server.GoMintServer;
import io.gomint.server.async.Future;
import io.gomint.server.network.PostProcessExecutor;
import io.gomint.server.network.PostProcessExecutorService;
import io.gomint.server.util.XXHash;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.generator.vanilla.chunk.ChunkRequest;
import io.gomint.server.world.generator.vanilla.client.Client;
import io.gomint.world.Chunk;
import io.gomint.world.World;
import io.gomint.world.generator.GeneratorContext;
import io.gomint.world.generator.integrated.VanillaGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.PlatformEnum;
import oshi.SystemInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author geNAZt
 * @version 1.0
 */
public class VanillaGeneratorImpl extends VanillaGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VanillaGenerator.class);
    private static final String WINDOWS_DIST = "https://minecraft.azureedge.net/bin-win/bedrock-server-1.16.100.04.zip";
    private static final String LINUX_DIST = "https://minecraft.azureedge.net/bin-linux/bedrock-server-1.16.100.04.zip";

    // Chunk loading queue
    private final Queue<ChunkRequest> queue = new LinkedBlockingQueue<>();

    private int port;
    private long seed;

    private final GoMintServer server;

    private List<Client> client;
    private ProcessWrapper processWrapper;

    private boolean firstSeen = true;

    private SettableFuture<BlockPosition> spawnPointFuture = SettableFuture.create();
    private AtomicBoolean manualClose = new AtomicBoolean(false);

    /**
     * Create a new chunk generator
     *
     * @param world   for which this generator should generate chunks
     * @param context with which this generator should generate chunks
     */
    public VanillaGeneratorImpl(World world, GeneratorContext context) {
        super(world, context);

        // Eula check
        if (!Objects.equals(System.getProperty("eula.accepted"), "true")) {
            LOGGER.warn("============================================================================");
            LOGGER.warn(" You decided to use the vanilla generator. This needs to download the ");
            LOGGER.warn(" vanilla server binaries. Doing that means you automatically accept following ");
            LOGGER.warn(" terms and conditions: ");
            LOGGER.warn(" ");
            LOGGER.error(" Minecraft Terms: https://account.mojang.com/terms");
            LOGGER.error(" Microsoft Privacy: https://go.microsoft.com/fwlink/?LinkId=521839");
            LOGGER.warn(" ");
            LOGGER.error(" IF YOU ACCEPT PASS -Deula.accepted=true IN YOUR START SCRIPT");
            LOGGER.warn(" ");
            LOGGER.warn("============================================================================");

            try {
                System.in.read();
            } catch (IOException e) {
                // Ignored
            }

            System.exit(-1);
        }

        this.server = ((WorldAdapter) world).server();

        this.ensureSeed();

        // Generate temp in current dir
        File temp = new File("temp");
        if (!temp.exists()) {
            temp.mkdirs();
        }

        // Check if we have the dist temp
        File distFolder = new File(temp, "dist");
        if (!distFolder.exists()) {
            distFolder.mkdirs();
        }

        String urlToCheck;

        // Check if the vanilla server is there
        if (SystemInfo.getCurrentPlatformEnum() == PlatformEnum.WINDOWS) {
            urlToCheck = WINDOWS_DIST;
        } else {
            urlToCheck = LINUX_DIST;
        }

        // Split the url by its path and check if file is there
        String[] urlSplit = urlToCheck.split("/");
        String file = urlSplit[urlSplit.length - 1];

        File extracted = new File(distFolder, "extracted");
        if (!extracted.exists()) {
            extracted.mkdirs();
        }

        File localServerCopy = new File(distFolder, file);
        if (!localServerCopy.exists()) {
            try {
                FileUtils.copyURLToFile(new URL(urlToCheck), localServerCopy, 30000, 5000);
                this.unzip(localServerCopy, extracted);
            } catch (IOException e) {
                LOGGER.error("Could not download server binaries", e);
            }
        }

        boolean needsToCopy = false;

        // Ok we have a unpacked server now, we need to copy it over
        File tempServer = new File(temp, "server-" + world.folder());
        if (!tempServer.exists()) {
            tempServer.mkdirs();
            needsToCopy = true;
        } else {
            String executable = (SystemInfo.getCurrentPlatformEnum() == PlatformEnum.WINDOWS ?
                "bedrock_server.exe" :
                "bedrock_server");

            long hashDist = hashExecutable(extracted, executable);
            long hashCurrent = hashExecutable(tempServer, executable);

            if (hashDist != hashCurrent) {
                try {
                    FileUtils.deleteDirectory(tempServer);
                    tempServer.mkdirs();
                } catch (IOException e) {
                    LOGGER.error("Could not delete old world server", e);
                }

                needsToCopy = true;
            }
        }

        if (needsToCopy) {
            // Copy over template
            try {
                FileUtils.copyDirectory(extracted, tempServer);
            } catch (IOException e) {
                LOGGER.error("Could not copy from server template to world server directory", e);
            }
        }

        // We need to delete the world folder either way or the seed doesn't stick through
        try {
            FileUtils.deleteDirectory(new File(tempServer.getAbsolutePath(), "worlds"));
        } catch (IOException e) {
            LOGGER.error("Could not delete old world folder in temp server", e);
        }

        // Edit the world servers server.properties
        File serverProperties = new File(tempServer, "server.properties");
        try (FileInputStream inputStream = new FileInputStream(new File(extracted, "server.properties"))) {
            String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            // Replace the port
            String portLine = Arrays.stream(data.split("\n")).filter(line -> line.contains("server-port=")).collect(Collectors.joining());
            data = data.replace(portLine, "server-port=" + this.getRandomPort());
            String portV6Line = Arrays.stream(data.split("\n")).filter(line -> line.contains("server-portv6=")).collect(Collectors.joining());
            data = data.replace(portV6Line, "server-portv6=" + this.getRandomPort());

            // Disable online mode
            data = data.replace("online-mode=true", "online-mode=false");

            // Set default to creative
            data = data.replace("gamemode=survival", "gamemode=creative");

            // Set difficulty to peaceful
            data = data.replace("difficulty=easy", "difficulty=peaceful");

            // Set world seed
            data = data.replace("level-seed=", "level-seed=" + this.seed);

            // Set world seed
            data = data.replace("allow-cheats=false", "allow-cheats=true");

            // Set world seed
            data = data.replace("default-player-permission-level=member", "default-player-permission-level=operator");

            // Idle timer
            data = data.replace("player-idle-timeout=30", "player-idle-timeout=1440");

            // Disable server movement
            data = data.replace("server-authoritative-movement=server-auth", "server-authoritative-movement=client-auth");

            // Delete old version and write new one
            serverProperties.delete();
            try (FileOutputStream outputStream = new FileOutputStream(serverProperties)) {
                outputStream.write(data.getBytes());
            }
        } catch (IOException e) {
            LOGGER.error("Could not edit world servers server.properties", e);
        }

        // Prepare a starter if needed
        if (SystemInfo.getCurrentPlatformEnum() == PlatformEnum.LINUX) {
            String data = "#!/bin/bash\nLD_LIBRARY_PATH=. ./bedrock_server";
            File startSH = new File(tempServer, "start.sh");

            try (FileOutputStream outputStream = new FileOutputStream(startSH)) {
                outputStream.write(data.getBytes());
            } catch (IOException e) {
                LOGGER.error("Could now write bash script for starting BDS", e);
            }

            try {
                Files.setPosixFilePermissions(startSH.toPath(), PosixFilePermissions.fromString("rwxr--r--"));
            } catch (IOException e) {
                LOGGER.error("Could not set execute flag on bash script", e);
            }
        }

        // Start the server
        ProcessBuilder builder = new ProcessBuilder((SystemInfo.getCurrentPlatformEnum() == PlatformEnum.WINDOWS ?
            tempServer.getAbsolutePath() + File.separator + "bedrock_server.exe" :
            tempServer.getAbsolutePath() + File.separator + "start.sh"));
        builder.directory(new File(tempServer.getAbsolutePath()));

        try {
            this.processWrapper = new ProcessWrapper(builder, line -> {
                if (line.contains("IPv4 supported, port:") && this.firstSeen) {
                    String[] split = line.split(" ");
                    this.port = Integer.parseInt(split[split.length - 1]);
                    LOGGER.info("Server {} bound to {}", tempServer, this.port);
                    this.firstSeen = false;
                }

                if (line.contains("Server started.")) {
                    this.connect();
                }
            });
        } catch (IOException e) {
            LOGGER.error("Could not start BDS", e);
        }
    }

    private int getRandomPort() {
        try (DatagramSocket datagramSocket = new DatagramSocket(0)) {
            return datagramSocket.getLocalPort();
        } catch (SocketException e) {
            throw new RuntimeException("Could not open socket to find next free port", e);
        }
    }

    private long hashExecutable(File folder, String executable) {
        try (FileInputStream s = new FileInputStream(new File(folder.getAbsolutePath(), executable))) {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(s.available());
            buf.writeBytes(s.readAllBytes());

            long hash = XXHash.hash(buf, 0);
            buf.release();
            return hash;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void ensureSeed() {
        // Check if we have a seed
        if (this.context.contains("seed")) {
            this.seed = this.context.get("seed");
        } else {
            this.seed = ThreadLocalRandom.current().nextLong();
            this.context.put("seed", this.seed);
        }
    }

    private void connect() {
        WorldAdapter worldAdapter = (WorldAdapter) this.world;

        this.client = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 2; i++) {
            this.server.asyncScheduler()
                .scheduleAsync(() -> this.connectClient(worldAdapter), (i + 1) * 2, TimeUnit.SECONDS);
        }
    }

    private Client connectClient(WorldAdapter worldAdapter) {
        PostProcessExecutorService postProcessService = this.server.networkManager().postProcessService();
        PostProcessExecutor executor = postProcessService.getExecutor();
        Client newClient = new Client(worldAdapter, worldAdapter.server().encryptionKeyFactory(),
            this.queue, executor, null);

        newClient.onDisconnect(aVoid -> {
            postProcessService.releaseExecutor(executor);
            if (this.manualClose.get()) {
                return;
            }

            LOGGER.debug("Bot disconnected");

            ChunkRequest request = newClient.currentRequest();
            if (request != null) {
                LOGGER.debug("There was a request attached: {} / {}", request.x(), request.z());
                this.queue.offer(request);
            }

            this.client.remove(newClient);

            this.server.asyncScheduler().scheduleAsync(() -> {
                Client client = this.connectClient(worldAdapter);
                this.client.add(client);
            }, 500, TimeUnit.MILLISECONDS);
        });

        // Accept the spawn point
        newClient.spawnPointConsumer(blockPosition -> this.spawnPointFuture.set(blockPosition));
        newClient.connect("127.0.0.1", this.port);

        this.client.add(newClient);
        return newClient;
    }

    private void unzip(File fileZip, File destDir) throws IOException {
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);

                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    @Override
    public Chunk generate(int x, int z) {
        LOGGER.debug("Requesting chunk {} / {}", x, z);

        ChunkRequest request = new ChunkRequest(x, z, new Future<>());
        if (!this.queue.contains(request)) {
            LOGGER.debug("Offering for client to process");

            this.queue.offer(request);

            try {
                return request.future().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.debug("Already in queue");

            for (ChunkRequest chunkRequest : this.queue) {
                if (chunkRequest.equals(request)) {
                    try {
                        return chunkRequest.future().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    @Override
    public BlockPosition spawnPoint() {
        try {
            return this.spawnPointFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Could not get spawn point", e);
        }

        return null;
    }

    @Override
    public void populate(Chunk chunk) {

    }

    @Override
    public void close() {
        // Close all connections
        this.manualClose.set(true);

        for (Client client1 : this.client) {
            client1.disconnect("Closing chunk generator down");
        }

        this.processWrapper.kill();
    }

}
