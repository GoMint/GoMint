/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.inmemory;

import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.GoMintServer;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.util.Precondition;
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

    private final Class<? extends ChunkGenerator> generator;

    public InMemoryWorldAdapter(GoMintServer server, String name, Class<? extends ChunkGenerator> generator) throws WorldCreateException {
        super( server, new File( name ), name );
        this.chunkCache = new ChunkCache( this );
        this.levelName = name;
        this.generator = generator;

        this.prepareGenerator();

        // Generate a spawnpoint
        BlockPosition spawnPoint = this.chunkGenerator.spawnPoint();
        this.spawn = new Location( this, spawnPoint.x(), spawnPoint.y(), spawnPoint.z() );

        // Prepare spawn region
        this.prepareSpawnRegion();
    }

    @Override
    public ChunkAdapter loadChunk( int x, int z, boolean generate ) {
        Precondition.safeWorldAccess(this, true);
        ChunkAdapter chunkAdapter = this.chunkCache.getChunk( x, z );
        if ( chunkAdapter != null ) {
            return chunkAdapter;
        }

        if ( generate ) {
            return this.generate( x, z, false );
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
    protected void prepareGenerator() {
        try {
            // Build up generator
            GeneratorContext context = new GeneratorContext();
            this.constructGenerator( this.generator, context );
        } catch ( WorldCreateException e ) {
            this.logger.error( "Could not construct generator", e );
        }
    }

    @Override
    public void persistPlayer( EntityPlayer player ) {

    }

    @Override
    public boolean loadPlayer(EntityPlayer player) {
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
