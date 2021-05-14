

package io.gomint.server.command.vanilla;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.BlockPositionValidator;
import io.gomint.command.validator.StringValidator;
import io.gomint.command.validator.TargetValidator;
import io.gomint.entity.EntityPlayer;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.world.WorldAdapter;
import io.gomint.world.World;

import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
@Name("tp")
@Description("Teleport to a given place or entity")
@Permission("gomint.command.tp")
@Overload({
    @Parameter(name = "world", validator = StringValidator.class, arguments = {"[a-zA-Z0-9ß\\-]+"}),
    @Parameter(name = "position", validator = BlockPositionValidator.class)
})
@Overload({
    @Parameter(name = "toTarget", validator = TargetValidator.class)
})
@Overload({
    @Parameter(name = "position", validator = BlockPositionValidator.class)
})
@Overload({
    @Parameter(name = "target", validator = TargetValidator.class),
    @Parameter(name = "position", validator = BlockPositionValidator.class, optional = true)
})
@Overload({
    @Parameter(name = "target", validator = TargetValidator.class),
    @Parameter(name = "toTarget", validator = TargetValidator.class)
})
public class TPCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        // Check for source
        EntityPlayer source = (sender instanceof PlayerCommandSender) ? (EntityPlayer) sender : null;
        if (arguments.containsKey("target")) {
            source = (EntityPlayer) arguments.get("target");
        }

        if (source == null) {
            output.fail("No source for teleport given");
            return;
        }

        // Check for entity teleportation
        if (arguments.containsKey("toTarget")) {
            EntityPlayer entity = (EntityPlayer) arguments.get("toTarget");
            if (source.world() != sender.world()) {
                output.markAsync();
                EntityPlayer finalSource = source;
                ((WorldAdapter) source.world()).syncScheduler().execute(() -> {
                    finalSource.teleport(entity.location());
                    output.success("%%s has been teleported to %%s", finalSource.name(), entity.name()).markFinished();
                });
            } else {
                source.teleport(entity.location());
                output.success("%%s has been teleported to %%s", source.name(), entity.name());
            }
            return;
        }

        // Do we have a world given?
        Location to = new Location(source.world(), 0, 0, 0);
        if (arguments.containsKey("world")) {
            World world = GoMint.instance().world((String) arguments.get("world"));
            if (world == null) {
                output.fail("World %%s could not be found", arguments.get("world"));
                return;
            } else {
                to.world(world);
            }
        }

        BlockPosition position = (BlockPosition) arguments.get("position");
        to.x(position.x());
        to.y(position.y());
        to.z(position.z());

        if (source.world() != sender.world()) {
            output.markAsync();
            EntityPlayer finalSource = source;
            ((WorldAdapter) source.world()).syncScheduler().execute(() -> {
                finalSource.teleport(to);
                output.success("%%s has been teleported to %%s, %%s, %%s, %%s", finalSource.name(), to.world().name(), to.x(), to.y(), to.z()).markFinished();
            });
        } else {
            source.teleport(to);
            output.success("%%s has been teleported to %%s, %%s, %%s, %%s", source.name(), to.world().name(), to.x(), to.y(), to.z());
        }
    }
}
