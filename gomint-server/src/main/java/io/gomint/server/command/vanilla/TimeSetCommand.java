/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.command.vanilla;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandSender;
import io.gomint.command.ConsoleCommandSender;
import io.gomint.command.annotation.Description;
import io.gomint.command.annotation.Name;
import io.gomint.command.annotation.Overload;
import io.gomint.command.annotation.Parameter;
import io.gomint.command.annotation.Permission;
import io.gomint.command.validator.EnumValidator;
import io.gomint.command.validator.IntegerValidator;
import io.gomint.entity.EntityPlayer;
import io.gomint.math.MathUtils;
import io.gomint.server.util.Values;
import io.gomint.world.World;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @version 1.0
 */
@Name("time set")
@Description("Set time of the world")
@Permission("gomint.command.time.set")
@Overload({
    @Parameter(name = "ticks", validator = IntegerValidator.class)
})
@Overload({
    @Parameter(name = "time", arguments = {
        "day", "night", "noon", "midnight", "sunrise", "sunset"
    }, validator = EnumValidator.class)
})
public class TimeSetCommand extends Command {

    @Override
    public void execute(CommandSender<?> commandSender, String alias, Map<String, Object> arguments, CommandOutput output) {
        if (commandSender instanceof ConsoleCommandSender) {
            output.fail("Only players can set the time in a world");
            return;
        }

        Integer ticksOrNull = (Integer) arguments.get("ticks");
        if ( ticksOrNull == null ) {
            String timeEnum = (String) arguments.get("time");
            switch (timeEnum) {
                case "day":
                    ticksOrNull = 1000;
                    break;
                case "night":
                    ticksOrNull = 13000;
                    break;
                case "noon":
                    ticksOrNull = 6000;
                    break;
                case "midnight":
                    ticksOrNull = 18000;
                    break;
                case "sunrise":
                    ticksOrNull = 23000;
                    break;
                case "sunset":
                    ticksOrNull = 12000;
                    break;
            }
        }

        long seconds = (long) (ticksOrNull / Values.CYCLE_TICKS_PER_SECOND);

        // Since 0 is not at 0 we need to offset 6 hours
        seconds += Values.SECONDS_ON_ZERO;

        if (seconds >= TimeUnit.HOURS.toSeconds(24)) {
            seconds -= TimeUnit.HOURS.toSeconds(24);
        }

        Duration time = Duration.ofSeconds(seconds);

        World world = ((EntityPlayer) commandSender).world();
        world.time(time);

        output.success("Set time to %%s", formatDuration(time));
    }

    private String formatDuration(Duration duration) {
        int seconds = (int) duration.getSeconds();
        int minutes = MathUtils.fastFloor(seconds / 60f);
        seconds -= minutes * 60;
        int hours = MathUtils.fastFloor(minutes / 60f);
        minutes -= hours * 60;

        StringBuilder timeString = new StringBuilder();
        if (hours < 10) {
            timeString.append("0");
        }

        timeString.append(hours).append(":");

        if (minutes < 10) {
            timeString.append("0");
        }

        timeString.append(minutes).append(":");

        if (seconds < 10) {
            timeString.append("0");
        }

        timeString.append(seconds);
        return timeString.toString();
    }

}
