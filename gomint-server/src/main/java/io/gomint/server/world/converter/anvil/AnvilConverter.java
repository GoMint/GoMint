package io.gomint.server.world.converter.anvil;

import io.gomint.server.GoMintServer;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.util.BlockIdentifier;
import io.gomint.server.world.converter.BaseChunkConverter;
import io.gomint.server.world.converter.BaseConverter;
import io.gomint.taglib.NBTTagCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author geNAZt
 * @version 1.0
 */
public class AnvilConverter extends BaseConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger( AnvilConverter.class );
    private static final int DATAVERSION_v1_13 = 1519;

    private BaseChunkConverter chunkConverter;

    private boolean nukkitPMMPConverted = false;

    private int dataVersion;

    public AnvilConverter( GoMintServer server, File worldFolder ) {
        super( worldFolder );

        File backupFolder = new File( worldFolder, "backup" );
        if ( !backupFolder.exists() ) {
            backupFolder.mkdir();
        }

        File alreadyConverted = new File( worldFolder, "ALREADY_CONVERTED" );
        this.nukkitPMMPConverted = alreadyConverted.exists();

        List<File> foldersToBeRemoved = new ArrayList<>();
        String parentFolder = worldFolder.toPath().toString();
        try ( Stream<Path> stream = Files.walk( worldFolder.toPath() ) ) {
            stream.forEach( path -> {
                String moveFile = path.toString().replace( parentFolder, "" );
                if ( moveFile.length() <= 1
                    || moveFile.substring( 1 ).startsWith( "backup" )
                    || moveFile.substring( 1 ).startsWith( "db" ) ) {
                    return;
                }

                File curFile = path.toFile();
                if ( curFile.isDirectory() ) {
                    File newFolderBackup = new File( backupFolder, moveFile );
                    newFolderBackup.mkdir();

                    foldersToBeRemoved.add( curFile );
                } else {
                    try {
                        Files.move( path, new File( backupFolder, moveFile ).toPath(), StandardCopyOption.ATOMIC_MOVE );
                    } catch ( IOException e ) {
                        LOGGER.error( "Could not move data into backup", e );
                    }
                }
            } );
        } catch ( IOException e ) {
            LOGGER.error( "Could not move data into backup", e );
        }

        for ( File file : foldersToBeRemoved ) {
            file.delete();
        }

        this.dataVersion = readDataVersion();

        if(dataVersion >= DATAVERSION_v1_13){
            chunkConverter = null;
        }else if(dataVersion == 0){ //dataVerion 0 = no dataVersion found, because it support 1.9+
            chunkConverter = new ChunkConverter1_8( server );
        }

        // Convert all region files first
        File regionFolder = new File( backupFolder, "region" );
        if ( regionFolder.exists() ) {
            convertRegionFiles( regionFolder );
        }
    }

    private void convertRegionFiles( File regionFolder ) {
        File[] regionFiles = regionFolder.listFiles( ( dir, name ) -> name.endsWith( ".mca" ) );
        if ( regionFiles == null ) {
            return;
        }

        AtomicLong amountOfChunksDone = new AtomicLong( 0 );
        ConcurrentLinkedQueue<NBTTagCompound> compounds = new ConcurrentLinkedQueue<>();
        AtomicBoolean readFinished = new AtomicBoolean( false );

        int useCores = Math.floorDiv( Runtime.getRuntime().availableProcessors(), 2 );

        ExecutorService service = Executors.newFixedThreadPool( useCores, r -> {
            Thread thread = new Thread( r );
            thread.setName( "Gomint - World converter" );
            return thread;
        } );

        // Start all threads
        for ( int i = 0; i < useCores; i++ ) {
            service.execute( () -> {
                while ( !readFinished.get() || !compounds.isEmpty() ) {
                    if ( compounds.isEmpty() ) {
                        try {
                            Thread.sleep( 5 );
                        } catch ( InterruptedException e ) {
                            // Ignored
                        }

                        continue;
                    }

                    doConvert( amountOfChunksDone, compounds.poll() );
                }

                finish();
            } );
        }

        // Iterate over all region files and check if they match the pattern
        long start = System.nanoTime();
        for ( File regionFile : regionFiles ) {
            String fileName = regionFile.getName();
            if ( fileName.startsWith( "r." ) ) {
                String[] split = fileName.split( "\\." );
                if ( split.length != 4 ) {
                    continue;
                }

                try {
                    RegionFileSingleChunk regionFileReader = new RegionFileSingleChunk( regionFile );

                    for ( int x = 0; x < 32; x++ ) {
                        for ( int z = 0; z < 32; z++ ) {
                            if ( compounds.size() > 400 ) { // Throttle when the converter threads are behind
                                try {
                                    Thread.sleep( 20 );
                                } catch ( InterruptedException e ) {
                                    // Ignore
                                }
                            }

                            NBTTagCompound compound = regionFileReader.loadChunk( x, z );
                            if ( compound == null ) {
                                continue;
                            }

                            compounds.offer( compound );
                        }
                    }
                } catch ( IOException e ) {
                    LOGGER.error( "Could not convert region file: {}", fileName, e );
                }
            }
        }

        // Set read to finish and tell the executors to shutdown when they are done
        readFinished.set( true );
        service.shutdown();

        // Wait for service to shutdown, help with converting if needed
        while ( !service.isTerminated() ) {
            if ( !compounds.isEmpty() ) {
                doConvert( amountOfChunksDone, compounds.poll() );
            } else {
                try {
                    service.awaitTermination( 500, TimeUnit.MILLISECONDS );
                } catch ( InterruptedException e ) {
                    // Ignore
                }
            }
        }

        // Persist stuff from this thread
        finish();

        // Make a level.dat
        try {
            File backupFolder = new File( this.worldFolder, "backup" );
            NBTTagCompound levelDat = NBTTagCompound.readFrom( new File( backupFolder, "level.dat" ), true, ByteOrder.BIG_ENDIAN );
            NBTTagCompound dataCompound = levelDat.getCompound( "Data", false );

            try ( FileOutputStream fileOutputStream = new FileOutputStream( new File( this.worldFolder, "level.dat" ) ) ) {
                fileOutputStream.write( new byte[8] );

                NBTTagCompound levelDBDat = new NBTTagCompound( "" );
                levelDBDat.addValue( "SpawnX", dataCompound.getInteger( "SpawnX", 0 ) );
                levelDBDat.addValue( "SpawnY", dataCompound.getInteger( "SpawnY", 0 ) );
                levelDBDat.addValue( "SpawnZ", dataCompound.getInteger( "SpawnZ", 0 ) );
                levelDBDat.addValue( "StorageVersion", 8 );
                levelDBDat.addValue( "LevelName", dataCompound.getString( "LevelName", "converted-gomint" ) );
                levelDBDat.writeTo( fileOutputStream, false, ByteOrder.LITTLE_ENDIAN );
            }
        } catch ( IOException e ) {
            LOGGER.error( "Could not convert level.dat", e );
        }

        // Performance output
        long needed = TimeUnit.NANOSECONDS.toMillis( System.nanoTime() - start );
        LOGGER.info( "Done in {} ms - Processed {} subchunks - {} subchunks/s", needed, amountOfChunksDone.get(), ( 1000 / ( needed / (double) amountOfChunksDone.get() ) ) );
    }

    private void doConvert( AtomicLong amountOfChunksDone, NBTTagCompound compound ) {
        if ( compound == null ) {
            return;
        }

        NBTTagCompound levelCompound = compound.getCompound( "Level", false );

        int dataVersion = compound.getInteger("DataVersion", 0);

        if(this.dataVersion != dataVersion){
            LOGGER.error("Could not convert Chunk: {}" , compound);
            return;
        }

        int chunkX = levelCompound.getInteger( "xPos", 0 );
        int chunkZ = levelCompound.getInteger( "zPos", 0 );

        this.startChunk( chunkX, chunkZ );

        List<Object> sections = levelCompound.getList( "Sections", false );
        for ( Object section : sections ) {
            NBTTagCompound sectionCompound = (NBTTagCompound) section;
            this.readAndConvertSubchunk( chunkX, chunkZ, sectionCompound );
            amountOfChunksDone.incrementAndGet();
        }

        List<Object> tileEntities = levelCompound.getList( "TileEntities", false );
        if ( tileEntities != null && tileEntities.size() > 0 ) {
            List<TileEntity> newTileEntities = new ArrayList<>();

            for ( Object entity : tileEntities ) {
                NBTTagCompound tileCompound = (NBTTagCompound) entity;
                TileEntity tileEntity = chunkConverter.convertTileEntity( tileCompound );
                if ( tileEntity == null ) {
                    LOGGER.warn( "Could not convert tile entity: {}", tileCompound );
                } else {
                    newTileEntities.add( tileEntity );
                }
            }

            this.storeTileEntities( chunkX, chunkZ, newTileEntities );
        }

        this.persistChunk();
    }

    private void readAndConvertSubchunk( int chunkX, int chunkZ, NBTTagCompound section ) {
        int sectionY = section.getByte( "Y", (byte) 0 );
        BlockIdentifier[] newBlocks = chunkConverter.convertChunkSection(chunkX, chunkZ, section);
        this.storeSubChunkBlocks( sectionY, chunkX, chunkZ, newBlocks );
    }

    private int readDataVersion(){
        File backupFolder = new File( this.worldFolder, "backup" );
        NBTTagCompound levelDat = null;
        try {
            levelDat = NBTTagCompound.readFrom( new File( backupFolder, "level.dat" ), true, ByteOrder.BIG_ENDIAN );
            NBTTagCompound dataCompound = levelDat.getCompound( "Data", false );
            return dataCompound.getInteger("DataVerion", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
