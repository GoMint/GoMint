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
public class UnsafeWorldAsyncAccessException extends RuntimeException {

    public UnsafeWorldAsyncAccessException(String world) {
        super("Accessing or editing world data not from world's thread. Affected world: " + world);
    }

}
