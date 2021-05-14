package io.gomint.server.command.vanilla;

import io.gomint.ChatColor;
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
 * @author Kaooot
 * @version 1.0
 */
@Name("say")
@Description("Sends a message in the chat to other players.")
@Permission("gomint.command.say")
@Overload({
    @Parameter(name = "message", validator = TextValidator.class)
})
public class SayCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        String message = (String) arguments.get("message");

        GoMint.instance().onlinePlayers().forEach(players ->
            players.sendMessage(ChatColor.LIGHT_PURPLE + "[" + (sender instanceof ConsoleCommandSender ? "CONSOLE" : ((EntityPlayer) sender).name()) + "] " + message));
    }
}
