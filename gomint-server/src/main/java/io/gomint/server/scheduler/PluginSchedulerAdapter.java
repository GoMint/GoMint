/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.scheduler;

import com.google.common.base.Preconditions;
import io.gomint.plugin.Plugin;
import io.gomint.scheduler.Scheduler;
import io.gomint.scheduler.Task;
import io.gomint.scheduler.WorldScheduler;
import io.gomint.server.world.WorldAdapter;
import io.gomint.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @author Janmm14
 * @version 1.0
 */
public class PluginSchedulerAdapter implements Scheduler {

    private Plugin plugin;
    private AsyncScheduler asyncScheduler;

    private Set<Task> runningTasks = Collections.synchronizedSet(new HashSet<>());
    private WeakHashMap<World, PluginWorldSchedulerAdapter> worldSchedulerAdapterCache = new WeakHashMap<>();

    /**
     * Create a new scheduler for the given plugin
     *
     * @param plugin         for which this scheduler is
     * @param asyncScheduler which schedules tasks inside gomint
     */
    public PluginSchedulerAdapter(Plugin plugin, AsyncScheduler asyncScheduler) {
        this.plugin = plugin;
        this.asyncScheduler = asyncScheduler;
    }

    @Override
    public Task executeAsync(Runnable runnable) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }

        Task task = this.asyncScheduler.executeAsync(runnable);
        task.onException(e -> {
            this.plugin.logger().warn("A task thrown a Exception", e);
            return true;
        });

        task.onComplete(() -> this.runningTasks.remove(task));

        this.runningTasks.add(task);
        return task;
    }

    @Override
    public Task scheduleAsync(Runnable runnable, long delay, TimeUnit timeUnit) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }

        Task task = this.asyncScheduler.scheduleAsync(runnable, delay, timeUnit);
        task.onException(e -> {
            this.plugin.logger().warn("A task thrown a Exception", e);
            return true;
        });

        task.onComplete(() -> this.runningTasks.remove(task));

        this.runningTasks.add(task);
        return task;
    }

    @Override
    public Task scheduleAsync(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }

        Task task = this.asyncScheduler.scheduleAsync(runnable, delay, period, timeUnit);
        task.onException(e -> {
            this.plugin.logger().warn("A task thrown a Exception", e);
            return true;
        });

        task.onComplete(() -> this.runningTasks.remove(task));

        this.runningTasks.add(task);
        return task;
    }

    private static void checkWorld(World world) {
        Preconditions.checkNotNull(world, "world");
        Preconditions.checkState(world.isRunning(), "world is shutting down, cannot schedule anymore");
    }

    @Override
    public Task execute(World world, Runnable runnable) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }
        checkWorld(world);

        Task task = ((WorldAdapter) world).syncScheduler().execute(runnable);
        task.onException(e -> {
            this.plugin.logger().warn("A task thrown a Exception", e);
            return true;
        });

        task.onComplete(() -> this.runningTasks.remove(task));

        this.runningTasks.add(task);
        return task;
    }

    @Override
    public Task schedule(World world, Runnable runnable, long delay, TimeUnit timeUnit) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }
        checkWorld(world);

        Task task = ((WorldAdapter) world).syncScheduler().schedule(runnable, delay, timeUnit);
        task.onException(e -> {
            this.plugin.logger().warn("A task thrown a Exception", e);
            return true;
        });

        task.onComplete(() -> this.runningTasks.remove(task));

        this.runningTasks.add(task);
        return task;
    }

    @Override
    public Task schedule(World world, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }
        checkWorld(world);

        Task task = ((WorldAdapter) world).syncScheduler().schedule(runnable, delay, period, timeUnit);
        task.onException(e -> {
            this.plugin.logger().warn("A task thrown a Exception", e);
            return true;
        });

        task.onComplete(() -> this.runningTasks.remove(task));

        this.runningTasks.add(task);
        return task;
    }

    @Override
    public WorldScheduler withWorld(World world) {
        if (this.asyncScheduler == null) {
            throw new IllegalStateException("This PluginScheduler has been cleaned and closed. No new Tasks can be scheduled");
        }
        checkWorld(world);
        return this.worldSchedulerAdapterCache.computeIfAbsent(world, w -> new PluginWorldSchedulerAdapter(this, w));
    }

    /**
     * Internal Method for cleaning up all Tasks
     */
    public void cleanup() {
        for (Task runningTask : new ArrayList<>(this.runningTasks)) {
            runningTask.cancel();
        }

        this.runningTasks.clear();
        this.plugin = null;
        this.asyncScheduler = null;
    }

}
