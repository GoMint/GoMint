/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.scheduler;

import io.gomint.server.GoMintServer;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * @author geNAZt
 * @version 1.0
 */
public class SyncTaskManager {

    private final PriorityQueue<SyncScheduledTaskHolder> taskList = new PriorityQueue<>( Comparator.comparingLong( o -> o.execution ) );

    /**
     * Add a new pre configured Task to this scheduler
     *
     * @param task which should be executed
     */
    public SyncTaskManager addTask( SyncScheduledTask task ) {
        if ( task.getNextExecution() == -1 ) return this;

        synchronized ( this.taskList ) {
            this.taskList.add( new SyncScheduledTaskHolder( task.getNextExecution(), task ) );
        }
        return this;
    }

    /**
     * Remove a specific task
     *
     * @param task The task which should be removed
     */
    SyncTaskManager removeTask( SyncScheduledTask task ) {
        synchronized ( this.taskList ) {
            this.taskList.remove( new SyncScheduledTaskHolder( -1, task ) );
        }
        return this;
    }

    /**
     * Update and run all tasks which should be run
     *
     * @param currentMillis The amount of millis when the update started
     */
    public SyncTaskManager update( long currentMillis ) {
        synchronized ( this.taskList ) {
            // Iterate over all Tasks until we find some for later ticks
            while ( this.taskList.peek() != null && this.taskList.peek().execution < currentMillis ) {
                SyncScheduledTaskHolder holder = this.taskList.poll();
                if ( holder == null ) {
                    return this;
                }

                SyncScheduledTask task = holder.task;
                if ( task == null ) {
                    return this;
                }

                // Check for abort value ( -1 )
                if ( task.getNextExecution() == -1 ) {
                    continue;
                }

                task.run();

                // Reschedule if needed
                if ( task.getNextExecution() > currentMillis ) {
                    this.taskList.add( new SyncScheduledTaskHolder( task.getNextExecution(), task ) );
                }
            }
        }
        return this;
    }
    
    public SyncTaskManager clear() {
        this.taskList.clear();
        return this;
    }

    private static class SyncScheduledTaskHolder {
        private long execution;
        private SyncScheduledTask task;

        public SyncScheduledTaskHolder(long execution, SyncScheduledTask task) {
            this.execution = execution;
            this.task = task;
        }

        public long getExecution() {
            return this.execution;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SyncScheduledTaskHolder that = (SyncScheduledTaskHolder) o;
            return Objects.equals(this.task, that.task);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.task);
        }
    }

}
