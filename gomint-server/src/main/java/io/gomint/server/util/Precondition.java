/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.gomint.server.util;

import io.gomint.server.world.WorldAdapter;
import io.gomint.world.World;

import javax.annotation.Nullable;

/**
 * @author Janmm14
 * @version 1.0
 */
public class Precondition {

    private Precondition() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws {@linkplain UnsafeWorldAsyncAccessException} when given {@code world} is not null and its
     * {@linkplain WorldAdapter#mainThread(boolean)} method returns false.
     *
     * @param world            the world to enforce sync access to
     * @param allowForIOThread whether to allow async world io thread
     */
    public static void safeWorldAccess(@Nullable World world, boolean allowForIOThread) {
        if (world != null && !((WorldAdapter) world).mainThread(allowForIOThread)) {
            throw new UnsafeWorldAsyncAccessException(world.folder());
        }
    }

}
