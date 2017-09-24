package io.gomint.server.command;

import io.gomint.command.CommandOverload;
import io.gomint.command.ParamValidator;
import io.gomint.server.network.packet.PacketAvailableCommands;
import io.gomint.server.network.type.CommandData;
import io.gomint.server.util.IndexedHashMap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author geNAZt
 * @version 1.0
 */
public class CommandPreprocessor {

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
    private static final int ARG_TYPE_TARGET = 0x04;
    private static final int ARG_TYPE_STRING = 0x0d;
    private static final int ARG_TYPE_POSITION = 0x0e;
    private static final int ARG_TYPE_RAWTEXT = 0x11;
    private static final int ARG_TYPE_TEXT = 0x13;
    private static final int ARG_TYPE_JSON = 0x16;
    private static final int ARG_TYPE_COMMAND = 0x1d;
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

    // Cached commands packet
    @Getter
    private PacketAvailableCommands commandsPacket;

    /**
     * This preprocessor takes GoMint commands and merges them together into a PE format
     *
     * @param commands which should be merged and written
     */
    public CommandPreprocessor( List<CommandHolder> commands ) {
        this.commandsPacket = new PacketAvailableCommands();

        // First we should scan all commands for aliases
        for ( CommandHolder command : commands ) {
            if ( command.getAlias() != null ) {
                for ( String s : command.getAlias() ) {
                    this.addEnum( command.getName() + "CommandAlias", s );
                }

                this.aliasIndex.put( command, this.enums.getIndex( command.getName() + "CommandAlias" ) );
            }
        }

        this.commandsPacket.setEnumValues( this.enumValues );

        // Now we need to search for enum validators
        for ( CommandHolder command : commands ) {
            if ( command.getOverload() != null ) {
                for ( CommandOverload overload : command.getOverload() ) {
                    if ( overload.getParameters() != null ) {
                        for ( Map.Entry<String, ParamValidator> entry : overload.getParameters().entrySet() ) {
                            if ( entry.getValue().hasValues() ) {
                                for ( String s : entry.getValue().values() ) {
                                    this.addEnum( entry.getKey(), s );
                                }

                                this.enumIndexes.put( command.getName() + "#" + entry.getKey(), this.enums.getIndex( entry.getKey() ) );
                            }
                        }
                    }
                }
            }
        }

        this.commandsPacket.setEnums( this.enums );

        // Now we should have sorted any enums. Move on to write the command data
        List<CommandData> commandDataList = new ArrayList<>();
        for ( CommandHolder command : commands ) {
            // Construct new data helper for the packet
            CommandData commandData = new CommandData( command.getName(), command.getDescription() );
            commandData.setFlags( (byte) 0 );
            commandData.setPermission( (byte) command.getCommandPermission().getId() );

            // Put in alias index
            if ( command.getAlias() != null ) {
                commandData.setAliasIndex( this.aliasIndex.get( command ) );
            } else {
                commandData.setAliasIndex( -1 );
            }

            // Do we need to hack a bit here?
            List<List<CommandData.Parameter>> overloads = new ArrayList<>();

            if ( command.getOverload() != null ) {
                for ( CommandOverload overload : command.getOverload() ) {
                    List<CommandData.Parameter> parameters = new ArrayList<>();
                    if ( overload.getParameters() != null ) {
                        for ( Map.Entry<String, ParamValidator> entry : overload.getParameters().entrySet() ) {
                            // Build together type
                            int paramType = ARG_FLAG_VALID; // We don't support postfixes yet

                            switch ( entry.getValue().getType() ) {
                                case INT:
                                    paramType |= ARG_TYPE_INT;
                                    break;
                                case BOOL:
                                case STRING_ENUM:
                                    paramType |= ARG_FLAG_ENUM;
                                    paramType |= this.enumIndexes.get( command.getName() + "#" + entry.getKey() );
                                    break;
                                case TARGET:
                                    paramType |= ARG_TYPE_TARGET;
                                    break;
                                case STRING:
                                    paramType |= ARG_TYPE_STRING;
                                    break;
                                case BLOCK_POS:
                                    paramType |= ARG_TYPE_POSITION;
                                    break;
                                case TEXT:
                                    paramType |= ARG_TYPE_TEXT;
                                    break;
                                default:
                                    paramType |= ARG_TYPE_VALUE;
                            }

                            parameters.add( new CommandData.Parameter( entry.getKey(), paramType, entry.getValue().isOptional() ) );
                        }
                    }

                    overloads.add( parameters );
                }
            }

            commandData.setParameters( overloads );
            commandDataList.add( commandData );
        }

        this.commandsPacket.setCommandData( commandDataList );
        this.commandsPacket.setPostFixes( new ArrayList<>() );
    }

    private void addEnum( String name, String value ) {
        // Check if we already know this enum value
        int enumValueIndex;
        if ( this.enumValues.contains( value ) ) {
            enumValueIndex = this.enumValues.indexOf( value );
        } else {
            this.enumValues.add( value );
            enumValueIndex = this.enumValues.indexOf( value );
        }

        // Create / add this value to the enum
        this.enums.computeIfAbsent( name, new Function<String, List<Integer>>() {
            @Override
            public List<Integer> apply( String s ) {
                return new ArrayList<>();
            }
        } ).add( enumValueIndex );
    }

}
