/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.test;

import io.gomint.server.world.WorldAdapter;
import io.gomint.world.World;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Janmm14
 * @version 1.0
 */
public class WorldTestUtil {
    
    private WorldTestUtil() {
        throw new UnsupportedOperationException();
    }
    
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
    
    public static void runInWorldThread(World w, ThrowingRunnable runnable) {
        WorldAdapter world = (WorldAdapter) w;
        if (world.mainThread()) {
            try {
                runnable.run();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            AtomicReference<Throwable> exception = new AtomicReference<>();
            world.syncScheduler().execute(() -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    countDownLatch.countDown();
                }
            });
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            final Throwable throwable = exception.get();
            if (throwable != null) {
                throw new RuntimeException(throwable);
            }
        }
    }
    
    public static void blockUntilWorldRuns(World w) {
        WorldAdapter world = (WorldAdapter) w;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        world.syncScheduler().execute(countDownLatch::countDown);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
