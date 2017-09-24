/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.plugin;

import io.gomint.command.Command;
import io.gomint.event.Event;
import io.gomint.event.EventListener;
import io.gomint.plugin.Plugin;
import io.gomint.plugin.PluginManager;
import io.gomint.plugin.PluginVersion;
import io.gomint.plugin.StartupPriority;
import io.gomint.server.GoMintServer;
import io.gomint.server.command.CommandManager;
import io.gomint.server.event.EventManager;
import io.gomint.server.scheduler.CoreScheduler;
import io.gomint.server.scheduler.PluginScheduler;
import io.gomint.server.util.CallerDetectorUtil;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author geNAZt
 * @version 1.0
 */
public class SimplePluginManager implements PluginManager {

    private static final Logger LOGGER = LoggerFactory.getLogger( SimplePluginManager.class );

    private final GoMintServer server;
    private final CoreScheduler scheduler;
    private final File pluginFolder;

    private final List<PluginMeta> detectedPlugins = new ArrayList<>();
    private final Map<String, Plugin> loadedPlugins = new LinkedHashMap<>();
    private final Map<String, Plugin> installedPlugins = new HashMap<>();
    private final Map<String, PluginMeta> metadata = new HashMap<>();

    private final EventManager eventManager = new EventManager();
    @Getter private final CommandManager commandManager = new CommandManager();

    private Field loggerField;
    private Field nameField;
    private Field pluginManagerField;
    private Field versionField;
    private Field schedulerField;
    private Field serverField;
    private Field listenerListField;

    public SimplePluginManager( GoMintServer server ) {
        this.server = server;
        this.scheduler = new CoreScheduler( server.getExecutorService(), server.getSyncTaskManager() );
        this.pluginFolder = new File( "plugins" );

        if ( !this.pluginFolder.exists() ) {
            this.pluginFolder.mkdirs();
        }

        // Prepare the field injections
        try {
            this.loggerField = Plugin.class.getDeclaredField( "logger" );
            this.loggerField.setAccessible( true );

            this.nameField = Plugin.class.getDeclaredField( "name" );
            this.nameField.setAccessible( true );

            this.pluginManagerField = Plugin.class.getDeclaredField( "pluginManager" );
            this.pluginManagerField.setAccessible( true );

            this.versionField = Plugin.class.getDeclaredField( "version" );
            this.versionField.setAccessible( true );

            this.schedulerField = Plugin.class.getDeclaredField( "scheduler" );
            this.schedulerField.setAccessible( true );

            this.serverField = Plugin.class.getDeclaredField( "server" );
            this.serverField.setAccessible( true );

            this.listenerListField = Plugin.class.getDeclaredField( "listeners" );
            this.listenerListField.setAccessible( true );
        } catch ( NoSuchFieldException e ) {
            e.printStackTrace();
        }
    }

    public void detectPlugins() {
        // Search the plugins folder for valid .jar files
        for ( File file : this.pluginFolder.listFiles( new FileFilter() {
            @Override
            public boolean accept( File pathname ) {
                return ( pathname.getAbsolutePath().endsWith( ".jar" ) );
            }
        } ) ) {
            PluginMeta metadata = getMetadata( file );
            if ( metadata != null ) {
                this.metadata.put( metadata.getName(), metadata );
                this.detectedPlugins.add( metadata );
            }
        }
    }

    public void loadPlugins( StartupPriority prio ) {
        LOGGER.debug( "Loading all plugins which have start priority: " + prio.name() );

        // Create a copy of the detected plugins
        for ( PluginMeta pluginMeta : new ArrayList<>( this.detectedPlugins ) ) {
            if ( !this.detectedPlugins.contains( pluginMeta ) ) {
                continue;
            }

            if ( pluginMeta.getPriority() == prio ) {
                LOGGER.debug( "Loading plugin " + pluginMeta.getName() );
                loadPlugin( pluginMeta );
            }
        }
    }

