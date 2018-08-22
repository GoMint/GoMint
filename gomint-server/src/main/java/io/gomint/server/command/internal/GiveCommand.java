package io.gomint.server.command.internal;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Parameter;
import io.gomint.command.annotation.Permission;
import io.gomint.command.annotation.Overload;
import io.gomint.command.validator.StringValidator;
import io.gomint.command.validator.IntegerValidator;
import io.gomint.command.validator.TargetValidator;
import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemType;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name( "give" )
@Description( "Gives an item to a player." )
@Permission( "gomint.command.give" )
@Overload( {
    @Parameter( name = "player", validator = TargetValidator.class ),
    @Parameter( name = "item", validator = StringValidator.class),
    @Parameter( name = "amount", validator = IntegerValidator.class, optional = true ),
    @Parameter( name = "data", validator = IntegerValidator.class, optional = true )
} )
public class GiveCommand extends Command {

    @Override
    public CommandOutput execute( CommandSender player, String alias, Map<String, Object> arguments ) {
        CommandOutput output = new CommandOutput();
        EntityPlayer target = (EntityPlayer) arguments.get( "player" );

        if( target == null ) {
            return output.fail( "No targets matched selector" );
        }

        ItemType itemType = ItemType.valueOf( (String) arguments.get( "item" ) );



        return output;
    }

}
