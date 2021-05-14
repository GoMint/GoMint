package io.gomint.server.plugin;

import io.gomint.GoMint;
import io.gomint.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PluginClassloader extends URLClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginClassloader.class);
    private static final Set<PluginClassloader> ALL_LOADERS = new CopyOnWriteArraySet<>();
    private static ClassLoader applicationClassloader;

    static {
        ClassLoader.registerAsParallelCapable();

        applicationClassloader = GoMint.class.getClassLoader();
    }

    public static String getPluginWhichLoaded(String name) {
        for (PluginClassloader loader : ALL_LOADERS) {
            try {
                loader.loadClass0(name, false, false);
                return loader.meta.name() + " v" + loader.meta.version().toString();
            } catch (ClassNotFoundException e) {
                // Ignored
            }
        }

        return null;
    }

    public static Class<?> find(String name) throws ClassNotFoundException {
        for (PluginClassloader loader : ALL_LOADERS) {
            try {
                return loader.loadClass(name, true);
            } catch (ClassNotFoundException e) {
                LOGGER.info("Could not find class in plugin");
            }
        }

        return applicationClassloader.loadClass(name);
    }

    private final PluginMeta meta;
    public Plugin instance;

    /**
     * Create a new plugin class loader
     *
     * @param meta which holds information about the plugin to be loaded
     * @throws MalformedURLException when the file is incorrectly labeled
     */
    PluginClassloader(PluginMeta meta) throws MalformedURLException {
        super(resolveURLs(meta));

        this.meta = meta;
        ALL_LOADERS.add(this);
    }

    private static URL[] resolveURLs(PluginMeta meta) throws MalformedURLException {
        URL[] loaderURLs = new URL[meta.moduleDependencies() != null ? meta.moduleDependencies().size() + 1 : 1];
        loaderURLs[0] = meta.pluginFile().toURI().toURL();
        if (meta.moduleDependencies() != null) {
            int index = 1;
            for (File moduleDependency : meta.moduleDependencies()) {
                loaderURLs[index++] = moduleDependency.toURI().toURL();
            }
        }

        return loaderURLs;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true);
    }

    private Class<?> loadClass0(String name, boolean resolve, boolean checkOther) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            // Ignored
        }

        if (checkOther) {
            for (PluginClassloader loader : ALL_LOADERS) {
                if (loader != this) {
                    try {
                        return loader.loadClass0(name, resolve, false);
                    } catch (ClassNotFoundException e) {
                        // Ignored
                    }
                }
            }

            try {
                return applicationClassloader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // Ignored
            }
        }

        throw new ClassNotFoundException(name);
    }

    /**
     * Remove the loader and free the resources loaded with it
     */
    public void remove() {
        ALL_LOADERS.remove(this);

        try {
            super.close();
        } catch (IOException e) {
            LOGGER.warn("Could not close plugin classloader", e);
        }
    }

    public PluginMeta meta() {
        return this.meta;
    }

}
