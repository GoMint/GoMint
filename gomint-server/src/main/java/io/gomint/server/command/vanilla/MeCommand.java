package io.gomint.server.command.vanilla;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.ConsoleCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.TextValidator;
import io.gomint.server.entity.EntityPlayer;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name("me")
@Description("Displays a message about yourself.")
@Permission("gomint.command.me")
@Overload({
    @Parameter(name = "message", validator = TextValidator.class, optional = true)
})
public class MeCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        String message = (String) arguments.get("message");
        GoMint.instance().onlinePlayers().forEach(players -> players.sendMessage("* " + (sender instanceof ConsoleCommandSender ? "CONSOLE" : ((EntityPlayer) sender).name()) + " " + (message != null ? message : "")));
    }
}
