/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.maintenance;

import backtrace.io.BacktraceClient;
import backtrace.io.BacktraceConfig;
import io.gomint.GoMint;
import io.gomint.entity.EntityPlayer;
import io.gomint.math.Location;
import io.gomint.server.GoMintServer;
import io.gomint.server.maintenance.report.PlayerReportData;
import io.gomint.server.maintenance.report.WorldData;
import io.gomint.server.plugin.PluginClassloader;
import io.gomint.server.world.WorldAdapter;
import oshi.SystemInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public final class ReportUploader {

    private final Map<String, Object> context = new HashMap<>();

    private Map<String, WorldData> worlds = new HashMap<>();
    private Map<String, PlayerReportData> players = new HashMap<>();
    private Exception exception = null;

    private ReportUploader() {
        this.context.put("java_version", System.getProperty("java.vm.name") + " (" + System.getProperty("java.runtime.version") + ")");

        // Ask for OS and CPU info
        SystemInfo systemInfo = new SystemInfo();
        this.context.put("system.os", systemInfo.getOperatingSystem().getFamily() + " [" + systemInfo.getOperatingSystem().getVersionInfo().getVersion() + "]");
        this.context.put("system.memory", getCount(systemInfo.getHardware().getMemory().getTotal()));
        this.context.put("system.cpu", systemInfo.getHardware().getProcessor().getProcessorIdentifier().getName());

        // Basic process stats
        this.context.put("system.process_memory_total", getCount(Runtime.getRuntime().totalMemory()));
        this.context.put("system.process_memory_free", getCount(Runtime.getRuntime().freeMemory()));

        this.context.put("system.current_thread", Thread.currentThread().getName());
    }

    private static String getCount(long bytes) {
        // Do we need to check for suffix
        if (bytes < 1024) {
            return bytes + " B";
        }

        // Get exp and get the correct suffix
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %siB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Include world data
     *
     * @return the report uploader for chaining
     */
    public ReportUploader includeWorlds() {
        GoMintServer server = (GoMintServer) GoMint.instance();
        for (WorldAdapter adapter : server.worldManager().worlds()) {
            this.worlds.put(adapter.folder(), new WorldData(adapter.chunkCache().size()));
        }

        return this;
    }

    /**
     * Include player data
     *
     * @return the report uploader for chaining
     */
    public ReportUploader includePlayers() {
        for (EntityPlayer player : GoMint.instance().onlinePlayers()) {
            String key = player.name() + ":" + player.uuid().toString();
            Location location = player.location();
            this.players.put(key, new PlayerReportData(location.world().folder(), location.x(), location.y(), location.z()));
        }

        return this;
    }

    /**
     * Convert a exception to a string and append it to the report
     *
     * @param exception which should be reported
     * @return the report uploader for chaining
     */
    public ReportUploader exception(Exception exception) {
        this.exception = exception;
        return this;
    }

    /**
     * Add a new property to this upload request
     *
     * @param key   of the property
     * @param value of the property
     * @return the report uploader for chaining
     */
    public ReportUploader property(String key, String value) {
        this.context.put("property." + key, value);
        return this;
    }

    public void upload() {
        this.upload(null);
    }

    /**
     * Upload this report
     *
     * @param message which should be used if no additional data has been given
     */
    public void upload(String message) {
        // Check if reporting has been disabled
        GoMintServer server = (GoMintServer) GoMint.instance();
        if (server.version().contains("dev/unsupported")) {
            return;
        }

        this.context.put("config.server", server.serverConfig());

        if (!this.worlds.isEmpty()) {
            this.worlds.forEach((worldName, worldData) -> this.context.put("world." + worldName, worldData));
        }

        if (!this.players.isEmpty()) {
            this.players.forEach((playerName, playerData) -> this.context.put("player." + playerName, playerData));
        }

        // Check for plugin crashes
        BacktraceConfig config = new BacktraceConfig("https://submit.backtrace.io/gomint/f66ad8232bdb6e07049c878ceb95b9d8bc74a2eeba663de5135d73d8b0db91ff/json");
        BacktraceClient client = new BacktraceClient(config);
        client.setApplicationName("GoMint");
        client.setApplicationVersion(((GoMintServer) GoMint.instance()).gitHash());

        // Check how we need to send data
        if (this.exception != null) {
            client.setBeforeSendEvent(data -> {
                for (StackTraceElement traceElement : this.exception.getStackTrace()) {
                    String plugin = PluginClassloader.getPluginWhichLoaded(traceElement.getClassName());
                    if (plugin != null) {
                        data.getAttributes().put("tag.plugin.crash", "true");
                        data.getAttributes().put("plugin", plugin);
                    }
                }

                return data;
            });

            client.send(this.exception);
        } else {
            client.send(message == null ? "Nulled message" : message);
        }
    }

    /**
     * Create a new report uploader
     *
     * @return a report uploader
     */
    public static ReportUploader create() {
        return new ReportUploader();
    }

    public ReportUploader tag(String tag) {
        this.context.put("tag." + tag, "true");
        return this;
    }

}
