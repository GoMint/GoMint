package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Overload;
import io.gomint.command.annotation.Parameter;
import io.gomint.command.annotation.Permission;
import io.gomint.command.validator.TargetValidator;
import io.gomint.entity.EntityPlayer;
import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.server.world.WorldAdapter;

import java.util.Map;

/**
 * @author Kaooot
 * @version 1.0
 */
@Name("kill")
@Description("Kills entities (players, mobs, etc.).")
@Permission("gomint.command.kill")
@Overload({
    @Parameter(name = "target", validator = TargetValidator.class, optional = true)
})
public class KillCommand extends Command {

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        EntityPlayer target = (EntityPlayer) arguments.getOrDefault("target", sender);

        if (target == null) {
            output.fail("No targets matched selector.");
            return;
        }

        if (sender.world() != target.world()) {
            output.markAsync();
            ((WorldAdapter) target.world()).syncScheduler().execute(() -> {
                target.attack(target.maxHealth(), EntityDamageEvent.DamageSource.COMMAND);
                output.success("Killed " + target.name()).markFinished();
            });
        } else {
            target.attack(target.maxHealth(), EntityDamageEvent.DamageSource.COMMAND);
            output.success("Killed " + target.name());
        }
    }
}
