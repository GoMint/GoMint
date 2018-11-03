package io.gomint.plugin.listener;

import io.gomint.entity.passive.EntityArmorStand;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.EventPriority;
import io.gomint.event.player.PlayerJoinEvent;
import io.gomint.plugin.TestPlugin;
import io.gomint.plugin.scoreboard.DebugScoreboard;
import io.gomint.world.Gamemode;
import lombok.RequiredArgsConstructor;

/**
 * @author geNAZt
 * @version 1.0
 */
@RequiredArgsConstructor
public class PlayerJoinListener implements EventListener {

    private final TestPlugin plugin;

    @EventHandler( priority = EventPriority.HIGHEST )
    public void onPlayerJoin( PlayerJoinEvent event ) {
        // Set to allow all permissions
        event.getPlayer().getPermissionManager().setPermission( "*", true );

        event.getPlayer().setGamemode( Gamemode.CREATIVE );

        // Give this player the debug scoreboard
        new DebugScoreboard( this.plugin, event.getPlayer() );

        // Spawn a Armor Stand to the player location
        EntityArmorStand.create().spawn( event.getPlayer().getLocation() );
    }

}
