/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.util;

import io.gomint.server.GoMintServer;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @version 1.0
 */
public class Watchdog implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Watchdog.class);
    private final Long2LongMap watchdogMap;
    private final Long2LongMap removed;

    public Watchdog(GoMintServer server) {
        this.watchdogMap = new Long2LongOpenHashMap();
        this.removed = new Long2LongOpenHashMap();

        server.asyncScheduler().scheduleAsync(this, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void run() {
        long currentTime = System.currentTimeMillis();

        final LongSet[] removeSet = {null};
        for (Long2LongMap.Entry entry : this.watchdogMap.long2LongEntrySet()) {
            // Check if we are over endTime
            if (currentTime > entry.getLongValue()) {
                // Get the threads stack
                ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
                ThreadInfo threadInfo = threadMXBean.getThreadInfo(entry.getLongKey(), 10);
                if (threadInfo == null) {
                    handling: {
                        for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
                            Thread thread = e.getKey();
                            if (thread.getId() == entry.getLongKey()) {
                                LOGGER.warn("Thread did not work in time and not found in ThreadMXBean: {} (#{})",
                                    thread.getName(), thread.getId());
                                LOGGER.warn("Status: {}", thread.getState());
                                for (StackTraceElement element : e.getValue()) {
                                    LOGGER.warn("  {}", element);
                                }
                                break handling;
                            }
                        }
                        LOGGER.warn("Thread did not work in time, but is dead now: id = #{}, lastMillis = {}", entry.getLongKey(),
                            entry.getLongValue());
                    }
                } else {
                    LOGGER.warn("Thread did not work in time: {} (#{})", threadInfo.getThreadName(),
                        threadInfo.getThreadId());
                    LOGGER.warn("Status: {}", threadInfo.getThreadState());
                    for (StackTraceElement element : threadInfo.getStackTrace()) {
                        LOGGER.warn("  {}", element);
                    }
                }

                if (removeSet[0] == null) {
                    removeSet[0] = new LongOpenHashSet();
                }

                removeSet[0].add(entry.getLongKey());
            }
        }

        if (removeSet[0] != null) {
            for (long threadId : removeSet[0]) {
                this.removed.put(threadId, this.watchdogMap.remove(threadId));
            }
        }
    }

    public synchronized void add(long currentTime, long diff, TimeUnit unit) {
        this.watchdogMap.put(Thread.currentThread().getId(), currentTime + unit.toMillis(diff));
    }

    public void add(long diff, TimeUnit unit) {
        long currentTime = System.currentTimeMillis();
        this.add(currentTime, diff, unit);
    }

    public synchronized void done() {
        long threadId = Thread.currentThread().getId();
        this.watchdogMap.remove(threadId);

        if (this.removed.containsKey(threadId)) {
            LOGGER.info("Thread {} took {}ms", threadId, (System.currentTimeMillis() - this.removed.remove(threadId)));
        }
    }

}