    private void loadPlugin( PluginMeta pluginMeta ) {
        // Check for depends
        if ( pluginMeta.getDepends() != null && !pluginMeta.getDepends().isEmpty() ) {
            for ( String dependPlugin : pluginMeta.getDepends() ) {
                // If the depend plugin is already loaded, skip it
                if ( this.loadedPlugins.containsKey( dependPlugin ) ) {
                    continue;
                }

                LOGGER.debug( "Searching depend for " + pluginMeta.getName() + ": " + dependPlugin );

                // We need to check if the depend plugin is detected
                boolean found = false;
                for ( PluginMeta detectedPlugin : new ArrayList<>( this.detectedPlugins ) ) {
                    if ( detectedPlugin.getName().equals( dependPlugin ) ) {
                        loadPlugin( detectedPlugin );
                        found = true;
                        break;
                    }
                }

                if ( !found ) {
                    LOGGER.warn( "Could not load plugin " + pluginMeta.getName() + " since the depend " + dependPlugin + " could not be found" );
                    this.metadata.remove( pluginMeta.getName() );
                    return;
                }
            }
        }

        // Check for soft depends
        if ( pluginMeta.getSoftDepends() != null && !pluginMeta.getSoftDepends().isEmpty() ) {
            for ( String dependPlugin : pluginMeta.getSoftDepends() ) {
                // If the depend plugin is already loaded, skip it
                if ( this.loadedPlugins.containsKey( dependPlugin ) ) {
                    continue;
                }

                // We need to check if the depend plugin is detected
                for ( PluginMeta detectedPlugin : new ArrayList<>( this.detectedPlugins ) ) {
                    if ( detectedPlugin.getName().equals( dependPlugin ) ) {
                        loadPlugin( pluginMeta );
                        break;
                    }
                }
            }
        }

        // Ok everything is fine now, load the plugin
        try {
            PluginClassloader loader = new PluginClassloader( new URL[]{
                    pluginMeta.getPluginFile().toURI().toURL()
            } );

            Class<?> main = loader.loadClass( pluginMeta.getMainClass() );
            Plugin clazz = (Plugin) main.getConstructor().newInstance();

            // Reflect the logger and stuff in
            this.loggerField.set( clazz, LoggerFactory.getLogger( main ) );
            this.pluginManagerField.set( clazz, this );
            this.schedulerField.set( clazz, new PluginScheduler( clazz, this.scheduler ) );
            this.nameField.set( clazz, pluginMeta.getName() );
            this.versionField.set( clazz, pluginMeta.getVersion() );
            this.serverField.set( clazz, this.server );

            clazz.onStartup();

            this.loadedPlugins.put( pluginMeta.getName(), clazz );
            this.detectedPlugins.remove( pluginMeta );
        } catch ( Exception e ) {
            LOGGER.warn( "Error whilst starting plugin " + pluginMeta.getName(), e );
            this.metadata.remove( pluginMeta.getName() );
        }
    }

    public void installPlugins() {
        this.loadedPlugins.forEach( new BiConsumer<String, Plugin>() {
            @Override
            public void accept( String name, Plugin plugin ) {
                try {
                    plugin.onInstall();
                    installedPlugins.put( name, plugin );
                } catch ( Exception e ) {
                    LOGGER.error( "Plugin did startup but could not be installed: " + name, e );
                    metadata.remove( plugin.getName() );
                }
            }
        } );

        this.loadedPlugins.clear();
    }

    private PluginMeta getMetadata( File file ) {
        // Open the jar
        try ( JarFile jar = new JarFile( file ) ) {
            Enumeration<JarEntry> jarEntries = jar.entries();

            // It seems like the jar is empty
            if ( jarEntries == null || !jarEntries.hasMoreElements() ) {
                LOGGER.warn( "Could not load Plugin. File " + file + " is empty" );
                return null;
            }

            // Try to read every file in the jar
            try {
                while ( jarEntries.hasMoreElements() ) {
                    JarEntry jarEntry = jarEntries.nextElement();

                    // When the entry is valid and ends with a .class its a java class and we need to scan it
                    if ( jarEntry != null && jarEntry.getName().endsWith( ".class" ) ) {
                        ClassFile classFile = new ClassFile( new DataInputStream( jar.getInputStream( jarEntry ) ) );

                        // Does this class extend the plugin class?
                        if ( classFile.getSuperclass().equals( "io.gomint.plugin.Plugin" ) ) {
                            String name = null;
                            PluginVersion version = null;
                            Set<String> depends = null;
                            Set<String> softDepends = null;
                            String startup = StartupPriority.STARTUP.name();

                            // Ok it did, time to parse the needed and optional annotations
                            AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute( AnnotationsAttribute.visibleTag );
                            for ( Annotation annotation : visible.getAnnotations() ) {
                                switch ( annotation.getTypeName() ) {
                                    case "io.gomint.plugin.Name":
                                        name = ( (StringMemberValue) annotation.getMemberValue( "value" ) ).getValue();
                                        break;

                                    case "io.gomint.plugin.Version":
                                        version = new PluginVersion( ( (IntegerMemberValue) annotation.getMemberValue( "major" ) ).getValue(),
                                                ( (IntegerMemberValue) annotation.getMemberValue( "minor" ) ).getValue() );
                                        break;

                                    case "io.gomint.plugin.Depends":
                                        MemberValue[] dependsValues = ( (ArrayMemberValue) annotation.getMemberValue( "value" ) ).getValue();
                                        depends = new HashSet<>();
                                        for ( MemberValue value : dependsValues ) {
                                            depends.add( ( (StringMemberValue) value ).getValue() );
                                        }
                                        break;

                                    case "io.gomint.plugin.Softdepends":
                                        dependsValues = ( (ArrayMemberValue) annotation.getMemberValue( "value" ) ).getValue();
                                        softDepends = new HashSet<>();
                                        for ( MemberValue value : dependsValues ) {
                                            softDepends.add( ( (StringMemberValue) value ).getValue() );
                                        }
                                        break;

                                    case "io.gomint.plugin.Startup":
                                        startup = ( (EnumMemberValue) annotation.getMemberValue( "value" ) ).getValue();
                                        break;
                                }
                            }

                            // We at least need the name and the version of the plugin
                            if ( name == null || version == null ) {
                                LOGGER.warn( "It seems like there is a plugin in the jar. But its missing the @Name or @Version annotation" );
                                return null;
                            }

                            return new PluginMeta( name, version, StartupPriority.valueOf( startup ), depends, softDepends, classFile.getName(), file );
                        }
                    }
                }

                return null;
            } catch ( IOException e ) {
                LOGGER.warn( "Could not load Plugin. File " + file + " is corrupted", e );
                return null;
            }
        } catch ( Exception ex ) {
            LOGGER.warn( "Could not load plugin from file " + file, ex );
            return null;
        }
    }

