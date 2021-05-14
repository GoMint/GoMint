/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Janmm14
 * @version 1.0
 */
public class SimpleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUncaughtExceptionHandler.class);

    public static final SimpleUncaughtExceptionHandler INSTANCE = new SimpleUncaughtExceptionHandler();

    private SimpleUncaughtExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Uncaught exception occurred in thread #" + t.getId(), e);
    }

}
