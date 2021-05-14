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
 * Do <b>not</b> safe instances of this class in your plugin. Worlds might get unloaded and make the instance invalid.
 *
 * @author Janmm14
 * @version 1.0
 * @stability 2
 */
public interface WorldScheduler extends AsyncScheduler {

    /**
     * Gets the world this scheduler will submit synchronized tasks to
     *
     * @return the world synchronized tasks will be associated to, might be null if the given world has been unloaded
     */
    World world();

    /**
     * Execute the given runnable on the next tick of the given world.
     *
     * @param runnable which should be executed
     * @return the created and scheduled Task
     */
    Task execute(Runnable runnable);

    /**
     * Executes a runnable with a delay. It gets scheduled to run on the given world's main thread. The time given is
     * not exactly taken, the task will run on the next tick of the given world after the delay time has elapsed.
     *
     * @param runnable which should be executed
     * @param delay    amount of timeUnit which should be used for waiting
     * @param timeUnit which should be used to multiply the delay
     * @return the created and scheduled Task
     */
    Task schedule(Runnable runnable, long delay, TimeUnit timeUnit);

    /**
     * Executes a runnable repeatedly with a delay. It gets scheduled to run on the given world's main thread. The
     * time given is not exactly taken, the task will run on the next tick of the given world after the delay time has
     * elapsed.
     *
     * @param runnable which should be executed
     * @param delay    amount of timeUnit which should be used for waiting
     * @param period   amount of timeUnit which should be used for rescheduling the runnable
     * @param timeUnit which should be used to multiply the delay / period
     * @return the created and scheduled Task
     */
    Task schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit);

}
