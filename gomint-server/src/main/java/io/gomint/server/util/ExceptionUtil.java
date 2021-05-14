/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.util;

/**
 * @author Janmm14
 * @version 1.0
 */
public class ExceptionUtil {

    private ExceptionUtil() {
        throw new UnsupportedOperationException();
    }

    public static Exception wrap(Throwable t) {
        if (t instanceof Exception) {
            return (Exception) t;
        }
        return new WrappedThrowable(t);
    }

    public static class WrappedThrowable extends Exception {

        public WrappedThrowable(Throwable t) {
            super("Wrapped exception: " + t.getClass().getName() + ": " + t.getMessage(), t.getCause());
            setStackTrace(t.getStackTrace());
        }

    }

}
