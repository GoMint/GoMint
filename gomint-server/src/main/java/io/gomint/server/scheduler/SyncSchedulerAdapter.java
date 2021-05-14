package io.gomint.server.scheduler;

import io.gomint.scheduler.Task;

import java.util.concurrent.TimeUnit;

/**
 * @author Janmm14
 * @version 1.0
 */
public class SyncSchedulerAdapter {

    private final SyncTaskManager syncTaskManager;

    public SyncSchedulerAdapter(SyncTaskManager syncTaskManager) {
        this.syncTaskManager = syncTaskManager;
    }

    public Task execute(Runnable runnable) {
        return this.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    public Task schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.schedule(runnable, delay, -1, timeUnit);
    }

    public Task schedule(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        SyncScheduledTask task = new SyncScheduledTask(this.syncTaskManager, runnable, delay, period, timeUnit);
        this.syncTaskManager.addTask(task);
        return task;
    }

}
