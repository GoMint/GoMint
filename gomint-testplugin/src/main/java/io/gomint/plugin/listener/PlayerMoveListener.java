package io.gomint.plugin.listener;

import io.gomint.entity.ChatType;
import io.gomint.event.EventHandler;
import io.gomint.event.EventListener;
import io.gomint.event.player.PlayerMoveEvent;
import io.gomint.math.BlockPosition;
import io.gomint.world.Gamemode;
import io.gomint.world.block.Block;
import io.gomint.world.block.Dirt;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PlayerMoveListener implements EventListener {

    @EventHandler
    public void onPlayerMove( PlayerMoveEvent event ) {
        BlockPosition toBlock = event.getTo().toBlockPosition();
        Block block = event.getTo().getWorld().getBlockAt( toBlock.clone().add( BlockPosition.DOWN ) );

        event.getPlayer().sendMessage( ChatType.POPUP,
                "§fX: §a" + toBlock.getX() + " §e- §fY: §a" + toBlock.getY() + " §e- §fZ: §a" + toBlock.getZ(),
                "§fWalking on block: §a" + block.getClass() );
    }

}
