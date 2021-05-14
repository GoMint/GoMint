package io.gomint.testplugin.command;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Alias;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.entity.EntityPlayer;
import io.gomint.plugin.injection.InjectPlugin;
import io.gomint.testplugin.TestPlugin;
import io.gomint.world.World;
import io.gomint.world.generator.CreateOptions;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author geNAZt
 * @version 1.1
 */
@Name("test")
@Alias("t")
@Description("A generic command for testing random stuff")
public class TestCommand extends Command {

    @InjectPlugin
    private TestPlugin plugin;

    @Override
    public void execute(CommandSender<?> commandSender, String alias, Map<String, Object> arguments, CommandOutput output) {
        World world = this.plugin.server().createWorld(String.valueOf(ThreadLocalRandom.current().nextInt()), new CreateOptions());
        EntityPlayer player = (EntityPlayer) commandSender;
        player.teleport(world.spawnLocation());
        output.success("Did teleport to %%s", world.name());
    }

}
