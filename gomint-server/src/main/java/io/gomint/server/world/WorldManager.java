/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world;

import io.gomint.server.GoMintServer;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.util.SimpleUncaughtExceptionHandler;
import io.gomint.server.util.Values;
import io.gomint.server.world.generator.vanilla.VanillaGeneratorImpl;
import io.gomint.server.world.inmemory.InMemoryWorldAdapter;
import io.gomint.server.world.leveldb.LevelDBWorldAdapter;
import io.gomint.server.world.leveldb.ZippedLevelDBWorldAdapter;
import io.gomint.world.generator.CreateOptions;
import io.gomint.world.generator.integrated.VanillaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nonnull;

/**
 * @author BlackyPaw
 * @author geNAZt
 * @author Janmm14
 * @version 1.0
 */
public class WorldManager {

    private static final String THREAD_GROUP_NAME = "gomint-worlds";
    private static final String THREAD_PREFIX = "Gomint World Thread #";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldManager.class);
    private final GoMintServer server;
    private final Map<String, WorldAdapter> loadedWorlds;
    private final ThreadGroup threadGroup = new ThreadGroup(THREAD_GROUP_NAME);
    private final ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicLong counter = new AtomicLong(1);

        @Override
        public Thread newThread(@Nonnull Runnable r) {
            Thread thread = new Thread(WorldManager.this.threadGroup, r, THREAD_PREFIX + this.counter.getAndIncrement());
            thread.setUncaughtExceptionHandler(SimpleUncaughtExceptionHandler.INSTANCE);
            return thread;
        }
    });

    /**
     * Constructs a new world manager that does not yet hold any worlds.
     *
     * @param server The server for which this WorldManager handles world for
     */
    public WorldManager(GoMintServer server) {
        this.server = server;
        this.loadedWorlds = new ConcurrentHashMap<>();
    }

    /**
     * Gets a collection of all worlds held by the world manager.
     *
     * @return A collection of all worlds held by the world manager
     */
    public Collection<WorldAdapter> worlds() {
        return this.loadedWorlds.values();
    }

    /**
     * Gets the world with the given name out of the world manager if any such
     * world exists.
     *
     * @param name The name of the world to be retrieved
     * @return The world if found or null otherwise
     */
    public WorldAdapter world(String name) {
        return this.loadedWorlds.get(name);
    }

    public ExecutorService executorService() {
        return this.executorService;
    }

    /**
     * Adds the given world to the world manager. It may be retrieved by name
     * from now on.
     *
     * @param world The world to be added
     */
    private void addWorld(WorldAdapter world) {
        this.loadedWorlds.put(world.folder(), world);
        world.startThread();
    }

    /**
     * Loads the world from the specified path. This method will attempt to detect the
     * format of the world automatically. On success the world will be added to the
     * world manage immediately.
     *
     * @param path The path of the world
     * @return the world which has been loaded
     * @throws WorldLoadException Thrown in case the world could not be loaded
     */
    public WorldAdapter loadWorld(String path) throws WorldLoadException {
        File file = new File(path);

        // Check if we already loaded
        if (this.loadedWorlds.containsKey(file.getName())) {
            return this.loadedWorlds.get(file.getName());
        }

        LOGGER.info("Attempting to load world '{}'", path);

        if (file.exists()) {
            if (file.isDirectory()) {
                // LevelDB world:
                File dbFolder = new File(file, "db");
                if (dbFolder.exists() && dbFolder.isDirectory()) {
                    LOGGER.info("Detected leveldb world '{}'", path);
                    return this.loadLevelDBWorld(file);
                }
            } else {
                throw new WorldLoadException("World does not exist");
            }
        } else {
            File mcWorldFile = new File(file + ".mcworld");
            if (mcWorldFile.exists()) {
                LOGGER.info("Detected zipped leveldb world '{}'", path);
                return this.loadZippedLevelDBWorld(mcWorldFile, path);
            } else {
                throw new WorldLoadException("World does not exist");
            }
        }

        throw new WorldLoadException("Could not detect world format");
    }

    private WorldAdapter loadZippedLevelDBWorld(File path, String name) throws WorldLoadException {
        LevelDBWorldAdapter world = ZippedLevelDBWorldAdapter.load(this.server, path, name);
        this.addWorld(world);
        LOGGER.info("Successfully loaded world '{}'", name);
        return world;
    }

    private WorldAdapter loadLevelDBWorld(File path) throws WorldLoadException {
        LevelDBWorldAdapter world = LevelDBWorldAdapter.load(this.server, path);
        this.addWorld(world);
        LOGGER.info("Successfully loaded world '{}'", path.getName());
        return world;
    }

    /**
     * Close and save all worlds
     */
    public void close() {
        LOGGER.info("Closing all worlds");
        ArrayList<WorldAdapter> worlds = new ArrayList<>(this.worlds());
        CountDownLatch countDownLatch = new CountDownLatch(worlds.size());

        for (WorldAdapter loadedWorld : worlds) {
            if (!loadedWorld.isRunning()) {
                LOGGER.warn("World {} is not running, but still registered", loadedWorld);
                countDownLatch.countDown();
            } else {
                loadedWorld.unloadInternal(player -> player.disconnect("Server closed"),
                    () -> {
                        LOGGER.debug("World {} closed.", loadedWorld);
                        countDownLatch.countDown();
                    });
            }
        }
        LOGGER.debug("Waiting for worlds to close...");
        try {
            countDownLatch.await(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // Ignored .-.
        }
        LOGGER.debug("Shutting down world executor...");

        int wait = (int) Values.CLIENT_TICK_MS;
        this.executorService.shutdown();
        while (!this.executorService.isTerminated() && wait-- > 0) {
            try {
                this.executorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // Ignored .-.
            }
        }

        LOGGER.info("Shutdown of world executor completed");

        if (wait <= 0) {
            List<Runnable> remainRunning = this.executorService.shutdownNow();
            for (Runnable runnable : remainRunning) {
                LOGGER.warn("Runnable " + runnable.getClass().getName() + " has been terminated due to shutdown");
            }
        }

        if (wait <= 0) {
            List<Runnable> remainRunning = this.executorService.shutdownNow();
            for (Runnable runnable : remainRunning) {
                LOGGER.warn("Runnable " + runnable.getClass().getName() + " has been terminated due to shutdown");
            }
        }
        LOGGER.info("All worlds closed");
    }

    /**
     * Unload given world from RAM
     *
     * @param worldAdapter which should be unloaded
     */
    void unloadWorld(WorldAdapter worldAdapter) {
        this.loadedWorlds.remove(worldAdapter.folder());
    }

    /**
     * Create a new world
     *
     * @param name    of the new world
     * @param options with which this world should be generated
     * @return generated world
     */
    public WorldAdapter createWorld(String name, CreateOptions options) {
        // Check if we need to cascade a generator
        if (options.generator() == VanillaGenerator.class) {
            options.generator(VanillaGeneratorImpl.class);
        }

        // Check which type of world we want to create
        WorldAdapter world;
        switch (options.worldType()) {
            case PERSISTENT:
                try {
                    world = LevelDBWorldAdapter.create(this.server, name, options.generator());
                } catch (WorldCreateException e) {
                    LOGGER.error("Could not create new world", e);
                    return null;
                }

                break;

            case IN_MEMORY:
                try {
                    world = InMemoryWorldAdapter.create(this.server, name, options.generator());
                } catch (WorldCreateException e) {
                    LOGGER.error("Could not create new world", e);
                    return null;
                }

                break;

            default:
                return null;
        }
        this.addWorld(world);
        return world;
    }

}
