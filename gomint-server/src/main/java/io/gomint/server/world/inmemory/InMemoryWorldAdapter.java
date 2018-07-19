/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.inmemory;

import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.GoMintServer;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.world.ChunkAdapter;
import io.gomint.server.world.ChunkCache;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.WorldCreateException;
import io.gomint.world.Chunk;
import io.gomint.world.generator.ChunkGenerator;
import io.gomint.world.generator.GeneratorContext;

import java.io.File;

/**
 * @author geNAZt
 * @version 1.0
 */
public final class InMemoryWorldAdapter extends WorldAdapter {

    private InMemoryWorldAdapter( GoMintServer server, String name, Class<? extends ChunkGenerator> generator ) throws WorldCreateException {
        super( server, new File( name ) );
        this.chunkCache = new ChunkCache( this );
        this.levelName = name;

        // Build up generator
        GeneratorContext context = new GeneratorContext();
        this.constructGenerator( generator, context );

        // Generate a spawnpoint
        BlockPosition spawnPoint = this.chunkGenerator.getSpawnPoint();
        this.spawn = new Location( this, spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ() );

        // Prepare spawn region
        this.prepareSpawnRegion();
    }

    @Override
    public ChunkAdapter loadChunk( int x, int z, boolean generate ) {
        ChunkAdapter chunkAdapter = this.chunkCache.getChunk( x, z );
        if ( chunkAdapter != null ) {
            return chunkAdapter;
        }

        if ( generate ) {
            return this.generate( x, z );
        }

        return null;
    }

    @Override
    protected void saveChunk( ChunkAdapter chunk ) {

    }

    @Override
    protected void closeFDs() {

    }

    @Override
    public boolean persistPlayer( EntityPlayer player ) {
        return false;
    }

    @Override
    public Chunk generateEmptyChunk( int x, int z ) {
        return new InMemoryChunkAdapter( this, x, z );
    }

    /**
     * Create a new in memory based world.
     *
     * @param server    which wants to create this world
     * @param name      of the new world
     * @param generator which is used to generate this worlds chunks and spawn point
     * @return new world
     * @throws WorldCreateException when there already is a world or a error during creating occured
     */
    public static InMemoryWorldAdapter create( GoMintServer server, String name, Class<? extends ChunkGenerator> generator ) throws WorldCreateException {
        return new InMemoryWorldAdapter( server, name, generator );
    }

}
