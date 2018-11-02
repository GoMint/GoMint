package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Overload;
import io.gomint.command.annotation.Parameter;
import io.gomint.command.annotation.Permission;
import io.gomint.command.validator.BlockPositionValidator;
import io.gomint.command.validator.TargetValidator;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.world.WorldAdapter;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name( "spawnpoint" )
@Description( "Sets the spawn point for a player." )
@Permission( "gomint.command.spawnpoint" )
@Overload( {
    @Parameter( name = "player", validator = TargetValidator.class, optional = true ),
    @Parameter( name = "spawnPos", validator = BlockPositionValidator.class, optional = true )
} )
public class SpawnPointCommand extends Command {

    @Override
    public CommandOutput execute( CommandSender sender, String alias, Map<String, Object> arguments ) {
        CommandOutput output = new CommandOutput();

        if (! ( sender instanceof PlayerCommandSender ) ) {
            return output.fail( "Executor is required to be a player" );
        }

        EntityPlayer player = (EntityPlayer) arguments.getOrDefault( "player", sender );
        BlockPosition spawnPos = (BlockPosition) arguments.get( "spawnPos" );
        Location location = player.getLocation();

        if( spawnPos != null ) {
            location.setX( spawnPos.getX() );
            location.setY( spawnPos.getY() );
            location.setZ( spawnPos.getZ() );
        }

        player.setSpawnLocation(location);

        return output.success( String.format( "Set %s's spawn point to (%.1f, %.1f, %.1f)", player.getName(), location.getX(), location.getY(), location.getZ() ) );
    }
}
