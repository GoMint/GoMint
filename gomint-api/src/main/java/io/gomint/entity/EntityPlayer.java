/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.entity;

import io.gomint.command.CommandOutput;
import io.gomint.entity.passive.EntityHuman;
import io.gomint.gui.Form;
import io.gomint.gui.FormListener;
import io.gomint.inventory.EnderChestInventory;
import io.gomint.inventory.Inventory;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.permission.PermissionManager;
import io.gomint.player.ChatType;
import io.gomint.player.DeviceInfo;
import io.gomint.scoreboard.Scoreboard;
import io.gomint.world.Gamemode;
import io.gomint.world.Particle;
import io.gomint.world.ParticleData;
import io.gomint.world.Sound;
import io.gomint.world.SoundData;

import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * @author BlackyPaw
 * @author Digot
 * @version 1.0
 * @stability 3
 */
public interface EntityPlayer extends EntityHuman<EntityPlayer> {

    /**
     * Set the new gamemode for this player
     *
     * @param gamemode The new gamemode to be used
     */
    EntityPlayer gamemode(Gamemode gamemode );

    /**
     * Get the current gamemode
     *
     * @return The gamemode the player is in
     */
    Gamemode gamemode();

    /**
     * Is this player op'ed?
     *
     * @return true when op'ed, false when not
     */
    boolean op();

    /**
     * Set the player's operator status
     *
     * @param value true when op, false otherwise
     */
    EntityPlayer op(boolean value );

    /**
     * Hide another player from this player
     *
     * @param player The player which should be hidden
     */
    EntityPlayer hidePlayer( EntityPlayer player );

    /**
     * Show a hidden player again
     *
     * @param player The player which should be shown again
     */
    EntityPlayer showPlayer( EntityPlayer player );

    /**
     * Check if given player is hidden to this player
     *
     * @param player The player which should be checked for
     * @return true if this player can't see the player given, false it it can
     */
    boolean isHidden( EntityPlayer player );

    /**
     * Opens a inventory for the player
     *
     * @param inventory which should be opened
     * @return true when the client has opened the inventory, false when not
     */
    boolean openInventory(Inventory<?> inventory );

    /**
     * Close the given inventory
     *
     * @param inventory which should be closed
     * @return true when inventory has been close, false otherwise
     */
    boolean closeInventory(Inventory<?> inventory );

    /**
     * Send a message to the client, this uses the normal {@link ChatType} enum.
     *
     * @param message which should be send to the client
     */
    EntityPlayer sendMessage( String message );

    /**
     * Send a message with a given type to the client
     *
     * @param message which should be send
     * @param type    of the message
     */
    EntityPlayer sendMessage( ChatType type, String... message );

    /**
     * Get the view distance of this player
     *
     * @return radius of chunks this player can see
     */
    int viewDistance();

    /**
     * Transfer player to another server
     *
     * @param host IP or Hostname for the user to connect to
     * @param port Of the new Server
     */
    EntityPlayer transfer( String host, int port );

    /**
     * Return the network latency
     *
     * @return network latency in ms
     */
    int ping();

    /**
     * Display new form and get a listener for the response
     *
     * @param form which should be shown
     * @param <R>  type of return value from the response
     * @return form listener to attaching for response
     */
    <R> FormListener<R> showForm( Form<R> form );

    /**
     * Set the server settings form
     *
     * @param form which will be set as the new settings form
     * @param <R>  type of return value from the response
     * @return form listener to attaching for response
     */
    <R> FormListener<R> settingsForm(Form<R> form );

    /**
     * Remove the current stored settings form
     */
    EntityPlayer removeSettingsForm();

    /**
     * Get the players permission manager
     *
     * @return permission manager
     */
    PermissionManager permissionManager();

    /**
     * Set the players permission manager
     *
     * This will print out a warning about replacing the permission manager. Since gomint does not hold
     * the API contract anymore this warning simply shows that the API may not behave like the contract says
     * it would.
     *
     * If you use a custom permission manager ensure that it calls {@link #sendCommands()} when permissions change
     * so the client gets its commands corrected.
     *
     * @param permissionManager for this player
     */
    EntityPlayer permissionManager(PermissionManager permissionManager);

    /**
     * Is this player still online?
     *
     * @return true if online, false if not
     */
    boolean online();

    /**
     * Locale of this player
     *
     * @return locale of the players client
     */
    Locale locale();

    /**
     * Disconnect a player for the given reason
     *
     * @param reason for disconnect
     */
    EntityPlayer disconnect( String reason );

    /**
     * Get absolute amount of xp in this entity
     *
     * @return absolute amount of xp
     */
    int xp();

    /**
     * Percentage of xp for next level
     *
     * @return percentage of next level
     */
    float xpPercentage();

    /**
     * Set the amount of xp this entity has
     *
     * @param xp of this entity
     */
    EntityPlayer xp(int xp );

