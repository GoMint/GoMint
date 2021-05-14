/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.util;

import io.gomint.plugin.Plugin;
import io.gomint.server.plugin.PluginClassloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geNAZt
 * @version 1.0
 */
public class SecurityManagerCallerDetector implements CallerDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityManagerCallerDetector.class);
    private static final MySecurityManager SECURITY_MANAGER = new MySecurityManager();

    @Override
    public Class<? extends Plugin> getCallerPlugin() {
        return SECURITY_MANAGER.getCallerPlugin();
    }

    @Override
    public PluginClassloader getCallerPluginLoader() {
        return SECURITY_MANAGER.getCallerPluginLoader();
    }

    /**
     * A custom security manager that exposes the getClassContext() information
     */
    static class MySecurityManager extends SecurityManager {
        /**
         * Get the plugin who called
         *
         * @return class of the plugin who called
         */
        public Class<? extends Plugin> getCallerPlugin() {
            ClassLoader cls = Thread.currentThread().getContextClassLoader();
            if (cls instanceof PluginClassloader) {
                PluginClassloader cl = (PluginClassloader) cls;
                try {
                    return (Class<? extends Plugin>) cl.loadClass(cl.meta().mainClass());
                } catch (ClassNotFoundException e) {
                    LOGGER.error("Plugin class could not be found in its own classloader", e);
                }

                return null;
            }

            for (Class<?> aClass : getClassContext()) {
                // Get the class loader, if its a plugin one return the main class
                if (aClass.getClassLoader() instanceof PluginClassloader) {
                    PluginClassloader cl = (PluginClassloader) aClass.getClassLoader();

                    try {
                        return (Class<? extends Plugin>) cl.loadClass(cl.meta().mainClass());
                    } catch (ClassNotFoundException e) {
                        LOGGER.error("Plugin class could not be found in its own classloader", e);
                    }

                    return null;
                }
            }

            return null;
        }

        /**
         * Get the plugin who called
         *
         * @return class of the plugin who called
         */
        public PluginClassloader getCallerPluginLoader() {
            ClassLoader cls = Thread.currentThread().getContextClassLoader();
            if (cls instanceof PluginClassloader) {
                return (PluginClassloader) cls;
            }

            for (Class<?> aClass : getClassContext()) {
                // Get the class loader, if its a plugin one return the main class
                if (aClass.getClassLoader() instanceof PluginClassloader) {
                    return (PluginClassloader) aClass.getClassLoader();
                }
            }

            return null;
        }

    }

}
