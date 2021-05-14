package io.gomint.server.command.vanilla;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Permission;
import io.gomint.entity.EntityPlayer;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author geNAZt
 * @version 1.0
 */
@Name("list")
@Description("List online players")
@Permission("gomint.command.list")
public class ListCommand extends Command {

    @Override
    public void execute(CommandSender<?> player, String alias, Map<String, Object> arguments, CommandOutput output) {
        Collection<EntityPlayer> players = GoMint.instance().onlinePlayers();
        output.success("There are %%s/%%s players online:", players.size(), GoMint.instance().maxPlayerCount());

        StringJoiner joiner = new StringJoiner(", ");
        players.forEach((player1) -> joiner.add(player1.displayName()));

        output.success(joiner.toString());
    }

}
