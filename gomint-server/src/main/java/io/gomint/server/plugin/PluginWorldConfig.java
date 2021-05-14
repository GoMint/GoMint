/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.plugin;

import io.gomint.config.YamlConfig;
import io.gomint.config.annotation.Comment;

import java.util.Set;

/**
 * {@code worlds.yml} config file of each plugin for plugin's active worlds
 *
 * @author Janmm14
 * @version 1.0
 */
public class PluginWorldConfig extends YamlConfig {

    @Comment("List of world folder names the plugin is active in")
    @Comment("An empty list always means that the plugin is active in all worlds. This ignores the isBlockList setting.")
    public Set<String> worldList;

    @Comment("Whether to treat the worldList as blockList")
    @Comment("Possible values:")
    @Comment(" - true: worldList is treated as blockList, plugin is active in all worlds NOT listed in worldList")
    @Comment(" - false: worldList is treated as allowList, plugin is only active in worlds listed in worldList")
    public boolean isBlockList = true;

}
