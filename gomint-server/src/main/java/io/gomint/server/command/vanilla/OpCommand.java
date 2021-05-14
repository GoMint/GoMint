package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.TargetValidator;
import io.gomint.entity.EntityPlayer;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name("op")
@Description("Grants operator status to a player.")
@Permission("gomint.command.op")
@Overload({
    @Parameter(name = "player", validator = TargetValidator.class, optional = true)
})
public class OpCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        EntityPlayer target = (EntityPlayer) arguments.get("player");
        if (target == null) {
            if (sender instanceof PlayerCommandSender) {
                target = (EntityPlayer) sender;
            } else {
                output.fail("Please provide a player to op");
                return;
            }
        }

        if (target.op()) {
            output.fail("Could not op (already op or higher): " + target.name());
            return;
        }

        target.op(true);
        target.sendMessage("You have been opped");
        output.success("Opped: " + target.name());
    }
}
