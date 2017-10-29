package io.gomint.server.network.handler;

import io.gomint.entity.Entity;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.PacketMobEquipment;

import java.util.function.Predicate;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PacketMobEquipmentHandler implements PacketHandler<PacketMobEquipment> {

    @Override
    public void handle( PacketMobEquipment packet, long currentTimeMillis, PlayerConnection connection ) {
        // Anti crash checks
        if ( packet.getSelectedSlot() > 8 ) {
            return;
        }

        // Ok the client wants to switch hotbar slot (itemInHand)
        ItemStack wanted = connection.getEntity().getInventory().getItem( packet.getSelectedSlot() );
        if ( wanted != null && wanted.equals( packet.getStack() ) && wanted.getAmount() == packet.getStack().getAmount() ) {
            // Inform the old item it got deselected
            io.gomint.server.inventory.item.ItemStack oldItemInHand =
                (io.gomint.server.inventory.item.ItemStack) connection.getEntity().getInventory().getItemInHand();
            oldItemInHand.removeFromHand( connection.getEntity() );

            // Set item in hand index
            connection.getEntity().getInventory().setItemInHand( packet.getSelectedSlot() );

            // Inform the item it got selected
            io.gomint.server.inventory.item.ItemStack newItemInHand =
                (io.gomint.server.inventory.item.ItemStack) connection.getEntity().getInventory().getItemInHand();
            newItemInHand.gotInHand( connection.getEntity() );

            // Relay packet
            connection.getEntity().getWorld().sendToVisible( connection.getEntity().getLocation().toBlockPosition(),
                packet, new Predicate<Entity>() {
                    @Override
                    public boolean test( Entity entity ) {
                        return !connection.getEntity().equals( entity );
                    }
                } );
        }
    }

}
