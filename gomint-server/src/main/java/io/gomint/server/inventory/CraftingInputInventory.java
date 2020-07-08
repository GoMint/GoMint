package io.gomint.server.inventory;

import io.gomint.inventory.InventoryType;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.type.WindowType;
import io.gomint.server.world.WorldAdapter;
import lombok.Setter;

/**
 * @author geNAZt
 * @version 1.0
 */
public class CraftingInputInventory extends ContainerInventory {

    @Setter
    private BlockPosition position;

    public CraftingInputInventory( InventoryHolder owner ) {
        super( owner, 4 );
    }

    @Override
    public WindowType getType() {
        return WindowType.WORKBENCH;
    }

    @Override
    public void onOpen(EntityPlayer player) {

    }

    @Override
    public BlockPosition getContainerPosition() {
        return this.position;
    }

    @Override
    public void onClose(EntityPlayer player) {
        WorldAdapter worldAdapter = player.getWorld();
        Location location = player.getLocation();

        // Push out all items in the crafting views
        for ( ItemStack stack : player.getCraftingInventory().getContentsArray() ) {
            worldAdapter.dropItem( location, stack );
        }

        // Client closed its crafting view
        player.getCraftingInventory().resizeAndClear( 4 );
        player.getCraftingResultInventory().resizeAndClear( 4 );
    }

    @Override
    public void sendContents( PlayerConnection playerConnection ) {

    }

    @Override
    public void sendContents( int slot, PlayerConnection playerConnection ) {

    }

    @Override
    public InventoryType getInventoryType() {
        return InventoryType.CRAFTING;
    }

}
