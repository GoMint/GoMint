/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.command;

import io.gomint.command.Command;
import io.gomint.command.CommandOutput;
import io.gomint.command.CommandOverload;
import io.gomint.command.CommandSender;
import io.gomint.command.ParamValidator;
import io.gomint.command.PlayerCommandSender;
import io.gomint.command.validator.CommandValidator;
import io.gomint.plugin.Plugin;
import io.gomint.server.entity.CommandPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public class SubCommand extends Command {

    private final Plugin plugin;
    private final Map<String, CommandHolder> subCommands = new HashMap<>();

    /**
     * Create a new sub command container for the plugin
     *
     * @param plugin   which created this sub command
     * @param baseName of the command
     */
    SubCommand(Plugin plugin, String baseName) {
        super(baseName);
        this.plugin = plugin;
    }

    public void addCommand(Plugin plugin, String subCommandName, CommandHolder holder) {
        if (plugin == null || this.plugin.equals(plugin)) {
            this.subCommands.put(subCommandName, holder);
        }
    }

    @Override
    public void execute(CommandSender<?> sender, String alias, Map<String, Object> arguments, CommandOutput output) {
        // Look out for subCmd# keys
        for (Map.Entry<String, Object> entry : arguments.entrySet()) {
            if (!entry.getKey().equals(entry.getValue()) || !this.subCommands.containsKey(entry.getKey())) {
                continue;
            }
            String subCommand = (String) entry.getValue();
            CommandHolder commandHolder = this.subCommands.get(subCommand);
            if (commandHolder == null) {
                continue;
            }
            // Check for world
            if (sender instanceof PlayerCommandSender) {
                if (!commandHolder.activeInWorld(sender.world())) {
                    continue;
                }
            }
            // Check permission
            if (commandHolder.permission() == null || sender.hasPermission(commandHolder.permission())) {
                Map<String, Object> arg = new HashMap<>(arguments);
                arg.remove(entry.getKey());

                commandHolder.executor().execute(sender, alias + " " + entry.getValue(), arg, output);
                return;
            } else {
                output.fail("No permission for this command");
                return;
            }
        }

        output.fail("Command for input '%s' could not be found", this.name());
    }

    /**
     * Create the correct command holder for package assembly
     *
     * @param player for which we need this sub command
     * @return null when no sub commands apply, a holder when they apply
     */
    CommandHolder createHolder(io.gomint.server.entity.EntityPlayer player) {
        List<CommandOverload> overloads = new ArrayList<>();

        for (Map.Entry<String, CommandHolder> entry : this.subCommands.entrySet()) {
            CommandHolder holder = entry.getValue();
            if (!holder.activeInWorld(player.world())) {
                continue;
            }
            if (holder.permission() != null && !player.hasPermission(holder.permission())) {
                continue;
            }
            if (holder.overloads() != null) {
                for (CommandOverload commandOverload : holder.overloads()) {
                    CommandOverload overload = new CommandOverload();
                    CommandValidator commandValidator = new CommandValidator();
                    overload.param(entry.getKey(), commandValidator);

                    for (Map.Entry<String, ParamValidator<?>> validatorEntry : commandOverload.parameters().entrySet()) {
                        overload.param(validatorEntry.getKey(), validatorEntry.getValue());
                    }

                    overloads.add(overload);
                }
            } else {
                CommandOverload overload = new CommandOverload();
                CommandValidator commandValidator = new CommandValidator();
                overload.param(entry.getKey(), commandValidator);
                overloads.add(overload);
            }
        }

        if (!overloads.isEmpty()) {
            return new CommandHolder(
                this.plugin,
                this.name(),
                "Subcommands for command '" + this.name() + "'",
                null,
                    CommandPermission.NORMAL,
                null,
                false,
                this,
                overloads);
        }

        return null;
    }

}
