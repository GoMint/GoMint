package io.gomint.sample;

import io.gomint.GoMint;
import io.gomint.command.Command;
import io.gomint.command.CommandSender;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.PlayerChatEvent;
import io.gomint.event.PlayerJoinEvent;
import io.gomint.plugin.Name;
import io.gomint.plugin.Plugin;
import io.gomint.plugin.Version;

/**
 * @author Digot
 * @version 1.0
 */
@Name( "SamplePlugin" )
@Version( major = 1, minor = 0)
public class SamplePlugin extends Plugin {

    @Override
    public void onStartup ( ) {
        this.getPluginManager().registerListener( new EventListener() {
            @EventHandler
            public void onPlayerJoin( PlayerJoinEvent event ) {

            }
        } );

        this.getPluginManager().registerCommand( new Command("test") {
            @Override
            public void execute ( CommandSender sender, String[] args ) {
                sender.sendMessage( "Online players" );
                sender.sendMessage( " -- " );
            }
        } );
    }
}
