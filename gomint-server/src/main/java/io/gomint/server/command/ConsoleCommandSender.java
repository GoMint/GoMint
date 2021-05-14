/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.command;

import io.gomint.player.ChatType;
import io.gomint.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * @author geNAZt
 * @version 1.0
 */
public class ConsoleCommandSender implements io.gomint.command.ConsoleCommandSender {

    private final Logger commandLogger;

    /**
     * Construct a new console command sender for the given command
     *
     * @param commandName which should be executed with this console command sender
     */
    ConsoleCommandSender( String commandName ) {
        this.commandLogger = LoggerFactory.getLogger( "Command '" + commandName + "'" );
    }

    @Override
    public ConsoleCommandSender sendMessage( String message ) {
        this.commandLogger.info( message );
        return this;
    }

    @Override
    public ConsoleCommandSender sendMessage( ChatType type, String... message ) {
        for ( String s : message ) {
            this.commandLogger.info( s );
        }

        return this;
    }

    @Override
    public boolean hasPermission( String permission ) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        return defaultValue;
    }

    @Nullable
    @Override
    public World world() {
        return null;
    }

}
