/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.plugin;

import com.google.common.collect.ImmutableSet;
import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.entity.EntityPlayer;
import io.gomint.event.Event;
import io.gomint.event.EventListener;
import io.gomint.event.interfaces.WorldEvent;
import io.gomint.scheduler.Scheduler;
import io.gomint.world.World;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * Base class for any plugin to be created for use with the GoMint system. Below you will find an in-depth
 * explanation of the GoMint plugin system.
 *
 * When creating a plugin you should take care that the plugin implementation class directly inherits from
 * the base Plugin class. It is important that the implementation will not be considered a valid plugin
 * by GoNets plugin loader in case it only indirectly inherits from plugin. There are also several
 * annotation which are required on any plugin and which you need to specify under all circumstances so that
 * your plugin will actually be loaded. Namely, these are:
 * <ul>
 * <li>@Name( "NameOfPlugin" ) - The name of your plugin; for internal use only.</li>
 * <li>@Version( major = 1, minor = 0 ) - The current version of your plugin; for incremental updates.</li>
 * </ul>
 * Furthermore you may specify additional annotation as needed, for example a @Startup annotation to specify
 * upmost priority in loading the specified plugin.
 *
 * Any plugin will live through up to 4 different stages which will be explained in detail below. Please note
 * that only the very first stage is obligatory (assumed you have been able to put your - correct - implementation,
 * as shown above, into the right folder of the application), that is the DETECTION stage.
 * <ol>
 * <li>DETECTION - In this stage only the bare meta-information provided by annotations is actually loaded.</li>
 * <li>INSTALLATION - In this stage the classes of the plugin are loaded and the startup hook will be invoked.</li>
 * <li>RUNTIME - The main stage your plugin will (probably) be in most of the time (expect your plugin is full of shit nobody wants to install).</li>
 * <li>UNINSTALLATION - In this stage your plugin will be removed and - as the name obviously indicates - uninstalled.</li>
 * </ol>
 *
 * @author BlackyPaw
 * @author Janmm14
 * @version 1.0
 * @stability 3
 */
public class Plugin {

    /**
     * The plugin manager which controls the plugin's lifecycle. This is used to allow
     * plugins to determine their end on their own by disabling them. As this operation
     * needs to be done by the plugin manager each plugin gets a reference to it.
     */
    PluginManager pluginManager;

    /**
     * The name of the plugin as provided by annotation data.
     */
    String name;

    /**
     * The version of the plugin as provided by annotation data.
     */
    PluginVersion version;

    /**
     * Logger for this Plugin
     */
    Logger logger;

    /**
     * Scheduler for this Plugin
     */
    Scheduler scheduler;

    /**
     * Server which is running
     */
    GoMint server;

    /**
     * Configured loaded active worlds
     */
    @Nullable
    ConcurrentHashMap.KeySetView<String, Boolean> activeLoadedWorlds;

    /**
     * List which contains all listeners this plugin has registered
     */
    private final List<EventListener> listeners = new ArrayList<>();

    /**
     * List which contains all registered commands for this plugin
     */
    private final List<Command> commands = new ArrayList<>();

    /**
     * Implementation hook. This hook is invoked once the plugin is being installed.
     */
    public void onStartup() {
    }

    /**
     * Implementation hook. This hook is invoked once the plugin enters the runtime stage.
     */
    public void onInstall() {
    }

    /**
     * Implementation hook. This hook is invoked once the plugin is being uninstalled.
     */
    public void onUninstall() {
    }

    /**
     * Check plugin installation status.
     *
     * @return Plugin installation status
     */
    public final boolean isInstalled() {
        return this.pluginManager.isPluginInstalled(this.name);
    }

    /**
     * Register a new command to this plugin
     *
     * @param command which should be registered
     */
    public final Plugin registerCommand(Command command) {
        this.pluginManager.registerCommand(this, command);
        return this;
    }

    /**
     * Register a new event listener for this plugin.
     * <br><br>
     * Events implementing {@linkplain io.gomint.event.interfaces.WorldEvent WorldEvent} will be filtered, it will only
     * be called for events taking place in worlds which {@linkplain World#folder() folder name} is present in the
     * {@code worlds.yml} configuration.
     *
     * @param listener The listener which should be registered
     * @see #eventInActiveWorlds(Event)
     * @see #registerListener(EventListener, Predicate)
     * @since 2.0
     */
    public final Plugin registerListener(EventListener listener) {
        this.pluginManager.registerListener(this, listener);
        this.listeners.add(listener);
        return this;
    }

