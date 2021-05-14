/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.inmemory;

import io.gomint.server.world.ChunkAdapter;
import io.gomint.server.world.WorldAdapter;

/**
 * @author geNAZt
 * @version 1.0
 */
class InMemoryChunkAdapter extends ChunkAdapter {

    public InMemoryChunkAdapter( WorldAdapter worldAdapter, int x, int z ) {
        super( worldAdapter, x, z );
        this.loadedTime = worldAdapter.currentTickTime();
    }

}
