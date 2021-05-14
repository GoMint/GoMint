package io.gomint.server.network.handler;

import io.gomint.event.player.PlayerDropItemEvent;
import io.gomint.event.player.PlayerExhaustEvent;
import io.gomint.event.player.PlayerInteractEvent;
import io.gomint.event.player.PlayerInteractWithEntityEvent;
import io.gomint.event.world.BlockBreakEvent;
import io.gomint.inventory.item.ItemStack;
import io.gomint.inventory.item.ItemSword;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.inventory.ContainerInventory;
import io.gomint.server.inventory.Inventory;
import io.gomint.server.inventory.item.ItemBow;
import io.gomint.server.inventory.item.ItemCrossbow;
import io.gomint.server.inventory.transaction.DropItemTransaction;
import io.gomint.server.inventory.transaction.InventoryTransaction;
import io.gomint.server.inventory.transaction.TransactionGroup;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.PacketInventoryTransaction;
import io.gomint.server.plugin.EventCaller;
import io.gomint.server.util.Values;
import io.gomint.server.world.block.Block;
import io.gomint.world.Gamemode;
import io.gomint.world.block.BlockAir;
import io.gomint.world.block.data.Facing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PacketInventoryTransactionHandler implements PacketHandler<PacketInventoryTransaction> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketInventoryTransactionHandler.class);

    @Override
    public void handle(PacketInventoryTransaction packet, long currentTimeMillis, PlayerConnection connection) {
        switch (packet.getType()) {
            case PacketInventoryTransaction.TYPE_NORMAL:
                this.handleTypeNormal(connection, packet);
                break;

            case PacketInventoryTransaction.TYPE_USE_ITEM:  // Interact / Build
            case PacketInventoryTransaction.TYPE_RELEASE_ITEM: // Consume / use item
                // Sanity checks before we interact with worlds and their chunk loading
                Vector packetPosition = packet.getPlayerPosition().add(0, -connection.entity().eyeHeight(), 0);
                Vector playerPosition = connection.entity().position();

                double distance = packetPosition.distanceSquared(playerPosition);
                double offsetLimit = 0.5;
                if (distance > offsetLimit) {
                    Block block = connection.entity().world().blockAt(playerPosition.toBlockPosition());
                    LOGGER.warn("Mismatching position: {} -> {} / {}", distance, playerPosition, block.getClass().getSimpleName());
                    reset(packet, connection);
                    return;
                }

                // Special case in air clicks
                if (packet.getType() == PacketInventoryTransaction.TYPE_USE_ITEM && packet.getActionType() != 1) {
                    // Check if we can interact
                    Vector interactCheckVector = packet.getBlockPosition().toVector().add(.5f, .5f, .5f);
                    if (!connection.entity().canInteract(interactCheckVector, 13) ||
                        connection.entity().gamemode() == Gamemode.SPECTATOR) {
                        LOGGER.warn("Can't interact from position: {} / {}", connection.entity().position(), packet.getBlockPosition());
                        reset(packet, connection);
                        return;
                    }
                }

                // Check if in hand item is in sync
                ItemStack<?> itemInHand = connection.entity().inventory().itemInHand();
                ItemStack<?> packetItemInHand = packet.getItemInHand();
                if (itemInHand.itemType() != packetItemInHand.itemType()) {
                    LOGGER.debug("{} item in hand does not match: {} / {}", connection.entity().name(), itemInHand, packetItemInHand);
                    reset(packet, connection);
                    return;
                }

                if (packet.getType() == PacketInventoryTransaction.TYPE_USE_ITEM) {
                    this.handleUseItem(itemInHand, connection, packet);
                } else {
                    this.handleConsumeItem(itemInHand, connection, packet);
                }

                break;
            case PacketInventoryTransaction.TYPE_USE_ITEM_ON_ENTITY:
                // Check item in hand
                itemInHand = connection.entity().inventory().itemInHand();
                packetItemInHand = packet.getItemInHand();
                if (itemInHand.itemType() != packetItemInHand.itemType()) {
                    LOGGER.warn("{} item in hand does not match (X): {} / {}", connection.entity().name(), itemInHand, packetItemInHand);
                    reset(packet, connection);
                    return;
                }

                // When the player wants to do this it should have selected a entity in its interact
                if (connection.entity().hoverEntity() == null) {
                    LOGGER.warn("{} selected entity is null", connection.entity().name());
                    reset(packet, connection);
                    return;
                }

                // Find the entity from this packet
                io.gomint.entity.Entity<?> entity = connection.entity().world().findEntity(packet.getEntityId());
                if (!connection.entity().hoverEntity().equals(entity)) {
                    LOGGER.warn("{} entity does not match: {}; {}; {}", connection.entity().name(), entity, connection.entity().hoverEntity(), packet.getEntityId());
                    reset(packet, connection);
                    return;
                }

                // Fast check for interact rules
                Vector interactCheckVector = packet.getVector1().add(.5f, .5f, .5f);
                if (!connection.entity().canInteract(interactCheckVector, 8) ||
                    connection.entity().gamemode() == Gamemode.SPECTATOR) {
                    LOGGER.warn("Can't interact from position");
                    reset(packet, connection);
                    return;
                }

                this.handleUseItemOnEntity(entity, connection, packet);

                break;
            default:
                connection.entity().setUsingItem(false);
                break;
        }
    }

    private void handleUseItemOnEntity(io.gomint.entity.Entity <?>target, PlayerConnection connection, PacketInventoryTransaction packet) {
        switch (packet.getActionType()) {
            case 0:     // Interact
                io.gomint.entity.Entity<?> entity = connection.entity().world().findEntity(packet.getEntityId());

                PlayerInteractWithEntityEvent event = new PlayerInteractWithEntityEvent(connection.entity(), entity);

                connection.server().pluginManager().callEvent(event);

                if (event.cancelled()) {
                    break;
                }

                entity.interact(connection.entity(), packet.getVector2());

                break;
            case 1:     // Attack
                if (connection.entity().attackWithItemInHand(target)) {
                    if (connection.entity().gamemode() != Gamemode.CREATIVE) {
                        ItemStack<?> itemInHand = connection.entity().inventory().itemInHand();
                        ((io.gomint.server.inventory.item.ItemStack<?>) itemInHand).calculateUsageAndUpdate(1);
                    }
                } else {
                    reset(packet, connection);
                }

                break;
            default:
                break;
        }
    }

    private void handleConsumeItem(ItemStack<?> itemInHand, PlayerConnection connection, PacketInventoryTransaction packet) {
        LOGGER.debug("Action Type: {}", packet.getActionType());
        if (packet.getActionType() == 0) { // Release item ( shoot bow )
            // Check if the item is a bow
            if (itemInHand instanceof ItemBow) {
                ((ItemBow) itemInHand).shoot(connection.entity());
            } else if (itemInHand instanceof ItemCrossbow) {
                ((ItemCrossbow) itemInHand).shoot(connection.entity());
            }
        }

        connection.entity().setUsingItem(false);
    }

    private void handleUseItem(ItemStack<?> itemInHand, PlayerConnection connection, PacketInventoryTransaction packet) {
        switch (packet.getActionType()) {
            case 0: // Click on block
                // Only accept valid interactions
                if (!checkInteraction(packet, connection)) {
                    return;
                }

                connection.entity().setUsingItem(false);

                if (!connection.entity().world().useItemOn(itemInHand, packet.getBlockPosition(), packet.getFace(),
                    packet.getClickPosition(), connection.entity())) {
                    reset(packet, connection);
                    return;
                }

                break;
            case 1: // Click in air
                // Only accept valid interactions
                if (!checkInteraction(packet, connection)) {
                    return;
                }

                // Only send interact events, there is nothing special to be done in here
                PlayerInteractEvent event = new PlayerInteractEvent(connection.entity(), PlayerInteractEvent.ClickType.RIGHT, null);
                connection.server().pluginManager().callEvent(event);

                if (!event.cancelled()) {
                    io.gomint.server.inventory.item.ItemStack<?> itemStack = (io.gomint.server.inventory.item.ItemStack<?>) connection.entity().inventory().itemInHand();
                    itemStack.interact(connection.entity(), null, packet.getClickPosition(), null);
                } else {
                    reset(packet, connection);
                }

                break;
            case 2: // Break block
                // Breaking blocks too fast / missing start_break
                if (connection.entity().gamemode() != Gamemode.CREATIVE && connection.entity().breakVector() == null) {
                    reset(packet, connection);
                    LOGGER.debug("Breaking block without break vector");
                    return;
                }

                if (connection.entity().gamemode() != Gamemode.CREATIVE) {
                    // Check for transactions first
                    if (packet.getActions().length > 1) {
                        // This can only have 0 or 1 transaction
                        reset(packet, connection);
                        connection.entity().breakVector(null);
                        return;
                    }

                    // The transaction can only affect the in hand item
                    if (packet.getActions().length > 0) {
                        ItemStack<?> source = packet.getActions()[0].getOldItem();
                        if (!source.equals(itemInHand) || source.amount() != itemInHand.amount()) {
                            // Transaction is invalid
                            reset(packet, connection);
                            connection.entity().breakVector(null);
                            return;
                        }

                        // Check if target item is either the same item or air
                        io.gomint.server.inventory.item.ItemStack<?> target = (io.gomint.server.inventory.item.ItemStack<?>) packet.getActions()[0].getNewItem();
                        if (!target.material().equals(((io.gomint.server.inventory.item.ItemStack<?>) itemInHand).material()) && !target.isAir()) {
                            // Transaction is invalid
                            reset(packet, connection);
                            connection.entity().breakVector(null);
                            return;
                        }
                    }
                }

                // Transaction seems valid
                io.gomint.server.world.block.Block block = connection.entity().world().blockAt(connection.entity().gamemode() == Gamemode.CREATIVE ? packet.getBlockPosition() : connection.entity().breakVector());
                if (block != null) {
                    BlockBreakEvent blockBreakEvent = new BlockBreakEvent(connection.entity(), block, connection.entity().gamemode() == Gamemode.CREATIVE ? new ArrayList<>() : block.drops(itemInHand));
                    blockBreakEvent.cancelled(connection.entity().gamemode() == Gamemode.ADVENTURE); // TODO: Better handling for canBreak rules for adventure gamemode

                    connection.entity().world().server().pluginManager().callEvent(blockBreakEvent);
                    if (blockBreakEvent.cancelled()) {
                        reset(packet, connection);
                        connection.entity().breakVector(null);
                        return;
                    }

                    // Check for special break rights (creative)
                    if (connection.entity().gamemode() == Gamemode.CREATIVE) {
                        if (connection.entity().world().breakBlock(packet.getBlockPosition(), blockBreakEvent.drops(), true)) {
                            block.blockType(BlockAir.class);
                            connection.entity().breakVector(null);
                        } else {
                            reset(packet, connection);
                            connection.entity().breakVector(null);
                        }

                        return;
                    }

                    // Check if we can break this block in time
                    long breakTime = block.finalBreakTime(connection.entity().inventory().itemInHand(), connection.entity());
                    if (breakTime > Values.CLIENT_TICK_MS && ((connection.entity().breakTime() + Values.CLIENT_TICK_MS) / (double) breakTime) < 0.75) {
                        LOGGER.warn(connection.entity().name() + " broke block too fast: break time: " + (connection.entity().breakTime() + Values.CLIENT_TICK_MS) +
                            "; should: " + breakTime + " for " + block.getClass().getSimpleName() + " with " + itemInHand.getClass().getSimpleName());
                        reset(packet, connection);
                        connection.entity().breakVector(null);
                    } else {
                        if (connection.entity().world().breakBlock(connection.entity().breakVector(), blockBreakEvent.drops(), false)) {
                            // Add exhaustion
                            connection.entity().exhaust(0.025f, PlayerExhaustEvent.Cause.MINING);

                            // Damage the target item
                            if (breakTime > Values.CLIENT_TICK_MS) {
                                int damage = 1;

                                // Swords get 2 calculateUsage
                                if (itemInHand instanceof ItemSword) {
                                    damage = 2;
                                }

                                io.gomint.server.inventory.item.ItemStack<?> itemStack = (io.gomint.server.inventory.item.ItemStack<?>) itemInHand;
                                itemStack.calculateUsageAndUpdate(damage);
                            }
                        } else {
                            reset(packet, connection);
                            return;
                        }

                        connection.entity().breakVector(null);
                    }
                } else {
                    reset(packet, connection);
                    connection.entity().breakVector(null);
                }

                break;
            default:
                break;
        }

    }

    private boolean checkInteraction(PacketInventoryTransaction packet, PlayerConnection connection) {
        // We cache all packets for this tick so we don't handle one twice, vanilla likes spam (https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/Spam_can.png/220px-Spam_can.png)
        if (connection.transactionsHandled().contains(packet)) {
            return false;
        }

        connection.transactionsHandled().add(packet);
        return true;
    }

    private void handleTypeNormal(PlayerConnection connection, PacketInventoryTransaction packet) {
        connection.entity().setUsingItem(false);

        TransactionGroup transactionGroup = new TransactionGroup(connection.entity());
        for (PacketInventoryTransaction.NetworkTransaction transaction : packet.getActions()) {
            Inventory<?> inventory = getInventory(transaction, connection.entity());

            switch (transaction.getSourceType()) {
                case 99999:
                case 100:
                case 0:
                    // Normal inventory stuff
                    InventoryTransaction<?,?,?> inventoryTransaction = new InventoryTransaction<>(connection.entity(),
                        inventory, transaction.getSlot(), transaction.getOldItem(), transaction.getNewItem(), (byte) 0);
                    transactionGroup.addTransaction(inventoryTransaction);
                    break;
                case 2:
                    // Drop item
                    PlayerDropItemEvent playerDropItemEvent = new PlayerDropItemEvent(connection.entity(), transaction.getNewItem());
                    connection.server().pluginManager().callEvent(playerDropItemEvent);
                    if (!playerDropItemEvent.cancelled()) {
                        DropItemTransaction<?> dropItemTransaction = new DropItemTransaction<>(
                            connection.entity().location().add(0, connection.entity().eyeHeight(), 0),
                            connection.entity().direction().normalize().multiply(0.4f),
                            transaction.getNewItem());
                        transactionGroup.addTransaction(dropItemTransaction);
                    } else {
                        reset(packet, connection);
                    }

                    break;
                default:
                    break;
            }
        }

        transactionGroup.execute(connection.entity().gamemode() == Gamemode.CREATIVE);
    }

    private Inventory<?> getInventory(PacketInventoryTransaction.NetworkTransaction transaction, EntityPlayer entity) {
        Inventory<?> inventory = null;
        switch (transaction.getWindowId()) {
            case 0:     // EntityPlayer window id
                inventory = entity.inventory();
                break;
            case 119:
                inventory = entity.getOffhandInventory();
                break;
            case 120:   // Armor window id
                inventory = entity.armorInventory();
                break;
            case 124:   // Cursor window id
                if (transaction.getSlot() > 0) {
                    // WTF is slot 50?!
                    if (transaction.getSlot() == 50) {
                        transaction.setSlot(0);
                        inventory = entity.getCursorInventory();
                    } else {

                        // Crafting input
                        if (transaction.getSlot() >= 28 && transaction.getSlot() <= 31) {
                            inventory = entity.craftingInventory();
                            if (inventory.size() != 4) {
                                LOGGER.warn("Crafting inventory is not correctly sized");
                                return null;
                            }

                            transaction.setSlot(transaction.getSlot() - 28);
                        } else if (transaction.getSlot() >= 32 && transaction.getSlot() <= 40) {
                            inventory = entity.craftingInventory();
                            if (inventory.size() != 9) {
                                LOGGER.warn("Crafting inventory is not correctly sized");
                                return null;
                            }

                            transaction.setSlot(transaction.getSlot() - 32);
                        }
                    }
                } else {
                    inventory = entity.getCursorInventory();
                }
                break;

            default:
                // Check for container windows
                ContainerInventory<?> containerInventory = entity.getContainerId((byte) transaction.getWindowId());
                if (containerInventory != null) {
                    inventory = containerInventory;
                } else {
                    LOGGER.warn("Unknown window id: {}", transaction.getWindowId());
                }
        }

        return inventory;
    }

    private void reset(PacketInventoryTransaction packet, PlayerConnection connection) {
        // Reset whole inventory
        connection.entity().inventory().sendContents(connection);
        connection.entity().getOffhandInventory().sendContents(connection);
        connection.entity().armorInventory().sendContents(connection);

        // Does the viewer currently see any additional inventory?
        if (connection.entity().currentOpenContainer() != null) {
            connection.entity().currentOpenContainer().sendContents(connection);
        }

        // Now check if we need to reset blocks
        if (packet.getBlockPosition() != null) {
            io.gomint.server.world.block.Block blockClicked = connection.entity().world().blockAt(packet.getBlockPosition());
            blockClicked.send(connection);

            if (packet.getFace() != null) {
                // Attach to block send queue
                io.gomint.server.world.block.Block replacedBlock = blockClicked.side(packet.getFace());
                replacedBlock.send(connection);

                for (Facing face : Facing.values()) {
                    io.gomint.server.world.block.Block replacedSide = replacedBlock.side(face);
                    replacedSide.send(connection);
                }
            }
        }

        // Resend attributes for consumptions
        connection.entity().resendAttributes();
    }

}