    @Override
    public void uninstallPlugin( Plugin plugin ) {
        // Check for security
        if ( !CallerDetectorUtil.getCallerPlugin().equals( plugin.getClass() ) ) {
            throw new SecurityException( "Plugins can only disable themselves" );
        }

        // Check if plugin is enabled
        if ( !this.installedPlugins.containsValue( plugin ) ) {
            return;
        }

        uninstallPlugin0( plugin );
    }

    private void uninstallPlugin0( Plugin plugin ) {
        // Check for plugins with hard depends
        new HashMap<>( this.metadata ).forEach( new BiConsumer<String, PluginMeta>() {
            @Override
            public void accept( String name, PluginMeta meta ) {
                if ( meta.getDepends() != null && meta.getDepends().contains( plugin.getName() ) ) {
                    Plugin pluginToUninstall = installedPlugins.get( name );
                    if ( pluginToUninstall != null ) {
                        uninstallPlugin0( pluginToUninstall );
                    }
                }
            }
        } );

        // Unregister listeners
        try {
            List<EventListener> listeners = (List<EventListener>) this.listenerListField.get( plugin );
            for ( EventListener listener : listeners ) {
                this.eventManager.unregisterListener( listener );
            }
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }

        // Cancel all tasks and cleanup scheduler
        try {
            PluginScheduler scheduler = (PluginScheduler) this.schedulerField.get( plugin );
            scheduler.cleanup();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }

        // CHECKSTYLE:OFF
        try {
            plugin.onUninstall();
        } catch ( Exception e ) {
            LOGGER.warn( "Plugin throw an exception whilst uninstalling: " + plugin.getName(), e );
        }
        // CHECKSTYLE:ON

        LOGGER.info( "Uninstalled plugin " + plugin.getName() );
        this.installedPlugins.remove( plugin.getName() );
        this.metadata.remove( plugin.getName() );
    }

    @Override
    public String getBaseDirectory() {
        return this.pluginFolder.getAbsolutePath();
    }

    @Override
    public <T extends Plugin> T getPlugin( String name ) {
        Plugin plugin = this.loadedPlugins.get( name );
        if ( plugin != null ) {
            return (T) plugin;
        }

        return (T) this.installedPlugins.get( name );
    }

    @Override
    public <T extends Event> T callEvent( T event ) {
        this.eventManager.triggerEvent( event );
        return event;
    }

    @Override
    public void registerListener( Plugin plugin, EventListener listener ) {
        if ( !plugin.getClass().getClassLoader().equals( listener.getClass().getClassLoader() ) ) {
            throw new SecurityException( "Wanted to register listener for another plugin" );
        }

        this.eventManager.registerListener( listener );
    }

    @Override
    public void unregisterListener( Plugin plugin, EventListener listener ) {
        if ( !plugin.getClass().getClassLoader().equals( listener.getClass().getClassLoader() ) ) {
            throw new SecurityException( "Wanted to unregister listener for another plugin" );
        }

        this.eventManager.unregisterListener( listener );
    }

    @Override
    public void registerCommand( Plugin plugin, Command command ) {
        if ( !CallerDetectorUtil.getCallerPlugin().equals( plugin.getClass() ) ) {
            throw new SecurityException( "Wanted to register command for another plugin" );
        }

        this.commandManager.register( plugin, command );
    }

    /**
     * Cleanup / uninstall all plugins
     */
    public void close() {
        for ( Plugin plugin : new ArrayList<>( this.loadedPlugins.values() ) ) {
            uninstallPlugin0( plugin );
        }
    }

}
