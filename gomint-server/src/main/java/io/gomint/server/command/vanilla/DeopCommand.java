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
@Name("deop")
@Description("Revokes operator status from a player.")
@Permission("gomint.command.deop")
@Overload({
    @Parameter(name = "player", validator = TargetValidator.class, optional = true)
})
public class DeopCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        EntityPlayer target = (EntityPlayer) arguments.get("player");
        if (target == null) {
            if (sender instanceof PlayerCommandSender) {
                target = (EntityPlayer) sender;
            } else {
                output.fail("Please provide a player to deop");
                return;
            }
        }

        if (!target.op()) {
            output.fail("Could not deop (already not op): " + target.name());
            return;
        }

        target.op(false);
        target.sendMessage("You have been de-opped");
        output.success("De-opped: " + target.name());
    }
}