    /**
     * Register a new event listener for this plugin with an additional event filter.
     * <br><br>
     * Events implementing {@linkplain io.gomint.event.interfaces.WorldEvent WorldEvent} will be filtered, it will only
     * be called for events taking place in worlds which {@linkplain World#folder() folder name} is present in the
     * {@code worlds.yml} configuration.
     *
     * @param listener  The listener which should be registered
     * @param predicate The predicate allows you to filter events. Events where the predicate returns {@code false} are
     *                  not forwarded to the actual listener. {@code null} means that only the default active world
     *                  filtering will be done.
     * @see #registerListener(EventListener)
     * @since 2.0
     */
    public final Plugin registerListener(EventListener listener, Predicate<Event> predicate) {
        this.pluginManager.registerListener(this, listener, predicate);
        this.listeners.add(listener);
        return this;
    }

    /**
     * Unregister a listener
     *
     * @param listener which should be unregistered
     */
    public final Plugin unregisterListener(EventListener listener) {
        if (this.listeners.remove(listener)) {
            this.pluginManager.unregisterListener(this, listener);
        }

        return this;
    }

    /**
     * Disables the plugin if - and only if - the plugin is currently in the runtime stage.
     * Under all other circumstances invocation of this method will show no effect.
     */
    protected final void uninstall() {
        this.pluginManager.uninstallPlugin(this);
    }

    /**
     * Get a resource from within this plugins jar or container. Care must be
     * taken to close the returned stream.
     *
     * @param name the full path name of this resource
     * @return the stream for getting this resource, or null if it does not
     * exist
     */
    public final InputStream resourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * Gets the data folder where this plugin may store arbitrary data.
     *
     * @return the data folder of this plugin
     */
    public final File dataFolder() {
        return new File(pluginManager().baseDirectory(), name());
    }

    public final PluginManager pluginManager() {
        return this.pluginManager;
    }

    public final String name() {
        return this.name;
    }

    public final PluginVersion version() {
        return this.version;
    }

    public final Logger logger() {
        return this.logger;
    }

    public final Scheduler scheduler() {
        return this.scheduler;
    }

    public final GoMint server() {
        return this.server;
    }

    /**
     * Get a snapshot copy of loaded worlds (by {@linkplain World#folder()} name) based on {@code worlds.yml}
     * configuration file in this plugin's data folder.
     * <br>
     * <b>Do not save the result for later checking.</b>
     * <br>It only contains names of worlds which are currently loaded. In most cases it is enough to use
     * {@linkplain #activeInWorld(World)}, that method is also faster.
     * <br><br>
     * Returns {@code null} if plugin should run in all worlds.
     *
     * @return The set of currently loaded worlds (by {@linkplain World#folder()} name) the given plugin should be
     * active in. Returns {@code null} if plugin should run in all worlds.
     * @see #activeInWorld(World)
     * @see #eventInActiveWorlds(Event)
     */
    @Nullable
    public final Set<String> activeWorldsSnapshot() {
        return this.activeLoadedWorlds == null ? null : ImmutableSet.copyOf(this.activeLoadedWorlds);
    }

    /**
     * Checks whether the plugin should be active in the given world. Based on {@code worlds.yml} configuration file in
     * this plugin's data folder.
     *
     * @param world The world to check
     * @return whether the plugin should be active in the given world
     * @see #activeWorldsSnapshot()
     * @see #eventInActiveWorlds(Event)
     */
    public final boolean activeInWorld(World world) {
        return this.activeLoadedWorlds == null || this.activeLoadedWorlds.contains(world.folder());
    }

    /**
     * Checks whether given event is no {@linkplain WorldEvent} or if the event happens is in a world this plugin is
     * configured to be active in.
     *
     * @param event the event to check
     * @return {@code true} - event is no {@linkplain WorldEvent}<br>
     * {@code true} - event is {@linkplain WorldEvent} and plugin is configured to be active in it's world<br>
     * {@code false} - otherwise (event is {@linkplain WorldEvent} and plugin is configured to <b>not</b> be active in
     * it's world)
     * @see #activeWorldsSnapshot()
     * @see #activeInWorld(World)
     */
    public final boolean eventInActiveWorlds(Event event) {
        if (!(event instanceof WorldEvent)) {
            return true;
        }
        return activeInWorld(((WorldEvent) event).world());
    }

    /**
     * Get a collection of all players in worlds this plugin is active in
     *
     * @return collection of players in plugin's active worlds
     * @see GoMint#activeWorldsPlayers(Plugin)
     */
    public final Collection<EntityPlayer> activeWorldsPlayers() {
        return this.server.activeWorldsPlayers(this);
    }

    /**
     * Schedules iteration of players in this plugin's active worlds in their world's thread.
     *
     * @param playerConsumer the consumer which will get called on each world's thread with every player of the world
     * @return this plugin for chaining
     * @see GoMint#activeWorldsPlayers(Plugin, Consumer)
     */
    public final Plugin activeWorldsPlayers(Consumer<EntityPlayer> playerConsumer) {
        this.server.activeWorldsPlayers(this, playerConsumer);
        return this;
    }

}
