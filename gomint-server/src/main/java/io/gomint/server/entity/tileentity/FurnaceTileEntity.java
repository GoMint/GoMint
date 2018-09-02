/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.entity.tileentity;

import io.gomint.GoMint;
import io.gomint.entity.Entity;
import io.gomint.entity.EntityPlayer;
import io.gomint.inventory.item.ItemAir;
import io.gomint.inventory.item.ItemBurnable;
import io.gomint.inventory.item.ItemType;
import io.gomint.math.Vector;
import io.gomint.server.GoMintServer;
import io.gomint.server.crafting.SmeltingRecipe;
import io.gomint.server.inventory.FurnaceInventory;
import io.gomint.server.inventory.InventoryHolder;
import io.gomint.server.inventory.item.ItemStack;
import io.gomint.server.network.packet.PacketSetContainerData;
import io.gomint.server.world.WorldAdapter;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class FurnaceTileEntity extends ContainerTileEntity implements InventoryHolder {

    private static final int CONTAINER_PROPERTY_TICK_COUNT = 0;
    private static final int CONTAINER_PROPERTY_LIT_TIME = 1;
    private static final int CONTAINER_PROPERTY_LIT_DURATION = 2;

    private FurnaceInventory inventory;

    private short cookTime;
    private short burnTime;
    private short burnDuration;

    private io.gomint.inventory.item.ItemStack output;

    // Update tracker
    private float updateDF;

    /**
     * Construct new TileEntity from TagCompound
     *
     * @param tagCompound The TagCompound which should be used to read data from
     * @param world       The world in which this TileEntity resides
     */
    public FurnaceTileEntity( NBTTagCompound tagCompound, WorldAdapter world ) {
        super( tagCompound, world );

        this.inventory = new FurnaceInventory( this );
        this.inventory.addObserver( pair -> {
            if ( pair.getFirst() == 0 ) {
                // Input slot has changed
                onInputChanged( pair.getSecond() );
            }
        } );

        List<Object> itemCompounds = tagCompound.getList( "Items", false );
        if ( itemCompounds != null ) {
            for ( Object itemCompound : itemCompounds ) {
                NBTTagCompound cd = (NBTTagCompound) itemCompound;

                byte slot = cd.getByte( "Slot", (byte) -1 );
                if ( slot == -1 ) {
                    this.inventory.addItem( getItemStack( cd ) );
                } else {
                    this.inventory.setItem( slot, getItemStack( cd ) );

                    if ( slot == 0 ) {
                        checkForRecipe( this.inventory.getItem( 0 ) );
                    }
                }
            }
        }

        this.cookTime = tagCompound.getShort( "CookTime", (short) 0 );
        this.burnTime = tagCompound.getShort( "BurnTime", (short) 0 );
        this.burnDuration = tagCompound.getShort( "BurnDuration", (short) 0 );
    }

    private void onInputChanged( io.gomint.inventory.item.ItemStack input ) {
        // If we currently smelt reset progress
        if ( this.cookTime > 0 ) {
            this.cookTime = 0;

            for ( Entity viewer : this.inventory.getViewers() ) {
                if ( viewer instanceof io.gomint.server.entity.EntityPlayer ) {
                    this.sendTickProgress( (io.gomint.server.entity.EntityPlayer) viewer );
                }
            }
        }

        // Check for new recipe
        this.checkForRecipe( input );
    }

    private void checkForRecipe( io.gomint.inventory.item.ItemStack input ) {
        // Reset just to be sure that the new item needs to have a new recipe
        this.output = null;

        // Check if there is a smelting recipe present
        GoMintServer server = (GoMintServer) GoMint.instance();
        SmeltingRecipe recipe = server.getRecipeManager().getSmeltingRecipe( input );
        if ( recipe != null ) {
            for ( io.gomint.inventory.item.ItemStack stack : recipe.createResult() ) {
                this.output = stack; // Smelting only has one result
            }
        }
    }

    @Override
    public void update( long currentMillis ) {
        // Check if we "crafted"
        if ( this.output != null && this.burnTime > 0 ) {
            this.cookTime++;

            if ( this.cookTime >= 200 ) {
                // We did it
                ItemStack itemStack = (ItemStack) this.inventory.getItem( 2 );
                if ( itemStack.getType() != this.output.getType() ) {
                    this.inventory.setItem( 2, this.output );
                } else {
                    itemStack.setAmount( itemStack.getAmount() + this.output.getAmount() );
                    this.inventory.setItem( 2, itemStack );
                }

                this.cookTime = 0;
                this.broadcastCookTime();
            } else if ( this.cookTime % 20 == 0 ) {
                this.broadcastCookTime();
            }
        }

        // Check if we have fuel loaded
        if ( this.burnDuration > 0 ) {
            this.burnTime--;

            // Check if we can refuel
            boolean didRefuel = false;
            if ( this.burnTime == 0 ) {
                this.burnDuration = 0;
                if ( this.checkForRefuel() ) {
                    didRefuel = true;
                    this.broadcastFuelInfo();
                }
            }

            // Broadcast data
            if ( !didRefuel && ( this.burnTime == 0 || this.burnTime % 20 == 0 ) ) {
                this.broadcastFuelInfo();
            }
        } else {
            if ( this.checkForRefuel() ) {
                this.broadcastFuelInfo();
            }
        }
    }

    private void broadcastCookTime() {
        for ( Entity viewer : this.inventory.getViewers() ) {
            if ( viewer instanceof io.gomint.server.entity.EntityPlayer ) {
                this.sendTickProgress( (io.gomint.server.entity.EntityPlayer) viewer );
            }
        }
    }

    private void broadcastFuelInfo() {
        for ( Entity viewer : this.inventory.getViewers() ) {
            if ( viewer instanceof io.gomint.server.entity.EntityPlayer ) {
                this.sendFuelInfo( (io.gomint.server.entity.EntityPlayer) viewer );
            }
        }
    }

    private boolean checkForRefuel() {
        // We need a recipe to load fuel
        if ( this.canProduceOutput() ) {
            io.gomint.inventory.item.ItemStack fuelItem = this.inventory.getItem( 1 );
            if ( fuelItem instanceof ItemBurnable ) {
                long duration = ( (ItemBurnable) fuelItem ).getBurnTime();

                if ( fuelItem.getAmount() > 0 ) {
                    ItemStack itemStack = (ItemStack) fuelItem;
                    itemStack.afterPlacement();

                    this.burnDuration = (short) ( duration / 50 );
                    this.burnTime = this.burnDuration;

                    return true;
                }
            }
        }

        return false;
    }

    private boolean canProduceOutput() {
        // Do we have a recipe loaded?
        if ( this.output == null ) {
            return false;
        }

        // Do we have enough input?


        // Do we have enough space in the output slot for this
        io.gomint.inventory.item.ItemStack itemStack = this.inventory.getItem( 2 );
        if ( itemStack.getType() == this.output.getType() ) {
            return itemStack.getAmount() + this.output.getAmount() <= itemStack.getMaximumAmount();
        }

        return true;
    }

    @Override
    public void interact( Entity entity, BlockFace face, Vector facePos, io.gomint.inventory.item.ItemStack item ) {
        if ( entity instanceof EntityPlayer ) {
            ( (EntityPlayer) entity ).openInventory( this.inventory );

            // Send the needed container data
            this.sendDataProperties( (io.gomint.server.entity.EntityPlayer) entity );
        }
    }

    private void sendTickProgress( io.gomint.server.entity.EntityPlayer player ) {
        byte windowId = player.getWindowId( this.inventory );

        PacketSetContainerData containerData = new PacketSetContainerData();
        containerData.setWindowId( windowId );
        containerData.setKey( CONTAINER_PROPERTY_TICK_COUNT );
        containerData.setValue( this.cookTime );
        player.getConnection().addToSendQueue( containerData );
    }

    private void sendFuelInfo( io.gomint.server.entity.EntityPlayer player ) {
        byte windowId = player.getWindowId( this.inventory );

        PacketSetContainerData containerData = new PacketSetContainerData();
        containerData.setWindowId( windowId );
        containerData.setKey( CONTAINER_PROPERTY_LIT_TIME );
        containerData.setValue( this.burnTime );
        player.getConnection().addToSendQueue( containerData );

        containerData = new PacketSetContainerData();
        containerData.setWindowId( windowId );
        containerData.setKey( CONTAINER_PROPERTY_LIT_DURATION );
        containerData.setValue( this.burnDuration );
        player.getConnection().addToSendQueue( containerData );
    }

    private void sendDataProperties( io.gomint.server.entity.EntityPlayer player ) {
        byte windowId = player.getWindowId( this.inventory );

        PacketSetContainerData containerData = new PacketSetContainerData();
        containerData.setWindowId( windowId );
        containerData.setKey( CONTAINER_PROPERTY_TICK_COUNT );
        containerData.setValue( this.cookTime );
        player.getConnection().addToSendQueue( containerData );

        containerData = new PacketSetContainerData();
        containerData.setWindowId( windowId );
        containerData.setKey( CONTAINER_PROPERTY_LIT_TIME );
        containerData.setValue( this.burnTime );
        player.getConnection().addToSendQueue( containerData );

        containerData = new PacketSetContainerData();
        containerData.setWindowId( windowId );
        containerData.setKey( CONTAINER_PROPERTY_LIT_DURATION );
        containerData.setValue( this.burnDuration );
        player.getConnection().addToSendQueue( containerData );
    }

    @Override
    public void toCompound( NBTTagCompound compound, SerializationReason reason ) {
        super.toCompound( compound, reason );

        compound.addValue( "id", "Furnace" );

        if ( reason == SerializationReason.PERSIST ) {
            List<NBTTagCompound> itemCompounds = new ArrayList<>();
            for ( int i = 0; i < this.inventory.size(); i++ ) {
                ItemStack itemStack = (ItemStack) this.inventory.getItem( i );
                if ( !( itemStack instanceof ItemAir ) ) {
                    NBTTagCompound itemCompound = new NBTTagCompound( "" );
                    itemCompound.addValue( "Slot", (byte) i );
                    putItemStack( itemStack, itemCompound );
                    itemCompounds.add( itemCompound );
                }
            }

            compound.addValue( "Items", itemCompounds );

            compound.addValue( "CookTime", this.cookTime );
            compound.addValue( "BurnTime", this.burnTime );
            compound.addValue( "BurnDuration", this.burnDuration );
        }
    }

}
