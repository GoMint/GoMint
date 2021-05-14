/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.i18n;

import io.gomint.i18n.localization.ResourceLoadFailedException;
import io.gomint.i18n.localization.ResourceLoader;
import io.gomint.i18n.localization.ResourceManager;
import io.gomint.i18n.localization.loader.PropertiesResourceLoader;
import io.gomint.i18n.localization.loader.YamlResourceLoader;
import io.gomint.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class LocaleManager {

    private static final Logger LOGGER = LoggerFactory.getLogger( LocaleManager.class );

    // The ResourceManager to use for this LocaleManager
    private ResourceManager resourceManager;

    // The fallback Locale to use
    private Locale defaultLocale = Locale.US;

    // Plugin for which we have this
    private final Plugin plugin;

    /**
     * Construct a new LocaleManager for this Plugin
     *
     * @param plugin The plugin for which this LocaleManager should be loaded
     */
    public LocaleManager( Plugin plugin ) {
        this.plugin = plugin;

        this.resourceManager = new ResourceManager( plugin.getClass().getClassLoader() );
        this.resourceManager.registerLoader( new PropertiesResourceLoader() );
        this.resourceManager.registerLoader( new YamlResourceLoader() );

        // Load stuff from the plugin jar if possible
        try {
            URL jarFile = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            String filePath = jarFile.toExternalForm();
            if ( filePath.startsWith( "file:/" ) ) {
                try ( JarFile openJarFile = new JarFile( filePath.substring( 6 ) ) ) {
                    Enumeration<JarEntry> jarEntryEnumeration = openJarFile.entries();
                    while ( jarEntryEnumeration.hasMoreElements() ) {
                        JarEntry entry = jarEntryEnumeration.nextElement();
                        if ( !entry.isDirectory() ) {
                            // We for sure don't support .class locales
                            String name = entry.getName();
                            if ( !name.endsWith( ".class" ) ) {
                                // remove file ending
                                String[] folderSplit = name.split( "/" );
                                String last = folderSplit[folderSplit.length - 1];
                                int lastDotIndex = last.lastIndexOf( '.' );
                                last = last.substring( 0, lastDotIndex );

                                if ( !last.contains( "_" ) ) {
                                    continue;
                                }

                                String[] localeSplit = last.split( "_" );
                                if ( localeSplit.length != 2 ) {
                                    continue;
                                }

                                Locale locale = new Locale( localeSplit[0], localeSplit[1] );
                                load( locale, name );
                            }
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            // Ignore
        }
    }

    /**
     * Gets the list of available locales from the specified file.
     *
     * @param path The path of the file to query.
     * @return A list of supported locales as well as their meta-information or null on faillure.
     */
    public List<Locale> availableLocales(File path ) {
        File[] files = path.listFiles();
        if ( files == null ) return null;

        List<Locale> supported = new ArrayList<>();
        for ( File file : files ) {
            String[] locale = file.getName().substring( 0, 5 ).split( "_" );
            supported.add( new Locale( locale[0], locale[1] ) );
        }

        return supported;
    }

    /**
     * Init / Load all Locales which could be found in the given spec file. This refreshes the languages all 5 minutes
     *
     * @param path The path of the file to query.
     */
    public LocaleManager initFromLocaleFolder( final File path ) {
        initFromLocaleFolderWithoutAutorefresh( path );
        this.plugin.scheduler().scheduleAsync(() -> initFromLocaleFolderWithoutAutorefresh( path ), 5, 5, TimeUnit.MINUTES );
        return this;
    }

    /**
     * Init / Load all Locales which could be found in the given spec file.
     *
     * @param path The path of the file to query.
     */
    public LocaleManager initFromLocaleFolderWithoutAutorefresh( File path ) {
        File[] files = path.listFiles();
        if ( files == null ) return this;

        for ( File file : files ) {
            String[] locale = file.getName().substring( 0, 5 ).split( "_" );

            try {
                load( new Locale( locale[0], locale[1] ), "file://" + file.getAbsolutePath() );
            } catch ( ResourceLoadFailedException e ) {
                LOGGER.warn( "Could not load i18n file {}", file.getAbsolutePath(), e );
            }
        }

        return this;
    }

    /**
     * Load a new Locale into the ResourceManager for this Plugin
     *
     * @param locale Locale which should be loaded
     * @param param  The param which should be given to the ResourceLoader
     * @throws ResourceLoadFailedException if the loading has thrown any Error
     */
    public synchronized LocaleManager load( Locale locale, String param ) throws ResourceLoadFailedException {
        this.resourceManager.load( locale, param );
        return this;
    }

    /**
     * Gets the correct String out of the Locale. If the locale given is not loaded by the underlying ResourceManager
     * it takes the set default Locale to read the String from.
     *
     * @param locale Locale which should be read for
     * @param key    The key which should be looked up
     * @return The String stored in the ResourceLoader
     * @throws ResourceLoadFailedException If the Resource was cleared out and could not be reloaded into the Cache
     */
    private String translation(Locale locale, String key ) throws ResourceLoadFailedException {
        return this.resourceManager.get( locale, key );
    }

    /**
     * Check if the given Locale has been loaded by the ResourceManager. If not return the default Locale
     *
     * @param locale Locale which should be checked
     * @return The default locale or the param
     */
    private Locale checkForDefault( Locale locale ) {
        if ( !this.resourceManager.isLoaded( locale ) ) {
            return this.defaultLocale;
        }

        return locale;
    }

    /**
     * Change the default Locale for this plugin.
     * It must be loaded before a Locale can be set as default.
     *
     * @param locale Locale which should be used as default Fallback
     * @return locale manager for chaining
     */
    public LocaleManager defaultLocale(Locale locale ) {
        this.defaultLocale = locale;
        return this;
    }

    /**
     * Translate the Text based on the locale.
     * If the locale is not loaded the LocaleManager will try to load it, if this fails
     * it will use the default Locale. If this is also not loaded you will get a ResourceNotLoadedException
     *
     * @param locale         Locale which should be used to translate
     * @param translationKey The key in the ResourceLoader which should be translated
     * @param args           The Arguments which will be passed into the String when translating
     * @return The translated String
     */
    public String translate( Locale locale, String translationKey, Object... args ) {
        //Get the resource and translate
        Locale playerLocale = checkForDefault( locale );

        String translationString = null;
        try {
            translationString = translation( playerLocale, translationKey );
        } catch ( ResourceLoadFailedException e ) {
            try {
                translationString = translation( playerLocale = this.defaultLocale, translationKey );
            } catch ( ResourceLoadFailedException e1 ) {
                // Ignore .-.
            }
        }

        // Check for untranslated messages
        if ( translationString == null ) {
            return "N/A (" + translationKey + ")";
        }

        MessageFormat msgFormat = new MessageFormat( translationString );
        msgFormat.setLocale( playerLocale );
        return msgFormat.format( args );
    }

    /**
     * Translate the Text based on the Player locale / default Locale.
     * If the locale from the player is not loaded the LocaleManager
     * will use the default Locale. If this is also not loaded it
     * will use the translationKey as text and give it back
     *
     * @param translationKey The key in the ResourceLoader which should be translated
     * @param args           The Arguments which will be passed into the String when translating
     * @return The translated String
     */
    public String translate( String translationKey, Object... args ) {
        //Get the resource and translate
        String translationString = null;
        try {
            translationString = translation(this.defaultLocale, translationKey );
        } catch ( ResourceLoadFailedException e ) {
            // Ignore .-.
        }

        if ( translationString == null ) {
            LOGGER.warn( "The key({}) is not present in the Locale {}", translationKey, this.defaultLocale);
            return "N/A (" + translationKey + ")";
        }

        MessageFormat msgFormat = new MessageFormat( translationString );
        msgFormat.setLocale(this.defaultLocale);
        return msgFormat.format( args );
    }

    /**
     * Register a new custom ResourceLoader. See {@link ResourceManager#registerLoader(ResourceLoader)}
     *
     * @param loader which is used to load specific locale resources
     */
    public LocaleManager registerLoader( ResourceLoader<?> loader ) {
        this.resourceManager.registerLoader( loader );
        return this;
    }

    /**
     * Gets a list of all loaded Locales
     *
     * @return Unmodifiable List
     */
    public List<Locale> loadedLocales() {
        return Collections.unmodifiableList(this.resourceManager.getLoadedLocales() );
    }

    /**
     * Tells the ResourceManager to reload all Locale Resources which has been loaded by this Plugin
     */
    public synchronized LocaleManager reload() {
        this.resourceManager.reload();
        return this;
    }

    /**
     * Be sure to remove resources loaded and to remove refs
     */
    public synchronized void cleanup() {
        this.resourceManager.cleanup();
        this.resourceManager = null;
    }

    public Locale defaultLocale() {
        return this.defaultLocale;
    }

}
