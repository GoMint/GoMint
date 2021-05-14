/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.util;

import io.gomint.plugin.Plugin;
import io.gomint.server.plugin.PluginClassloader;

/**
 * @author Fabian
 * @version 1.0
 */
public interface CallerDetector {

    /**
     * Get the plugin which called
     *
     * @return class of plugin
     */
    Class<? extends Plugin> getCallerPlugin();
    /**
     * Get the plugin loader of the plugin which called
     *
     * @return class loader of plugin
     */
    PluginClassloader getCallerPluginLoader();

}



