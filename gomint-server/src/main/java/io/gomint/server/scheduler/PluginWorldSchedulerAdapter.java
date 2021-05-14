/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.scheduler;

import com.google.common.base.Preconditions;
import io.gomint.scheduler.Task;
import io.gomint.scheduler.WorldScheduler;
import io.gomint.world.World;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @author Janmm14
 * @version 1.0
 */
public class PluginWorldSchedulerAdapter implements WorldScheduler {

    private PluginSchedulerAdapter pluginSchedulerAdapter;
    private final WeakReference<World> worldRef;

    /**
     * Create a new scheduler for the given plugin
     *
     * @param pluginSchedulerAdapter for which this scheduler is
     * @param world                  world for context of synchronized scheduler methods
     */
    public PluginWorldSchedulerAdapter(PluginSchedulerAdapter pluginSchedulerAdapter, World world) {
        this.pluginSchedulerAdapter = pluginSchedulerAdapter;
        this.worldRef = new WeakReference<>(world);
    }

    @Override
    public World world() {
        return this.worldRef.get();
    }

    private World checkAndGetWorld() {
        World world = world();
        Preconditions.checkNotNull(world, "world");
        Preconditions.checkState(world.isRunning(), "world is shutting down, cannot schedule anymore");
        return world;
    }

    @Override
    public Task executeAsync(Runnable runnable) {
        return this.pluginSchedulerAdapter.executeAsync(runnable);
    }

    @Override
    public Task scheduleAsync(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.pluginSchedulerAdapter.scheduleAsync(runnable, delay, timeUnit);
    }

    @Override
    public Task scheduleAsync(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.pluginSchedulerAdapter.scheduleAsync(runnable, delay, period, timeUnit);
    }

    @Override
    public Task execute(Runnable runnable) {
        return this.pluginSchedulerAdapter.execute(checkAndGetWorld(), runnable);
    }

    @Override
    public Task schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.pluginSchedulerAdapter.schedule(checkAndGetWorld(), runnable, delay, timeUnit);
    }

    @Override
    public Task schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return this.pluginSchedulerAdapter.schedule(checkAndGetWorld(), runnable, delay, period, timeUnit);
    }

}
