package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.BlockPositionValidator;
import io.gomint.command.validator.TargetValidator;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name("spawnpoint")
@Description("Sets the spawn point for a player.")
@Permission("gomint.command.spawnpoint")
@Overload({
    @Parameter(name = "player", validator = TargetValidator.class, optional = true),
    @Parameter(name = "spawnPos", validator = BlockPositionValidator.class, optional = true)
})
public class SpawnPointCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        if (!(sender instanceof PlayerCommandSender)) {
            output.fail("Executor is required to be a player");
            return;
        }

        EntityPlayer player = (EntityPlayer) arguments.getOrDefault("player", sender);
        BlockPosition spawnPos = (BlockPosition) arguments.get("spawnPos");
        Location location = player.location();

        if (spawnPos != null) {
            location.x(spawnPos.x());
            location.y(spawnPos.y());
            location.z(spawnPos.z());
        }

        player.spawnLocation(location);
        output.success(String.format("Set %s's spawn point to (%.1f, %.1f, %.1f)", player.name(), location.x(), location.y(), location.z()));
    }
}
