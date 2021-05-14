package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.BlockPositionValidator;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.world.WorldAdapter;

import java.util.Map;

/**
 * @author Clockw1seLrd
 * @version 1.0
 */
@Name("setworldspawn")
@Description("Sets the world spawn")
@Permission("gomint.command.setworldspawn")
@Overload({
    @Parameter(name = "spawnPoint", validator = BlockPositionValidator.class, optional = true)
})
public class SetWorldSpawnCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        if (!(sender instanceof PlayerCommandSender)) {
            output.fail("Executor is required to be a player");
            return;
        }

        EntityPlayer executor = (EntityPlayer) sender;
        WorldAdapter affectedWorld = executor.world();
        Location worldSpawnLocation = executor.location();

        // Handling argument: spawnPoint
        BlockPosition spawnPoint = (BlockPosition) arguments.get("spawnPoint");
        if (spawnPoint != null) {
            worldSpawnLocation.x(spawnPoint.x());
            worldSpawnLocation.y(spawnPoint.y());
            worldSpawnLocation.z(spawnPoint.z());
        }

        this.floorLocation(worldSpawnLocation);
        affectedWorld.spawnLocation(worldSpawnLocation);

        output.success(String.format("Set the world spawn point to (%.1f, %.1f, %.1f)",
            worldSpawnLocation.x(),
            worldSpawnLocation.y(),
            worldSpawnLocation.z()));
    }

    private void floorLocation(Location location) {
        location.x((float) Math.floor(location.x()));
        location.y((float) Math.floor(location.y()));
        location.z((float) Math.floor(location.z()));
    }
}
