package io.gomint.server.command;

import io.gomint.command.Command;
import io.gomint.command.CommandOverload;
import io.gomint.plugin.Plugin;
import io.gomint.server.entity.CommandPermission;
import io.gomint.world.World;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * @author geNAZt
 * @version 1.0
 */
public class CommandHolder {

    @Nullable
    private final Plugin plugin;
    private final String name;
    private final String description;
    private final Set<String> alias;

    private final CommandPermission commandPermission;
    private final String permission;
    private final boolean permissionDefault;
    private final Command executor;
    private final List<CommandOverload> overloads;

    public CommandHolder(@Nullable Plugin plugin, String name, String description, Set<String> alias,
                         CommandPermission commandPermission, String permission,
                         boolean permissionDefault, Command executor, List<CommandOverload> overloads) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.alias = alias;
        this.commandPermission = commandPermission;
        this.permission = permission;
        this.permissionDefault = permissionDefault;
        this.executor = executor;
        this.overloads = overloads;
    }
    
    public boolean activeInWorld(World world) {
        return this.plugin == null || this.plugin.activeInWorld(world);
    }

    @Nullable
    public Plugin plugin() {
        return this.plugin;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Set<String> alias() {
        return this.alias;
    }

    public CommandPermission commandPermission() {
        return this.commandPermission;
    }

    public String permission() {
        return this.permission;
    }

    public boolean isPermissionDefault() {
        return this.permissionDefault;
    }

    public Command executor() {
        return this.executor;
    }

    public List<CommandOverload> overloads() {
        return this.overloads;
    }

}
