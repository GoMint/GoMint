/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.scheduler;

import io.gomint.world.World;

import java.util.concurrent.TimeUnit;

/**
 * @author BlackyPaw
 * @author Janmm14
 * @version 2.0
 * @stability 2
 */
public interface Scheduler extends AsyncScheduler {

    /**
     * Execute the given runnable on the next tick of the given world.
     *
     * @param world    world
     * @param runnable which should be executed
     * @return the created and scheduled Task
     */
    Task execute(World world, Runnable runnable);

    /**
     * Executes a runnable with a delay. It gets scheduled to run on the given world's main thread. The time given is
     * not exactly taken, the task will run on the next tick of the given world after the delay time has elapsed.
     *
     * @param world    world
     * @param runnable which should be executed
     * @param delay    amount of timeUnit which should be used for waiting
     * @param timeUnit which should be used to multiply the delay
     * @return the created and scheduled Task
     */
    Task schedule(World world, Runnable runnable, long delay, TimeUnit timeUnit);

    /**
     * Executes a runnable repeatedly with a delay. It gets scheduled to run on the given world's main thread. The
     * time given is not exactly taken, the task will run on the next tick of the given world after the delay time has
     * elapsed.
     *
     * @param world    world
     * @param runnable which should be executed
     * @param delay    amount of timeUnit which should be used for waiting
     * @param period   amount of timeUnit which should be used for rescheduling the runnable
     * @param timeUnit which should be used to multiply the delay / period
     * @return the created and scheduled Task
     */
    Task schedule(World world, Runnable runnable, long delay, long period, TimeUnit timeUnit);

    /**
     * Get a {@linkplain WorldScheduler} with the world predefined for non-asynchronous schedule methods.
     * <br><br>
     * Do <b>not</b> safe the returned WorldScheduler, as worlds might get unloaded and then world scheduler will be
     * invalid.
     *
     * @param world the world
     * @return {@linkplain WorldScheduler} with the world predefined
     */
    WorldScheduler withWorld(World world);

}
