package io.gomint.server.command.gomint;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Overload;
import io.gomint.command.annotation.Parameter;
import io.gomint.command.annotation.Permission;
import io.gomint.command.validator.TargetValidator;
import io.gomint.command.validator.TextValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */

@Name("kick")
@Description("Kick a player from the server.")
@Permission("gomint.command.kick")
@Overload({
    @Parameter(name = "player", validator = TargetValidator.class),
    @Parameter(name = "reason", validator = TextValidator.class, optional = true)
})
public class KickCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        EntityPlayer target = (EntityPlayer) arguments.get("player");

        if (target == null) {
            output.fail("You must provide a player!");
            return;
        }

        String reason = (String) arguments.getOrDefault("reason", "Kicked by an operator.");

        if (sender.world() != target.world()) {
            output.markAsync();
            target.world().syncScheduler().execute(() -> {
                target.disconnect(reason);
                output.fail("Kicked %%s from the server", target.displayName()).markFinished();
            });
        } else {
            target.disconnect(reason);
            output.fail("Kicked %%s from the server", target.displayName());
        }
    }
}