    /**
     * Get exp level of this entity
     *
     * @return exp level
     */
    int level();

    /**
     * Set the level of the exp bar
     *
     * @param level of this entity
     */
    EntityPlayer level(int level );

    /**
     * Play a sound for this player
     *
     * @param location of the sound in the client
     * @param sound    The sound which should be played
     * @param pitch    The pitch at which the sound should be played
     * @param data     additional data for the sound
     */
    EntityPlayer playSound(Vector location, Sound sound, byte pitch, SoundData data );

    /**
     * Play a sound for this player
     *
     * @param location of the sound in the client
     * @param sound    The sound which should be played
     * @param pitch    The pitch at which the sound should be played
     */
    EntityPlayer playSound( Vector location, Sound sound, byte pitch );

    /**
     * Send a particle to this player
     *
     * @param location of the particle in the client
     * @param particle which should be send
     */
    EntityPlayer sendParticle( Vector location, Particle particle );

    /**
     * Send a particle to this player
     *
     * @param location of the particle in the client
     * @param particle which should be send
     * @param data     which should be used to construct additional data needed to display the particle
     */
    EntityPlayer sendParticle( Vector location, Particle particle, ParticleData data );

    /**
     * Allow flying for the client
     *
     * @param value if true the client can fly, if false the client can't fly
     */
    EntityPlayer allowFlight(boolean value );

    /**
     * Get the setting for allowing flight
     *
     * @return true when the player can fly, false when not
     */
    boolean allowFlight();

    /**
     * Set flying state of the player
     *
     * @param value if true the player is flying, if false the player doesn't fly
     */
    EntityPlayer flying(boolean value );

    /**
     * Check if this player is flying
     *
     * @return true when flying otherwise false
     */
    boolean flying();

    /**
     * Send a title text to the user's screen, with an optional subtitle.
     *
     * @param title    Big text displayed in the middle of the screen
     * @param subtitle Smaller text displayed below the title text
     * @param fadein   duration for the fade in effect
     * @param duration which is used for how long the title should be shown
     * @param fadeout  duration for the fade out effect
     * @param unit     of duration multiplier
     */
    EntityPlayer sendTitle( String title, String subtitle, long fadein, long duration, long fadeout, TimeUnit unit );

    /**
     * Send a title without subtitle.
     *
     * @param title Big text displayed in the middle of the screen
     */
    EntityPlayer sendTitle( String title );

    /**
     * Send a title with title and subtitle.
     *
     * @param title    Big text displayed in the middle of the screen
     * @param subtitle Smaller text displayed below the title text
     *                 <p>
     *                 Default time for fadein and duration is 1 second
     */
    EntityPlayer sendTitle( String title, String subtitle );

    /**
     * Toggle gliding status of the player
     *
     * @param value true for gliding, false for not gliding
     */
    EntityPlayer gliding(boolean value );

    /**
     * Is the player currently gliding?
     *
     * @return true when gliding, false when not
     */
    boolean gliding();

    /**
     * Get information about the device the player is using
     *
     * @return device information from this player
     */
    DeviceInfo deviceInfo();

    /**
     * Get the socket address from the connection of this player
     *
     * @return the socket address of this player
     */
    InetSocketAddress address();

    /**
     * Dispatch a command for this player. Will be scheduled in world's thread, if the current thread is not the world's
     * thread.
     *
     * @param command        which should be dispatched (includes leading {@code /})
     * @param outputConsumer the command output consumer, no guarantee is given in which thread it is executed
     * @return the output of this command
     */
    EntityPlayer dispatchCommand(String command, @Nullable Consumer<CommandOutput> outputConsumer);

    /**
     * Update the players spawn position. It will be used in sending first chunks (when set in {@link io.gomint.event.player.PlayerPreJoinEvent}.
     * The spawn location is always in the world the player currently is in. So when the player changes the world the spawn location stays.
     *
     * @param spawnLocation which should be used for this player
     */
    EntityPlayer spawnLocation(Location spawnLocation );

    /**
     * Get the location of the spawn
     *
     * @return location of spawn
     */
    Location spawnLocation();

    /**
     * Set a new scoreboard
     *
     * @param scoreboard which should be displayed to this player
     */
    EntityPlayer scoreboard(Scoreboard scoreboard );

    /**
     * Get the current scoreboard of a player
     *
     * @return the scoreboard of this player or null
     */
    Scoreboard scoreboard();

    /**
     * Remove the current scoreboard
     */
    EntityPlayer removeScoreboard();

    /**
     * Get the ender chest inventory for this player
     *
     * @return ender chest inventory
     */
    EnderChestInventory enderChestInventory();

    /**
     * Update the clients commands. This is needed when permissions change for example or worlds change and custom
     * handling is involved
     */
    EntityPlayer sendCommands();

}
