/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event;

/**
 * Importance of the {@link EventHandler} method. When executing an Event, the handlers
 * are called in order of their Priority:
 * <ol>
 * <li>LOWEST</li>
 * <li>LOW</li>
 * <li>NORMAL (default)</li>
 * <li>HIGH</li>
 * <li>HIGHEST</li>
 * </ol>
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public enum EventPriority {

    LOWEST((byte) -64),
    LOW((byte) -32),
    NORMAL((byte) 0),
    HIGH((byte) 32),
    HIGHEST((byte) 64);

    private byte order;

    EventPriority(final byte value) {
        this.order = value;
    }

    public byte value() {
        return this.order;
    }

}
