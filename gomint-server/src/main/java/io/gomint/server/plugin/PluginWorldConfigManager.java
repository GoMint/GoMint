/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.plugin;

import io.gomint.config.InvalidConfigurationException;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.world.WorldLoadEvent;
import io.gomint.event.world.WorldUnloadEvent;
import io.gomint.plugin.Plugin;
import io.gomint.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage plugin's active worlds configs
 *
 * @author Janmm14
 * @version 1.0
 */
public class PluginWorldConfigManager implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginWorldConfigManager.class);
    private final SimplePluginManager pluginManager;

    PluginWorldConfigManager(SimplePluginManager pluginManager) {
        this.pluginManager = pluginManager;
        pluginManager.eventManager().registerListener(this);
    }

    void onPluginLoad(PluginMeta meta, Plugin plugin) throws InvalidConfigurationException {
        PluginWorldConfig cfg = new PluginWorldConfig();
        cfg.load(new File(plugin.dataFolder(), "worlds.yml"));
        meta.pluginWorldConfig(cfg);
        if (cfg.worldList.isEmpty()) {
            if (!cfg.isBlockList) {
                LOGGER.warn("Active worlds of plugin " + meta.name() + " are configured as whitelist in worlds.yml, " +
                    "but world name list is empty. Plugin will be active in all worlds.");
            }
        } else {
            meta.activeLoadedWorlds(ConcurrentHashMap.newKeySet());
        }
        this.pluginManager.server().worlds().forEach(world -> {
            handleWorldLoad(world, meta);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onWorldLoad(WorldLoadEvent event) {
        this.pluginManager.pluginMetadatas().values().forEach(meta -> handleWorldLoad(event.world(), meta));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onWorldUnload(WorldUnloadEvent event) {
        this.pluginManager.pluginMetadatas().values().forEach(meta -> handleWorldUnload(event.world(), meta));
    }

    private void handleWorldLoad(World world, PluginMeta meta) {
        PluginWorldConfig cfg = meta.pluginWorldConfig();
        if (!cfg.isBlockList ^ cfg.worldList.contains(world.folder())) {
            ConcurrentHashMap.KeySetView<String, Boolean> activeWorlds = meta.activeLoadedWorlds();
            if (activeWorlds != null) {
                activeWorlds.add(world.folder());
            }
        }
    }

    private void handleWorldUnload(World world, PluginMeta pluginMeta) {
        PluginWorldConfig cfg = pluginMeta.pluginWorldConfig();
        if (!cfg.isBlockList ^ cfg.worldList.contains(world.folder())) {
            ConcurrentHashMap.KeySetView<String, Boolean> activeWorlds = pluginMeta.activeLoadedWorlds();
            if (activeWorlds != null) {
                activeWorlds.remove(world.folder());
            }
        }
    }

}
