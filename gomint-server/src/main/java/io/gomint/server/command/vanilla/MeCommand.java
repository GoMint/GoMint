package io.gomint.server.command.vanilla;

import io.gomint.GoMint;
import io.gomint.command.*;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.TextValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name( "me" )
@Description( "Displays a message about yourself." )
@Permission( "gomint.command.me" )
@Overload( {
    @Parameter( name = "message", validator = TextValidator.class )
} )
public class MeCommand extends Command {

    @Override
    public CommandOutput execute( CommandSender sender, String alias, Map<String, Object> arguments ) {
        String message = (String) arguments.get( "message" );

        GoMint.instance().getPlayers().forEach( players ->
            players.sendMessage( "* " + ( sender instanceof ConsoleCommandSender ? "CONSOLE" : ((EntityPlayer) sender).getName() ) + " " + message ) );

        return new CommandOutput();
    }
}
