/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.network;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import io.gomint.server.scheduler.AsyncScheduler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @version 1.0
 * <p>
 * This is needed since a single thread can only handle ~150 players on a ~4.2 ghz ish 6700k. To handle more player than
 * that we need more threads to decompress stuff. Problem is we need to do this in order for each connection so we can't
 * use normal executor services because then packets would get out of order. We need to ping users to certain executors.
 */
public class PostProcessExecutorService implements Runnable {

    private AsyncScheduler executorService;
    private List<PostProcessExecutor> executors = new CopyOnWriteArrayList<>();

    public PostProcessExecutorService( AsyncScheduler executorService ) {
        this.executorService = executorService;
        this.executorService.scheduleAsync( this, 0, 10, TimeUnit.MILLISECONDS );

        this.executors.add( new PostProcessExecutor( this.executorService ) );
    }

    public PostProcessExecutor getExecutor() {
        int amountOfConnections = -1;
        PostProcessExecutor selectedExecutor = null;

        // Select anything under the given threshold
        for ( PostProcessExecutor executor : this.executors ) {
            if ( executor.load() < 85 ) {
                if ( executor.connectionsInUse().get() > amountOfConnections ) {
                    selectedExecutor = executor;
                    amountOfConnections = executor.connectionsInUse().get();
                }
            }
        }

        if ( selectedExecutor == null ) {
            PostProcessExecutor executor = new PostProcessExecutor( this.executorService );
            executor.connectionsInUse().incrementAndGet();
            this.executors.add( executor );
            return executor;
        } else {
            selectedExecutor.connectionsInUse().incrementAndGet();
            return selectedExecutor;
        }
    }

    public void releaseExecutor( PostProcessExecutor executor ) {
        executor.connectionsInUse().decrementAndGet();
    }

    @Override
    public void run() {
        int canKill = this.executors.size() - 1;
        if ( canKill == 0 ) {
            return;
        }

        // Check if we can get rid of some old unused post process workers
        for ( PostProcessExecutor executor : this.executors ) {
            if ( executor.connectionsInUse().get() == 0 && canKill-- > 0 ) {
                executor.stop();
            }
        }
    }

}
