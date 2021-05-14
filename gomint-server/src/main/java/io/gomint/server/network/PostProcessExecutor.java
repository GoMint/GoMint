/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.network;

import io.gomint.scheduler.Task;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.scheduler.AsyncScheduler;
import io.gomint.server.util.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PostProcessExecutor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger( PostProcessExecutor.class );

    private final AtomicInteger connectionsInUse = new AtomicInteger( 0 );
    private Queue<PostProcessWorker> workers = new ConcurrentLinkedQueue<>();
    private float load;
    private Task future;
    private AtomicBoolean running = new AtomicBoolean( true );
    private final Object waiter = new Object();
    private final AsyncScheduler executorService;

    public PostProcessExecutor( AsyncScheduler executorService ) {
        this.executorService = executorService;
        this.future = executorService.executeAsync( this );
    }

    public float load() {
        return this.load;
    }

    public AtomicInteger connectionsInUse() {
        return this.connectionsInUse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostProcessExecutor that = (PostProcessExecutor) o;
        return Float.compare(that.load, this.load) == 0 &&
            Objects.equals(this.connectionsInUse, that.connectionsInUse) &&
            Objects.equals(this.workers, that.workers) &&
            Objects.equals(this.future, that.future) &&
            Objects.equals(this.running, that.running) &&
            Objects.equals(this.waiter, that.waiter) &&
            Objects.equals(this.executorService, that.executorService);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.connectionsInUse, this.workers, this.load, this.future, this.running, this.waiter, this.executorService);
    }

    public void addWork(ConnectionWithState connection, Packet[] packets, Consumer<Void> callback) {
        this.workers.offer( new PostProcessWorker( connection, packets, callback ) );

        synchronized ( this.waiter ) {
            this.waiter.notifyAll();
        }
    }

    public void stop() {
        this.running.set( false );
        this.future.cancel();
    }

    @Override
    public void run() {
        while ( this.running.get() && !this.executorService.isShutdown() ) {
            long start = System.currentTimeMillis();

            while ( !this.workers.isEmpty() ) {
                PostProcessWorker worker = this.workers.poll();
                if ( worker != null ) {
                    try {
                        worker.run();
                    } catch ( Throwable t ) {
                        t.printStackTrace();
                    }
                }
            }

            this.load = ( ( System.currentTimeMillis() - start ) / Values.CLIENT_TICK_MS ) * 100;
            if ( this.load > 60 ) {
                LOGGER.debug( "Post processor load > 60%: {}", this.load );
            }

            // Wait on the next worker
            if ( this.workers.isEmpty() ) {
                synchronized ( this.waiter ) {
                    try {
                        this.waiter.wait( 500 );
                    } catch ( InterruptedException e ) {
                        // Ignored
                    }
                }
            }
        }
    }

}
