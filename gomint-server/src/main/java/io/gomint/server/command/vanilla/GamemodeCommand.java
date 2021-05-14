package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.ConsoleCommandSender;
import io.gomint.command.annotation.*;
import io.gomint.command.validator.EnumValidator;
import io.gomint.command.validator.IntegerValidator;
import io.gomint.command.validator.TargetValidator;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.world.Gamemode;

import java.util.Map;

/**
 * @author lukeeey
 * @version 1.0
 */
@Name("gamemode")
@Description("Sets a player's game mode.")
@Permission("gomint.command.gamemode")
@Overload({
    @Parameter(name = "gameMode", validator = EnumValidator.class, arguments = {"a", "adventure", "c", "creative", "s", "survival", "sp", "spectator"}),
    @Parameter(name = "player", validator = TargetValidator.class, optional = true)
})
@Overload({
    @Parameter(name = "gameMode", validator = IntegerValidator.class),
    @Parameter(name = "player", validator = TargetValidator.class, optional = true)
})
public class GamemodeCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        EntityPlayer target;
        Gamemode mode;

        if (arguments.containsKey("player") || sender instanceof ConsoleCommandSender) {
            if (arguments.get("player") == null) {
                output.fail("No targets matched selector.");
                return;
            }
            target = (EntityPlayer) arguments.get("player");
        } else {
            target = (EntityPlayer) sender;
        }

        switch (String.valueOf(arguments.get("gameMode"))) {
            case "0":
            case "s":
            case "survival":
                mode = Gamemode.SURVIVAL;
                break;
            case "1":
            case "c":
            case "creative":
                mode = Gamemode.CREATIVE;
                break;
            case "2":
            case "a":
            case "adventure":
                mode = Gamemode.ADVENTURE;
                break;
            case "3":
            case "sp":
            case "spectator":
                mode = Gamemode.SPECTATOR;
                break;
            default:
                output.fail("Unknown game mode");
                return;
        }

        target.gamemode(mode);
        target.sendMessage("Your game mode has been updated to " + this.getGamemodeName(mode));

        if (target == sender) {
            output.success("Set own game mode to %%s", this.getGamemodeName(mode));
        } else {
            output.success("Set %%s's game mode to %%s", target.displayName(), this.getGamemodeName(mode));
        }
    }

    private String getGamemodeName(Gamemode mode) {
        switch (mode) {
            case SURVIVAL:
                return "Survival";
            case CREATIVE:
                return "Creative";
            case ADVENTURE:
                return "Adventure";
            case SPECTATOR:
                return "Spectator";
        }

        return "Unknown";
    }
}
