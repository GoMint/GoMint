package io.gomint.server.command;

import io.gomint.command.CommandOverload;
import io.gomint.command.ParamValidator;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.network.packet.PacketAvailableCommands;
import io.gomint.server.network.type.CommandData;
import io.gomint.server.util.collection.IndexedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public class CommandPreprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandPreprocessor.class);

    // Those a static values which are used for PE to identify the type
    /**
     * This flag is set on all types EXCEPT the TEMPLATE type. Not completely sure what this is for, but it is required
     * for the argtype to work correctly. VALID seems as good a name as any.
     */
    private static final int ARG_FLAG_VALID = 0x100000;

    /**
     * Basic parameter types. These must be combined with the ARG_FLAG_VALID private static final intant.
     * ARG_FLAG_VALID | (type private static final int)
     */
    private static final int ARG_TYPE_INT = 0x01;
    private static final int ARG_TYPE_FLOAT = 0x02;
    private static final int ARG_TYPE_VALUE = 0x03;
    // WILDCARD_INT
    private static final int ARG_TYPE_TARGET = 0x06;
    // WILDCARD_TARGET
    private static final int ARG_TYPE_STRING = 0x1d;
    private static final int ARG_TYPE_POSITION = 0x25;
    private static final int ARG_TYPE_RAWTEXT = 0x2b;

    /**
     * Enums are a little different: they are composed as follows:
     * ARG_FLAG_ENUM | ARG_FLAG_VALID | (enum index)
     */
    private static final int ARG_FLAG_ENUM = 0x200000;

    /**
     * This is used for for /xp <level: int>L.
     */
    private static final int ARG_FLAG_POSTFIX = 0x1000000;

    // Enums are stored in an indexed list at the start. Enums are just collections of a name and
    // a integer list reflecting the index inside enumValues
    private List<String> enumValues = new ArrayList<>();
    private IndexedHashMap<String, List<Integer>> enums = new IndexedHashMap<>();
    private Map<CommandHolder, Integer> aliasIndex = new HashMap<>();
    private Map<String, Integer> enumIndexes = new HashMap<>();
    private List<String> postfixes = new ArrayList<>();

    // Cached commands packet
    private PacketAvailableCommands commandsPacket;

    /**
     * This preprocessor takes GoMint commands and merges them together into a PE format
     *
     * @param player   which should get the packet
     * @param commands which should be merged and written
     */
    public CommandPreprocessor(EntityPlayer player, List<CommandHolder> commands) {
        this.commandsPacket = new PacketAvailableCommands();

        // First we should scan all commands for aliases
        for (CommandHolder command : commands) {
            if (command.alias() != null) {
                for (String s : command.alias()) {
                    this.addEnum(command.name() + "CommandAlias", s);
                }

                this.aliasIndex.put(command, this.enums.getIndex(command.name() + "CommandAlias"));
            }
        }

        this.commandsPacket.setEnumValues(this.enumValues);

        // Now we need to search for enum validators
        for (CommandHolder command : commands) {
            if (command.overloads() != null) {
                for (CommandOverload overload : command.overloads()) {
                    if (overload.permission().isEmpty() || player.hasPermission(overload.permission())) {
                        if (overload.parameters() != null) {
                            for (Map.Entry<String, ParamValidator<?>> entry : overload.parameters().entrySet()) {
                                if (entry.getValue().hasValues()) {
                                    for (String s : entry.getValue().values()) {
                                        this.addEnum(command.name() + "#" + entry.getKey(), s);
                                    }

                                    this.enumIndexes.put(command.name() + "#" + entry.getKey(), this.enums.getIndex(command.name() + "#" + entry.getKey()));
                                }

                                if (entry.getValue().postfix() != null && !this.postfixes.contains(entry.getValue().postfix())) {
                                    this.postfixes.add(entry.getValue().postfix());
                                }
                            }
                        }
                    }
                }
            }
        }

        this.commandsPacket.setEnums(this.enums);
        this.commandsPacket.setPostFixes(this.postfixes);

        // Now we should have sorted any enums. Move on to write the command data
        List<CommandData> commandDataList = new ArrayList<>();
        for (CommandHolder command : commands) {
            // Construct new data helper for the packet
            CommandData commandData = new CommandData(command.name(), command.description());
            commandData.flags((byte) 0);
            commandData.permission((byte) command.commandPermission().getId());

            // Put in alias index
            if (command.alias() != null) {
                commandData.aliasIndex(this.aliasIndex.get(command));
            } else {
                commandData.aliasIndex(-1);
            }

            // Do we need to hack a bit here?
            List<List<CommandData.Parameter>> overloads = new ArrayList<>();

            if (command.overloads() != null) {
                for (CommandOverload overload : command.overloads()) {
                    if (overload.permission().isEmpty() || player.hasPermission(overload.permission())) {
                        List<CommandData.Parameter> parameters = new ArrayList<>();
                        if (overload.parameters() != null) {
                            for (Map.Entry<String, ParamValidator<?>> entry : overload.parameters().entrySet()) {
                                // Build together type
                                int paramType = 0; // We don't support postfixes yet

                                switch (entry.getValue().type()) {
                                    case INT:
                                        if (entry.getValue().postfix() != null) {
                                            paramType |= ARG_FLAG_POSTFIX;
                                            paramType |= this.postfixes.indexOf(entry.getValue().postfix());
                                        } else {
                                            paramType |= ARG_FLAG_VALID;
                                            paramType |= ARG_TYPE_INT;
                                        }

                                        break;
                                    case BOOL:
                                    case STRING_ENUM:
                                        paramType |= ARG_FLAG_ENUM;
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= this.enumIndexes.get(command.name() + "#" + entry.getKey());
                                        break;
                                    case TARGET:
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= ARG_TYPE_TARGET;
                                        break;
                                    case STRING:
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= ARG_TYPE_STRING;
                                        break;
                                    case BLOCK_POS:
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= ARG_TYPE_POSITION;
                                        break;
                                    case TEXT:
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= ARG_TYPE_RAWTEXT;
                                        break;
                                    case FLOAT:
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= ARG_TYPE_FLOAT;
                                        break;
                                    case COMMAND:
                                        paramType |= ARG_FLAG_ENUM;
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= this.enumIndexes.get(command.name() + "#" + entry.getKey());
                                        break;
                                    default:
                                        paramType |= ARG_FLAG_VALID;
                                        paramType |= ARG_TYPE_VALUE;
                                }

                                parameters.add(new CommandData.Parameter(entry.getKey(), paramType, entry.getValue().optional()));
                            }
                        }

                        overloads.add(parameters);
                    }
                }
            }

            commandData.parameters(overloads);
            commandDataList.add(commandData);
        }

        this.commandsPacket.setCommandData(commandDataList);
    }

    private void addEnum(String name, String value) {
        // Check if we already know this enum value
        int enumValueIndex;
        if (this.enumValues.contains(value)) {
            enumValueIndex = this.enumValues.indexOf(value);
        } else {
            this.enumValues.add(value);
            enumValueIndex = this.enumValues.indexOf(value);
        }

        // Create / add this value to the enum
        List<Integer> old = this.enums.get(name);
        if (old == null) {    // DONT use computeIfAbsent, the index won't show up
            old = new ArrayList<>();
            this.enums.put(name, old);
        }

        old.add(enumValueIndex);
    }

    public PacketAvailableCommands getCommandsPacket() {
        return this.commandsPacket;
    }

}
