/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.scheduler;

import io.gomint.scheduler.Task;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @author Janmm14
 * @version 1.0
 */
public class AsyncScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger( AsyncScheduler.class );

    private final ScheduledExecutorService executorService;

    private final Object2LongMap<Thread> threads = new Object2LongOpenHashMap<>();
    private final Map<Thread, Runnable> threadRunnables = new HashMap<>();
    private final Set<Thread> alreadyAlerted = new HashSet<>();

    public AsyncScheduler( ScheduledExecutorService executorService ) {
        this.executorService = executorService;

        // Check for long execution timings
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        this.executorService.scheduleWithFixedDelay( () -> {
            long current = System.currentTimeMillis();

            synchronized (this.threads) {
                Object2LongMap.FastEntrySet<Thread> threadSet = (Object2LongMap.FastEntrySet<Thread>) this.threads.object2LongEntrySet();
                ObjectIterator<Object2LongMap.Entry<Thread>> threadIterator = threadSet.fastIterator();
                while ( threadIterator.hasNext() ) {
                    Object2LongMap.Entry<Thread> entry = threadIterator.next();

                    long diff = current - entry.getLongValue();
                    if ( diff > TimeUnit.SECONDS.toMillis( 10 ) ) {
                        ThreadInfo threadInfo = mxBean.getThreadInfo( entry.getKey().getId() );
                        Thread.State state = threadInfo.getThreadState();
                        if ( !this.alreadyAlerted.contains( entry.getKey() ) && ( state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING || state == Thread.State.BLOCKED ) ) {
                            LOGGER.warn( "Following runnable is blocking the scheduler loops: {}", this.threadRunnables.get( entry.getKey() ).getClass().getName() );

                            threadInfo = mxBean.getThreadInfo( entry.getKey().getId(), Integer.MAX_VALUE );
                            for ( StackTraceElement element : threadInfo.getStackTrace() ) {
                                LOGGER.warn( "  {}", element );
                            }

                            this.alreadyAlerted.add( entry.getKey() );
                        }
                    }
                }
            }
        }, 10, 10, TimeUnit.MILLISECONDS );
    }

    public Task executeAsync( Runnable runnable ) {
        return this.scheduleAsync( runnable, 0, TimeUnit.MILLISECONDS );
    }

    public Task scheduleAsync( Runnable runnable, long delay, TimeUnit timeUnit ) {
        return this.scheduleAsync( runnable, delay, -1, timeUnit );
    }

    private Runnable wrapRunnable( AsyncScheduledTask task ) {
        return () -> {
            long val = System.currentTimeMillis();

            synchronized (this.threads) {
                this.threadRunnables.put( Thread.currentThread(), task.runnable() );
                this.threads.put( Thread.currentThread(), val );
            }

            task.run();

            synchronized (this.threads) {
                this.threads.remove( Thread.currentThread(), val );
                this.threadRunnables.remove( Thread.currentThread(), task.runnable() );
                this.alreadyAlerted.remove( Thread.currentThread() );
            }
        };
    }

    public Task scheduleAsync( Runnable runnable, long delay, long period, TimeUnit timeUnit ) {
        AsyncScheduledTask task = new AsyncScheduledTask( runnable );

        Future<?> future;
        if ( period > 0 ) {
            future = this.executorService.scheduleAtFixedRate( wrapRunnable( task ), delay, period, timeUnit );
        } else if ( delay > 0 ) {
            future = this.executorService.schedule( wrapRunnable( task ), delay, timeUnit );
        } else {
            future = this.executorService.submit( wrapRunnable( task ) );
        }

        task.assignFuture( future );
        return task;
    }

    public boolean isShutdown() {
        return this.executorService.isShutdown();
    }

}
