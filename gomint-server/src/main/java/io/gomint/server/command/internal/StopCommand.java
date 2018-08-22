package io.gomint.server.command.internal;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Permission;

import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
@Name( "stop" )
@Description( "Stops the GoMint server." )
@Permission( "gomint.command.stop" )
public class StopCommand extends Command {

    @Override
    public CommandOutput execute( CommandSender player, String alias, Map<String, Object> arguments ) {
        GoMint.instance().shutdown();
        return new CommandOutput().success( "§7[§aSYSTEM§7] §fServer will be stopped" );
    }

}
