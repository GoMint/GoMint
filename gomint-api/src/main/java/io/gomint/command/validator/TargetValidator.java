package io.gomint.command.validator;

import io.gomint.GoMint;
import io.gomint.command.CommandSender;
import io.gomint.command.ParamType;
import io.gomint.command.ParamValidator;
import io.gomint.command.PlayerCommandSender;
import io.gomint.entity.EntityPlayer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class TargetValidator extends ParamValidator {

    @Override
    public ParamType getType() {
        return ParamType.TARGET;
    }

    @Override
    public boolean hasValues() {
        return false;
    }

    @Override
    public List<String> values() {
        return null;
    }

    @Override
    public Object validate( String input, CommandSender commandSender ) {
        Collection<EntityPlayer> searchPool = GoMint.instance().getPlayers();
        if ( commandSender instanceof PlayerCommandSender ) {
            if ( input.equals( "@s" ) ) {
                return commandSender;
            }
        }

        for ( EntityPlayer player : searchPool ) {
            if ( player.getPlayerListName().equals( input ) || player.getName().equalsIgnoreCase( input ) ) {
                return player;
            }
        }

        return null;
    }

    @Override
    public String consume( Iterator<String> data ) {
        // Check if we have one element left
        if ( !data.hasNext() ) {
            return null;
        }

        // The first element can either be " to signal that the name has spaces or its the player name itself
        String first = data.next();
        if ( first.startsWith( "\"" ) ) {
            StringBuilder nameBuilder = new StringBuilder( first.substring( 1 ) );
            while ( data.hasNext() ) {
                String current = data.next();
                if ( current.endsWith( "\"" ) ) {
                    nameBuilder.append( " " ).append( current, 0, current.length() - 1 );
                    return nameBuilder.toString();
                }

                nameBuilder.append( " " ).append( current );
            }

            return nameBuilder.toString();
        }

        return first;
    }

    @Override
    public String getHelpText() {
        return "target:player";
    }

}
