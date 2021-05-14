/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.entity;

import io.gomint.ChatColor;
import io.gomint.GoMint;
import io.gomint.command.CommandOutput;
import io.gomint.command.PlayerCommandSender;
import io.gomint.enchant.EnchantmentKnockback;
import io.gomint.enchant.EnchantmentSharpness;
import io.gomint.entity.Entity;
import io.gomint.entity.potion.PotionEffect;
import io.gomint.event.entity.EntityDamageByEntityEvent;
import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.event.entity.EntityTeleportEvent;
import io.gomint.event.inventory.InventoryCloseEvent;
import io.gomint.event.inventory.InventoryOpenEvent;
import io.gomint.event.player.PlayerDeathEvent;
import io.gomint.event.player.PlayerExhaustEvent;
import io.gomint.event.player.PlayerFoodLevelChangeEvent;
import io.gomint.event.player.PlayerJoinEvent;
import io.gomint.event.player.PlayerMoveEvent;
import io.gomint.event.player.PlayerRespawnEvent;
import io.gomint.event.player.PlayerWorldJoinEvent;
import io.gomint.event.player.PlayerWorldLeaveEvent;
import io.gomint.gui.Form;
import io.gomint.gui.FormListener;
import io.gomint.math.AxisAlignedBB;
import io.gomint.math.BlockPosition;
import io.gomint.math.Location;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.math.Vector2;
import io.gomint.player.ChatType;
import io.gomint.player.DeviceInfo;
import io.gomint.plugin.Plugin;
import io.gomint.server.entity.metadata.MetadataContainer;
import io.gomint.server.entity.passive.EntityHuman;
import io.gomint.server.entity.projectile.EntityFishingHook;
import io.gomint.server.inventory.ArmorInventory;
import io.gomint.server.inventory.ContainerInventory;
import io.gomint.server.inventory.CraftingInputInventory;
import io.gomint.server.inventory.CursorInventory;
import io.gomint.server.inventory.EnderChestInventory;
import io.gomint.server.inventory.Inventory;
import io.gomint.server.inventory.InventoryHolder;
import io.gomint.server.inventory.OffhandInventory;
import io.gomint.server.inventory.OneSlotInventory;
import io.gomint.server.inventory.PlayerInventory;
import io.gomint.server.inventory.WindowMagicNumbers;
import io.gomint.server.inventory.item.ItemAir;
import io.gomint.server.inventory.item.ItemStack;
import io.gomint.server.maintenance.performance.LoginPerformance;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.PlayerConnectionState;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketAvailableCommands;
import io.gomint.server.network.packet.PacketAvailableEntityIdentifiers;
import io.gomint.server.network.packet.PacketBiomeDefinitionList;
import io.gomint.server.network.packet.PacketContainerClose;
import io.gomint.server.network.packet.PacketEntityEvent;
import io.gomint.server.network.packet.PacketItemComponent;
import io.gomint.server.network.packet.PacketModalRequest;
import io.gomint.server.network.packet.PacketPlayState;
import io.gomint.server.network.packet.PacketPlayerlist;
import io.gomint.server.network.packet.PacketRespawnPosition;
import io.gomint.server.network.packet.PacketRespawnPosition.RespawnState;
import io.gomint.server.network.packet.PacketServerSettingsResponse;
import io.gomint.server.network.packet.PacketSetGamemode;
import io.gomint.server.network.packet.PacketSetTitle;
import io.gomint.server.network.packet.PacketSpawnPlayer;
import io.gomint.server.network.packet.PacketText;
import io.gomint.server.network.packet.PacketTransfer;
import io.gomint.server.network.packet.PacketUpdateAttributes;
import io.gomint.server.network.packet.PacketWorldEvent;
import io.gomint.server.permission.PermissionManager;
import io.gomint.server.player.EntityVisibilityManager;
import io.gomint.server.plugin.EventCaller;
import io.gomint.server.scoreboard.Scoreboard;
import io.gomint.server.util.CallerDetectorUtil;
import io.gomint.server.util.EnumConnectors;
import io.gomint.server.util.Precondition;
import io.gomint.server.util.Values;
import io.gomint.server.world.ChunkAdapter;
import io.gomint.server.world.CoordinateUtils;
import io.gomint.server.world.LevelEvent;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.Block;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Chunk;
import io.gomint.world.Gamemode;
import io.gomint.world.Particle;
import io.gomint.world.ParticleData;
import io.gomint.world.Sound;
import io.gomint.world.SoundData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

/**
 * The entity implementation for players. Players are considered living entities even though they
 * do not possess an AI. But as they still move around freely and in an unpredictable fashion
 * (and because we do hope players playing on GoMint actually are living entities) EntityPlayer
 * still inherits from EntityLiving. Their attached behaviour will simply contain no AI states
 * and will not be started either.
 *
 * @author BlackyPaw
 * @version 1.0
 */
public class EntityPlayer extends EntityHuman<io.gomint.entity.EntityPlayer> implements io.gomint.entity.EntityPlayer, InventoryHolder, PlayerCommandSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityPlayer.class);

    private final PlayerConnection connection;
    private io.gomint.permission.PermissionManager permissionManager;
    private boolean isUsingDefaultPermissionManager;
    private final EntityVisibilityManager entityVisibilityManager = new EntityVisibilityManager(this);
    private int viewDistance = 4;
    private Queue<ChunkAdapter> chunkSendQueue = new LinkedBlockingQueue<>();
    private boolean hasCompletedLogin;

    // EntityPlayer Information
    private Gamemode gamemode = Gamemode.SURVIVAL;
    private AdventureSettings adventureSettings;
    private Entity<?> hoverEntity;
    private Location respawnPosition = null;
    private Locale locale;

    // Hidden players
    private Set<Long> hiddenPlayers;

    // Container handling
    private ContainerInventory<?> currentOpenContainer;

    // Inventory
    private Inventory<?> cursorInventory;
    private Inventory<?> offhandInventory;
    private EnderChestInventory enderChestInventory;

    // Crafting
    private Inventory<?> craftingInventory;
    private CraftingInputInventory craftingInputInventory;
    private Inventory<?> craftingResultInventory;

    // Block break data
    private BlockPosition breakVector;
    private long startBreak;
    private long breakTime;

    // Update data
    private Set<BlockPosition> blockUpdates = new HashSet<>();
    private Location teleportPosition = null;

    // Form stuff
    private int formId;
    private Int2ObjectMap<io.gomint.server.gui.Form<?>> forms = new Int2ObjectOpenHashMap<>();
    private Int2ObjectMap<io.gomint.server.gui.FormListener<?>> formListeners = new Int2ObjectOpenHashMap<>();

    // Server settings
    private int serverSettingsForm = -1;

    // Entity data
    private EntityFishingHook fishingHook;
    private long lastPickupXP;
    private Location spawnLocation;

    // Item usage ticking
    private long actionStart = -1;

    // Exp
    private int xp;

    // Performance metrics
    private LoginPerformance loginPerformance;

    // Movement delay
    private Location nextMovement;
    private boolean spawnPlayers;

    // Scoreboard
    private Scoreboard scoreboard;

    private final EventCaller eventCaller;
    private long enchantmentSeed;

    /**
     * Constructs a new player entity which will be spawned inside the specified world.
     *
     * @param world      The world the entity should spawn in
     * @param connection The specific player connection associated with this entity
     * @param username   The name the user has chosen
     * @param xboxId     The xbox id from xbox live which has logged in
     * @param uuid       The uuid which has been sent from the client
     * @param locale     language of the player
     */
    public EntityPlayer(WorldAdapter world,
                        PlayerConnection connection,
                        String username,
                        String xboxId,
                        UUID uuid,
                        Locale locale,
                        EventCaller eventCaller) {
        super(EntityType.PLAYER, world);

        this.connection = connection;
        this.eventCaller = eventCaller;
        this.generateNewEnchantmentSeed();

        // EntityHuman stuff
        this.setPlayerData(username, username, xboxId, uuid);

        this.locale = locale;
        this.adventureSettings = new AdventureSettings(this);

        // Performance stuff
        this.loginPerformance = new LoginPerformance();

        // Permission stuff
        this.permissionManager = new PermissionManager(this);
        this.isUsingDefaultPermissionManager = true;
    }

    // ==================================== ACCESSORS ==================================== //

    /**
     * Gets the view distance set by the player.
     *
     * @return The view distance set by the player
     */
    @Override
    public int viewDistance() {
        return this.viewDistance;
    }

    /**
     * Sets the view distance used to calculate the chunk to be sent to the player.
     *
     * @param viewDistance The view distance to set
     */
    public void setViewDistance(int viewDistance) {
        int tempViewDistance = Math.min(viewDistance, this.world.config().viewDistance());
        if (this.viewDistance != tempViewDistance) {
            this.viewDistance = tempViewDistance;
        }

        if (this.connection.state() == PlayerConnectionState.LOGIN) {
            this.connection.addToSendQueue(new PacketBiomeDefinitionList());
            this.connection.sendPlayState(PacketPlayState.PlayState.SPAWN);
            this.loginPerformance().setChunkStart(this.world.currentTickTime());
        }

        this.connection.onViewDistanceChanged();
    }

    @Override
    public EntityPlayer transfer(String host, int port) {
        PacketTransfer packetTransfer = new PacketTransfer();
        packetTransfer.setAddress(host);
        packetTransfer.setPort(port);
        this.connection.addToSendQueue(packetTransfer);
        return this;
    }

    @Override
    public int ping() {
        return this.connection.ping();
    }

    /**
     * Gets the connection associated with this player entity.
     *
     * @return The connection associated with this player entity
     */
    public PlayerConnection connection() {
        return this.connection;
    }

    private void updateGamemode() {
        int gameModeNumber = EnumConnectors.GAMEMODE_CONNECTOR.convert(this.gamemode).magicNumber();

        PacketSetGamemode packetSetGamemode = new PacketSetGamemode();
        packetSetGamemode.setGameMode(gameModeNumber == 1 ? 1 : 0);
        this.connection.addToSendQueue(packetSetGamemode);

        this.sendAdventureSettings();

        // Set fly
        this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.CAN_FLY,
            this.gamemode == Gamemode.SPECTATOR || this.gamemode == Gamemode.CREATIVE);

        // Set invis
        if (this.gamemode == Gamemode.SPECTATOR) {
            this.invisible(true);
        } else {
            // Check for invis potion effect
            if (!this.hasEffect(PotionEffect.INVISIBILITY)) {
                this.invisible(false);
            }
        }
    }

    /**
     * Send adventure settings based on the gamemode
     */
    public void sendAdventureSettings() {
        int gameModeNumber = EnumConnectors.GAMEMODE_CONNECTOR.convert(this.gamemode).magicNumber();

        // Recalc adventure settings
        this.adventureSettings.setWorldImmutable(gameModeNumber == 0x03);
        this.adventureSettings.setCanFly((gameModeNumber & 0x01) > 0);
        this.adventureSettings.setNoClip(gameModeNumber == 0x03);
        this.adventureSettings.setFlying(gameModeNumber == 0x03);
        this.adventureSettings.setAttackMobs(gameModeNumber < 0x02);
        this.adventureSettings.setAttackPlayers(gameModeNumber < 0x02);
        this.adventureSettings.setNoPvP(gameModeNumber == 0x03);
        this.adventureSettings.update();
    }

    @Override
    public Gamemode gamemode() {
        return this.gamemode;
    }

    @Override
    public EntityPlayer gamemode(Gamemode gamemode) {
        this.gamemode = gamemode;
        this.updateGamemode();
        return this;
    }

    @Override
    public boolean op() {
        return this.adventureSettings.isOperator();
    }

    @Override
    public EntityPlayer op(boolean value) {
        this.adventureSettings.setOperator(value);
        this.sendAdventureSettings();

        // Grant all permissions / notify the permission manager
        this.permissionManager.toggleOp();
        return this;
    }

    @Override
    public EntityPlayer hidePlayer(io.gomint.entity.EntityPlayer player) {
        // Never hide myself (client crashes when this is done)
        if (player.equals(this)) {
            LOGGER.warn("You can't hide a player itself. Please tell the plugin author to remove the hidePlayer call");
            return this;
        }

        EntityPlayer other = (EntityPlayer) player;

        if (this.hiddenPlayers == null) {
            this.hiddenPlayers = new HashSet<>();
        }

        this.hiddenPlayers.add(other.id());

        LOGGER.debug("Player {} hides {} from now on", this.name(), player.name());

        // Remove the entity clientside
        this.entityVisibilityManager.removeEntity(other);

        // Remove from player list
        PacketPlayerlist packetPlayerlist = new PacketPlayerlist();
        packetPlayerlist.setMode((byte) 1);
        packetPlayerlist.setEntries(new ArrayList<>() {{
            add(new PacketPlayerlist.Entry(other));
        }});
        connection().addToSendQueue(packetPlayerlist);

        return this;
    }

    @Override
    public EntityPlayer showPlayer(io.gomint.entity.EntityPlayer player) {
        if (this.hiddenPlayers == null) {
            return this;
        }

        if (this.hiddenPlayers.remove(player.id())) {
            LOGGER.debug("Player {} shows {} from now on", this.name(), player.name());

            EntityPlayer other = (EntityPlayer) player;

            // Send tablist and spawn packet
            PacketPlayerlist packetPlayerlist = new PacketPlayerlist();
            packetPlayerlist.setMode((byte) 0);
            packetPlayerlist.setEntries(new ArrayList<>() {{
                add(new PacketPlayerlist.Entry(other));
            }});
            connection().addToSendQueue(packetPlayerlist);

            // Check bounds
            Chunk playerChunk = this.chunk();
            Chunk chunk = other.chunk();
            if (Math.abs(playerChunk.x() - chunk.x()) <= this.viewDistance() &&
                Math.abs(playerChunk.z() - chunk.z()) <= this.viewDistance()) {
                this.entityVisibilityManager.addEntity(other);
            }
        }

        return this;
    }

    @Override
    public boolean isHidden(io.gomint.entity.EntityPlayer player) {
        return this.hiddenPlayers != null && this.hiddenPlayers.contains(player.id());
    }

    @Override
    public EntityPlayer teleport(Location to, EntityTeleportEvent.Cause cause) {
        Precondition.safeWorldAccess(this.world, false);
        // Only teleport when online
        if (!online()) {
            return this;
        }

        EntityTeleportEvent entityTeleportEvent = new EntityTeleportEvent(this, this.location(), to, cause);
        this.world.server().pluginManager().callEvent(entityTeleportEvent);
        if (entityTeleportEvent.cancelled()) {
            return this;
        }

        Location from = location();

        // Reset chunks
        this.connection.resetQueuedChunks();

        Chunk fromChunk = this.chunk();

        // Check if we need to change worlds
        if (!to.world().equals(from.world())) {
            this.world.server().pluginManager().callEvent(new PlayerWorldLeaveEvent(this, to.world()));

            // Despawn entities first
            this.entityVisibilityManager.clear();

            // Remove from old world
            world().removePlayer(this);

            this.world.syncScheduler().execute(() -> {
                //Add to new world
                this.world((WorldAdapter) to.world());

                // Set the new location
                this.setAndRecalcPosition(to);
                
                // Resend commands (world-restrictions)
                this.sendCommands();

                this.world.server().pluginManager().callEvent(new PlayerWorldJoinEvent(this, from.world()));
                this.world.spawnEntityAt(this, to.x(), to.y(), to.z(), to.yaw(), to.pitch());

                // Be sure to get rid of all loaded chunks
                this.connection.resetPlayerChunks();

                // Send all packets needed for a world switch
                this.world.playerSwitched(this);
                teleport0(fromChunk, from, to);
            });
        } else {
            // Set the new location
            this.setAndRecalcPosition(to);

            teleport0(fromChunk, from, to);
        }
        return this;
    }
    
    private void teleport0(Chunk fromChunk, Location from, Location to) {
        // Check for attached entities
        if (!this.getAttachedEntities().isEmpty()) {
            for (Entity<?> entity : new HashSet<>(this.getAttachedEntities())) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    Chunk playerChunk = player.chunk();
                    if (Math.abs(playerChunk.x() - fromChunk.x()) > player.viewDistance() &&
                        Math.abs(playerChunk.z() - fromChunk.z()) > player.viewDistance()) {
                        player.entityVisibilityManager().removeEntity(this);
                    }
                }
            }
        }

        // Force load the new spawn chunk
        this.chunk(); // This getChunk uses loadChunk so it will generate or load from disc if needed

        // Move the client
        this.connection.sendMovePlayer(to);

        // Send chunks to the client
        this.connection.checkForNewChunks(from, true);
        this.fallDistance = 0;

        // Tell the movement handler to force this position to the client
        this.teleportPosition = to;
    }

    @Override
    public EntityPlayer addHunger(float amount) {
        PlayerFoodLevelChangeEvent foodLevelChangeEvent = new PlayerFoodLevelChangeEvent(
            this, amount
        );
        this.world.server().pluginManager().callEvent(foodLevelChangeEvent);

        if (!foodLevelChangeEvent.cancelled()) {
            super.addHunger(amount);
        } else {
            this.resendAttributes();
        }

        return this;
    }

    /**
     * Queue which holds chunks to be sent to the client
     *
     * @return queue with chunks to be sent to the client
     */
    public Queue<ChunkAdapter> chunkSendQueue() {
        return this.chunkSendQueue;
    }

    // ==================================== UPDATING ==================================== //

    @Override
    public void update(long currentTimeMS, float dT) {
        // Move first
        if (this.nextMovement != null) {
            Location from = this.location();
            PlayerMoveEvent playerMoveEvent = this.eventCaller.callEvent(new PlayerMoveEvent(this, from, this.nextMovement));

            if (playerMoveEvent.cancelled()) {
                playerMoveEvent.to(playerMoveEvent.from());
            }

            Location to = playerMoveEvent.to();
            if (to.x() != this.nextMovement.x() || to.y() != this.nextMovement.y() || to.z() != this.nextMovement.z() ||
                !to.world().equals(this.nextMovement.world()) || to.yaw() != this.nextMovement.yaw() ||
                to.pitch() != this.nextMovement.pitch() || to.headYaw() != this.nextMovement.headYaw()) {
                this.teleport(to);
            } else {
                float moveX = to.x() - from.x();
                float moveY = to.y() - from.y();
                float moveZ = to.z() - from.z();

                // Try to at least move the gravitation down
                Vector moved = this.safeMove(moveX, moveY, moveZ);

                // Exhaustion
                float distance = (float) Math.sqrt(moved.x() * moved.x() + moved.z() * moved.z());
                if (distance > 0.01f) {
                    if (this.onGround) {
                        if (this.sprinting()) {
                            this.exhaust((0.1f * distance), PlayerExhaustEvent.Cause.SPRINTING);
                        } else if (this.onGround()) {
                            this.exhaust((0.01f * distance), PlayerExhaustEvent.Cause.WALKING);
                        }
                    }
                }

                this.pitch(to.pitch());
                this.yaw(to.yaw());
                this.headYaw(to.headYaw());
            }

            boolean changeWorld = !to.world().equals(from.world());
            boolean changeXZ = (int) from.x() != (int) to.x() || (int) from.z() != (int) to.z();
            boolean changeY = (int) from.y() != (int) to.y();

            if (changeWorld || changeXZ || changeY) {
                if (changeWorld || changeXZ) {
                    this.connection.checkForNewChunks(from, false);
                }

                // Check for interaction
                Block block = from.world().blockAt(from.toBlockPosition());
                block.gotOff(this);

                block = to.world().blockAt(to.toBlockPosition());
                block.stepOn(this);
            }

            this.nextMovement = null;
        }

        super.update(currentTimeMS, dT);

        // Update permissions
        if (this.isUsingDefaultPermissionManager) {
            ((PermissionManager) this.permissionManager).update(currentTimeMS, dT);
        }

        if (this.dead() || this.health() <= 0) {
            return;
        }

        // Look around
        Collection<Entity<?>> nearbyEntities = this.world.getNearbyEntities(this.boundingBox.grow(1, 0.5f, 1), this);
        if (nearbyEntities != null) {
            for (Entity<?> nearbyEntity : nearbyEntities) {
                io.gomint.server.entity.Entity<?> implEntity = (io.gomint.server.entity.Entity<?>) nearbyEntity;
                implEntity.onCollideWithPlayer(this);
            }
        }

        // Update attributes which are flagged as dirty
        this.updateAttributes();

        // Check for sprint, skip if player is in Creative mode
        if (this.gamemode() != Gamemode.CREATIVE) {
            if (this.hunger() <= 6 && this.sprinting()) {
                this.sprinting(false);
            }
        }
    }

    @Override
    public AxisAlignedBB boundingBox() {
        return this.boundingBox;
    }

    @Override
    public boolean openInventory(io.gomint.inventory.Inventory<?> inventory) {
        if (inventory instanceof ContainerInventory) {
            InventoryOpenEvent event = new InventoryOpenEvent(this, inventory);
            this.world().server().pluginManager().callEvent(event);

            if (event.cancelled()) {
                return false;
            }

            // Check if we have a open container
            if (this.currentOpenContainer != null) {
                this.closeInventory(this.currentOpenContainer);
            }

            // Trigger open
            ContainerInventory<?> containerInventory = (ContainerInventory<?>) inventory;
            containerInventory.addViewer(this, WindowMagicNumbers.OPEN_CONTAINER);

            this.currentOpenContainer = containerInventory;
            return true;
        }

        return false;
    }

    @Override
    public boolean closeInventory(io.gomint.inventory.Inventory<?> inventory) {
        if (inventory instanceof ContainerInventory) {
            if (this.currentOpenContainer == inventory) {
                this.closeInventory(WindowMagicNumbers.OPEN_CONTAINER, true);
                return true;
            }
        }

        return false;
    }

    /**
     * Get the virtual inventory for the cursor item
     *
     * @return the players cursor item
     */
    public Inventory<?> getCursorInventory() {
        return this.cursorInventory;
    }

    /**
     * Get offhand inventory. This inventory only has one slot
     *
     * @return current offhand inventory
     */
    public Inventory<?> getOffhandInventory() {
        return this.offhandInventory;
    }

    /**
     * Check for attribute updates and send them to the player if needed
     */
    private void updateAttributes() {
        PacketUpdateAttributes updateAttributes = null;

        for (AttributeInstance instance : this.attributes.values()) {
            if (instance.isDirty()) {
                if (updateAttributes == null) {
                    updateAttributes = new PacketUpdateAttributes();
                    updateAttributes.setEntityId(this.id());
                }

                updateAttributes.addAttributeInstance(instance);
            }
        }

        if (updateAttributes != null) {
            updateAttributes.setTick(this.world.currentTickTime() / (int) Values.CLIENT_TICK_MS);
            this.connection.addToSendQueue(updateAttributes);
        }
    }

    /**
     * Force send all attributes
     */
    public void resendAttributes() {
        PacketUpdateAttributes updateAttributes = new PacketUpdateAttributes();
        updateAttributes.setEntityId(this.id());
        updateAttributes.setTick(this.world.currentTickTime() / (int) Values.CLIENT_TICK_MS);

        for (AttributeInstance instance : this.attributes.values()) {
            updateAttributes.addAttributeInstance(instance);
        }

        this.connection.addToSendQueue(updateAttributes);
    }

    /**
     * Send all data which the client needs before getting chunks
     */
    public void prepareEntity() {
        // Inventories
        this.inventory = new PlayerInventory(this.world.server().items(), this);
        this.armorInventory = new ArmorInventory(this.world.server().items(), this);

        this.cursorInventory = new CursorInventory(this.world.server().items(), this);
        this.offhandInventory = new OffhandInventory(this.world.server().items(), this);
        this.enderChestInventory = new EnderChestInventory(this.world.server().items(), this);

        this.craftingInventory = new CraftingInputInventory(this.world.server().items(), this);
        this.craftingInputInventory = new CraftingInputInventory(this.world.server().items(), this);
        this.craftingResultInventory = new OneSlotInventory(this.world.server().items(), this);

        // Load from world
        if (!this.world.loadPlayer(this)) {
            // Get default spawn
            this.setAndRecalcPosition(this.spawnLocation() != null ? this.spawnLocation() : this.world.spawnLocation());
        }

        // Send world init data
        this.connection.sendWorldTime(this.world.timeAsTicks());
        this.connection.sendWorldInitialization(this.id());
        this.connection.addToSendQueue(new PacketItemComponent());
        this.connection.sendSpawnPosition();
        // this.connection.sendWorldTime(this.world.getTimeAsTicks());
        this.connection.addToSendQueue(new PacketAvailableEntityIdentifiers());

        this.connection.sendDifficulty();
        this.connection.sendCommandsEnabled();

        // Set spawn
        if (this.connection.entity().spawnLocation() == null) {
            this.connection.entity().spawnLocation(this.connection.entity().world().spawnLocation());
        }

        // Send adventure settings
        this.sendAdventureSettings();

        // Attributes
        this.updateAttributes();

        this.inventory.addViewer(this);
        this.armorInventory.addViewer(this);

        this.cursorInventory.addViewer(this);
        this.offhandInventory.addViewer(this);
        this.enderChestInventory.addViewer(this);

        this.craftingInventory.addViewer(this);
        this.craftingInputInventory.addViewer(this);
        this.craftingResultInventory.addViewer(this);

        // Send entity metadata
        this.sendData(this);

        this.connection.server().creativeInventory().addViewer(this);

        PacketPlayerlist playerlist = new PacketPlayerlist();
        playerlist.setMode((byte) 0);
        playerlist.setEntries(new ArrayList<>() {{
            add(new PacketPlayerlist.Entry(EntityPlayer.this));
        }});
        this.connection().addToSendQueue(playerlist);

        // Send all recipes
        this.connection.addToSendQueue(this.world.server().recipeManager().getCraftingRecipesBatch());

        LOGGER.debug("Did send all prepare entity data");
    }

    @Override
    protected boolean shouldMove() {
        return false;
    }

    /**
     * Check if the player can interact with the given position
     *
     * @param position    to check for
     * @param maxDistance for which we check
     * @return true if the player can interact, false if not
     */
    public boolean canInteract(Vector position, int maxDistance) {
        // Distance
        Vector eyePosition = this.position().add(0, this.eyeHeight(), 0);
        if (eyePosition.distanceSquared(position) > MathUtils.square(maxDistance)) {
            return false;
        }

        // Direction
        Vector playerPosition = this.position();
        Vector2 directionPlane = this.directionPlane();
        float dot = directionPlane.dot(new Vector2(eyePosition.x(), eyePosition.z()));
        float dot1 = directionPlane.dot(new Vector2(playerPosition.x(), playerPosition.z()));
        return (dot1 - dot) >= -(MathUtils.SQRT_3 / 2);
    }

    /**
     * Remove player from PlayerList and remove from global inventories etc.
     */
    public void cleanup() {
        this.connection.server().creativeInventory().removeViewer(this);
        this.connection.server().uuidMappedPlayers().remove(this.uuid());

        Block block = this.world.blockAt(this.position().toBlockPosition());
        block.gotOff(this);

        // Remove from player list
        PacketPlayerlist packetPlayerlist = new PacketPlayerlist();
        packetPlayerlist.setMode((byte) 1);
        packetPlayerlist.setEntries(new ArrayList<>() {{
            add(new PacketPlayerlist.Entry(EntityPlayer.this));
        }});

        // Cleanup the visibility manager
        this.entityVisibilityManager.clear();

        // Check all entities
        for (WorldAdapter worldAdapter : this.connection.server().worldManager().worlds()) {
            worldAdapter.syncScheduler().execute(() -> worldAdapter.iterateAllEntities(entity -> {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer entityPlayer = (EntityPlayer) entity;
                    if (!entityPlayer.equals(EntityPlayer.this)) {
                        entityPlayer.connection().addToSendQueue(packetPlayerlist);
                    }

                    // Check if player did hide this one
                    if (entityPlayer.hiddenPlayers != null) {
                        entityPlayer.hiddenPlayers.remove(id());
                    }

                    // Check if mouseover is the entity
                    if (entityPlayer.hoverEntity != null && entityPlayer.hoverEntity.equals(EntityPlayer.this)) {
                        entityPlayer.hoverEntity = null;
                    }

                    entityPlayer.entityVisibilityManager.removeEntity(EntityPlayer.this);
                } else {
                    entity.hideFor(EntityPlayer.this);
                }
            }));
        }
    }

    /**
     * Close a container inventory
     *
     * @param windowId which should be closed
     */
    public void closeInventory(byte windowId, boolean isServerSided) {
        if (this.currentOpenContainer != null) {
            this.currentOpenContainer.removeViewer(this);

            InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(this, this.currentOpenContainer);
            this.world().server().pluginManager().callEvent(inventoryCloseEvent);

            PacketContainerClose packetContainerClose = new PacketContainerClose();
            packetContainerClose.setWindowId(windowId);
            packetContainerClose.setServerSided(isServerSided);
            this.connection.addToSendQueue(packetContainerClose);

            this.currentOpenContainer = null;
        }
    }

    /**
     * Get a container by its id
     *
     * @param windowId which should be looked up
     * @return container inventory or null when not found
     */
    public ContainerInventory<?> getContainerId(byte windowId) {
        if (windowId == WindowMagicNumbers.OPEN_CONTAINER) {
            return this.currentOpenContainer;
        }

        return null;
    }

    @Override
    public EntityPlayer sendMessage(String message) {
        PacketText packetText = new PacketText();
        packetText.setMessage(message);
        packetText.setDeviceId(this.deviceInfo().deviceId());
        packetText.setType(PacketText.Type.CLIENT_MESSAGE);
        this.connection.addToSendQueue(packetText);
        return this;
    }

    @Override
    public EntityPlayer sendMessage(ChatType type, String... message) {
        PacketText packetText = new PacketText();
        packetText.setMessage(message[0]);
        packetText.setDeviceId(this.deviceInfo().deviceId());
        switch (type) {
            case TIP:
                packetText.setType(PacketText.Type.TIP_MESSAGE);
                break;
            case NORMAL:
                packetText.setType(PacketText.Type.CLIENT_MESSAGE);
                break;
            case SYSTEM:
                packetText.setType(PacketText.Type.SYSTEM_MESSAGE);
                break;
            case POPUP:
                packetText.setType(PacketText.Type.POPUP_NOTICE);

                if (message.length > 1) {
                    packetText.setSubtitle(message[1]);
                } else {
                    packetText.setSubtitle("");
                }

                break;
            default:
                break;
        }

        this.connection.addToSendQueue(packetText);
        return this;
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.permissionManager.has(permission);
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultValue) {
        return this.permissionManager.has(permission, defaultValue);
    }

    @Override
    public EntityPlayer sendCommands() {
        // Send commands
        PacketAvailableCommands packetAvailableCommands = this.connection.server().
            pluginManager().commandManager().createPacket(this);
        this.connection.addToSendQueue(packetAvailableCommands);
        return this;
    }

    /**
     * Basicly a override of the {@link EntityHuman#exhaust(float)} method with a event call in it.
     *
     * @param amount of exhaustion
     * @param cause  of exhaustion
     */
    @Override
    public EntityPlayer exhaust(float amount, PlayerExhaustEvent.Cause cause) {
        if (this.gamemode == Gamemode.SURVIVAL) {
            PlayerExhaustEvent exhaustEvent = new PlayerExhaustEvent(this, amount, cause);
            this.world.server().pluginManager().callEvent(exhaustEvent);

            if (exhaustEvent.cancelled()) {
                this.resendAttributes();
                return this;
            }

            super.exhaust(exhaustEvent.additionalAmount());
        } else {
            if (this.exhaustion() != 0) {
                this.exhaustion(0);
            }
        }

        return this;
    }

    /**
     * Handle a jump
     */
    public void jump() {
        // Jumping is only handled for exhaustion it seems
        if (this.sprinting()) {
            this.exhaust(0.8f, PlayerExhaustEvent.Cause.SPRINT_JUMP);
        } else {
            this.exhaust(0.2f, PlayerExhaustEvent.Cause.JUMP);
        }
    }

    /**
     * Attack another entity with the item currently in hand
     *
     * @param target which should be attacked
     * @return true when damage has been dealt, false when not
     */
    public boolean attackWithItemInHand(Entity<?> target) {
        if (target instanceof io.gomint.server.entity.Entity) {
            io.gomint.server.entity.Entity<?> targetEntity = (io.gomint.server.entity.Entity<?>) target;

            // Check if the target can be attacked
            if (targetEntity.canBeAttackedWithAnItem() && !targetEntity.isInvulnerableFrom(this)) {
                boolean success = false;

                // Get this entity attack damage
                EntityDamageEvent.DamageSource damageSource = EntityDamageEvent.DamageSource.ENTITY_ATTACK;
                float damage = this.attribute(Attribute.ATTACK_DAMAGE);

                EnchantmentSharpness sharpness = this.inventory().itemInHand().enchantment(EnchantmentSharpness.class);
                if (sharpness != null) {
                    damage += sharpness.level() * 1.25f;
                }

                // Check for knockback stuff
                int knockbackLevel = 0;

                if (this.sprinting()) {
                    knockbackLevel++;
                }

                EnchantmentKnockback knockback = this.inventory().itemInHand().enchantment(EnchantmentKnockback.class);
                if (knockback != null) {
                    knockbackLevel += knockback.level();
                }

                if (damage > 0) {
                    boolean crit = this.fallDistance > 0 && !this.onGround && !this.isOnLadder() && !this.isInsideLiquid();
                    if (crit && damage > 0.0f) {
                        damage *= 1.5;
                    }

                    // Check if target can absorb this damage
                    if ((success = targetEntity.damage(new EntityDamageByEntityEvent(targetEntity, this, damageSource, damage)))) {
                        // Apply knockback
                        if (knockbackLevel > 0) {
                            // Modify target velocity
                            Vector targetVelo = targetEntity.velocity();
                            targetEntity.velocity(targetVelo.add(
                                (float) (-Math.sin(this.yaw() * (float) Math.PI / 180.0F) * (float) knockbackLevel * 0.5F),
                                0.1f,
                                (float) (Math.cos(this.yaw() * (float) Math.PI / 180.0F) * (float) knockbackLevel * 0.5F)));

                            // Modify our velocity / movement
                            Vector ownVelo = this.velocity();
                            ownVelo.x(ownVelo.x() * 0.6F);
                            ownVelo.z(ownVelo.z() * 0.6F);
                            this.velocity(ownVelo);

                            if (!this.world.server().serverConfig().vanilla().disableSprintReset()) {
                                this.sprinting(false);
                            }
                        }

                        targetEntity.broadCastMotion();
                    }
                }

                this.exhaust(0.3f, PlayerExhaustEvent.Cause.ATTACK);
                return success;
            }
        }

        return false;
    }

    @Override
    public boolean damage(EntityDamageEvent damageEvent) {
        // When allowFlight is on we don't need falling damage
        if (this.adventureSettings.isCanFly() && damageEvent.damageSource() == EntityDamageEvent.DamageSource.FALL) {
            return false;
        }

        // Can't touch this!
        return this.gamemode != Gamemode.CREATIVE && this.gamemode != Gamemode.SPECTATOR && super.damage(damageEvent);
    }

    @Override
    protected float applyArmorReduction(EntityDamageEvent damageEvent, boolean damageArmor) {
        if (damageEvent.damageSource() == EntityDamageEvent.DamageSource.FALL ||
            damageEvent.damageSource() == EntityDamageEvent.DamageSource.VOID ||
            damageEvent.damageSource() == EntityDamageEvent.DamageSource.DROWNING) {
            return damageEvent.damage();
        }

        float damage = damageEvent.damage();
        float maxReductionDiff = 25 - this.armorInventory.getTotalArmorValue();
        float amplifiedDamage = damage * maxReductionDiff;
        if (damageArmor) {
            this.armorInventory.damageEvenly(damage);
        }

        return amplifiedDamage / 25.0F;
    }

    @Override
    public void attach(EntityPlayer player) {
        super.attach(player);
        this.armorInventory.addViewer(player);

        // Send death animation if needed
        if (this.health() <= 0) {
            PacketEntityEvent entityEvent = new PacketEntityEvent();
            entityEvent.setEntityId(this.id());
            entityEvent.setEventId(EntityEvent.DEATH.getId());
            player.connection().addToSendQueue(entityEvent);
        }
    }

    /**
     * Respawn this player
     */
    public void respawn() {
        // Event first
        PlayerRespawnEvent event = new PlayerRespawnEvent(this, this.respawnPosition);
        this.connection.server().pluginManager().callEvent(event);

        if (event.cancelled()) {
            PacketEntityEvent entityEvent = new PacketEntityEvent();
            entityEvent.setEntityId(this.id());
            entityEvent.setEventId(EntityEvent.DEATH.getId());
            this.connection.addToSendQueue(entityEvent);

            return;
        }

        // Check if we need to despawn first
        if (this.deadTimer != -1) {
            this.despawn();
            this.deadTimer = -1;
        }

        // Reset last damage stuff
        this.lastDamageEntity = null;
        this.lastDamageSource = null;
        this.lastDamage = 0;

        // Send metadata
        this.sendData(this);

        // Resend adventure settings
        this.adventureSettings.update();

        // Reset attributes
        this.resetAttributes();
        this.resendAttributes();

        // Remove all effects
        this.removeAllEffects();

        // Check for new chunks
        this.teleport(event.respawnLocation());
        this.respawnPosition = null;

        // Reset motion
        this.velocity(new Vector(0, 0, 0));

        // Send all inventories
        this.inventory.sendContents(this.connection);
        this.offhandInventory.sendContents(this.connection);
        this.armorInventory.sendContents(this.connection);

        PacketEntityEvent entityEvent = new PacketEntityEvent();
        entityEvent.setEntityId(this.id());
        entityEvent.setEventId(EntityEvent.RESPAWN.getId());

        // Update all other players
        for (io.gomint.entity.EntityPlayer player : this.world.onlinePlayers()) {
            EntityPlayer implPlayer = (EntityPlayer) player;
            implPlayer.entityVisibilityManager().updateEntity(this, this.chunk());
        }

        // Apply item in hand stuff
        ItemStack<?> itemInHand = (ItemStack<?>) this.inventory.itemInHand();
        itemInHand.gotInHand(this);
    }

    @Override
    protected void kill() {
        super.kill();

        // Send the death screen to us
        PacketEntityEvent entityEvent = new PacketEntityEvent();
        entityEvent.setEntityId(this.id);
        entityEvent.setEventId(EntityEvent.DEATH.getId());
        this.connection.addToSendQueue(entityEvent);

        // Prepare a death message
        String deathMessage = "";
        EntityDamageEvent.DamageSource cause = this.lastDamageSource();
        switch (cause) {
            case ENTITY_ATTACK:
                deathMessage = this.displayName() + " was slain by " + this.lastDamageEntity().nameTag();
                break;
            case FALL:
                deathMessage = this.displayName() + " fell from a high place";
                break;
            case LAVA:
                deathMessage = this.displayName() + " tried to swim in lava";
                break;
            case FIRE:
                deathMessage = this.displayName() + " went up in flames";
                break;
            case VOID:
                deathMessage = this.displayName() + " fell out of the world";
                break;
            case CACTUS:
                deathMessage = this.displayName() + " was pricked to death";
                break;
            case STARVE:
                deathMessage = this.displayName() + " starved to death";
                break;
            case ON_FIRE:
                deathMessage = this.displayName() + " burned to death";
                break;
            case DROWNING:
                deathMessage = this.displayName() + " drowned";
                break;
            case HARM_EFFECT:
                deathMessage = this.displayName() + " was killed by magic";
                break;
            case ENTITY_EXPLODE:
                deathMessage = this.displayName() + " blew up";
                break;
            case PROJECTILE:
                deathMessage = this.displayName() + " has been shot";
                break;
            case API:
                deathMessage = this.displayName() + " was killed by setting health to 0";
                break;
            case COMMAND:
                deathMessage = this.displayName() + " died";
                break;
            default:
                deathMessage = this.displayName() + " died for unknown reasons";
                break;
        }

        List<io.gomint.inventory.item.ItemStack<?>> drops = this.getDrops();

        PlayerDeathEvent event = new PlayerDeathEvent(this, deathMessage, true, drops);
        this.connection.server().pluginManager().callEvent(event);

        if (event.dropInventory()) {
            for (io.gomint.inventory.item.ItemStack<?> drop : event.drops()) {
                this.world.dropItem(this.location(), drop);
            }

            this.inventory.clear();
            this.offhandInventory.clear();
            this.armorInventory.clear();
        }

        this.craftingInventory.clear();
        this.craftingInputInventory.clear();
        this.craftingResultInventory.clear();

        if (event.deathMessage() != null && !event.deathMessage().isEmpty()) {
            for (io.gomint.entity.EntityPlayer player : this.world.onlinePlayers()) {
                player.sendMessage(event.deathMessage());
            }
        }

        this.respawnPosition = this.world.spawnLocation().add(0, this.eyeHeight, 0);

        PacketRespawnPosition packetRespawnPosition = new PacketRespawnPosition();
        packetRespawnPosition.setPosition(this.respawnPosition);
        packetRespawnPosition.setEntityId(this.id());
        packetRespawnPosition.setState(RespawnState.SEARCHING_FOR_SPAWN);
        this.connection().addToSendQueue(packetRespawnPosition);
    }

    private List<io.gomint.inventory.item.ItemStack<?>> getDrops() {
        List<io.gomint.inventory.item.ItemStack<?>> drops = new ArrayList<>();

        for (io.gomint.inventory.item.ItemStack<?> itemStack : this.inventory.contents()) {
            if (!(itemStack instanceof ItemAir)) {
                drops.add(itemStack);
            }
        }

        for (io.gomint.inventory.item.ItemStack<?> itemStack : this.offhandInventory.contents()) {
            if (!(itemStack instanceof ItemAir)) {
                drops.add(itemStack);
            }
        }

        for (io.gomint.inventory.item.ItemStack<?> itemStack : this.armorInventory.contents()) {
            if (!(itemStack instanceof ItemAir)) {
                drops.add(itemStack);
            }
        }

        return drops;
    }

    @Override
    protected void checkIfCollided(float movX, float movY, float movZ, float dX, float dY, float dZ) {
        // Check if we are not on ground or we moved on y axis
        if (!this.onGround || movY != 0) {
            AxisAlignedBB bb = new AxisAlignedBB(this.boundingBox.minX(), this.boundingBox.minY() - 0.2f, this.boundingBox.minZ(),
                this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ());

            // Check if we collided with a block
            this.onGround = this.world.collisionCubes(this, bb, false) != null;
        }

        this.isCollided = this.onGround;
    }

    @Override
    public boolean online() {
        return this.connection.entity() != null;
    }

    @Override
    public Locale locale() {
        return this.locale;
    }

    @Override
    public EntityPlayer disconnect(String reason) {
        this.connection.disconnect(reason);
        return this;
    }

    // ------- GUI stuff
    public void sendServerSettings() {
        if (this.serverSettingsForm != -1) {
            io.gomint.server.gui.Form<?> form = this.forms.get(this.serverSettingsForm);

            PacketServerSettingsResponse response = new PacketServerSettingsResponse();
            response.setFormId(this.serverSettingsForm);
            response.setJson(form.toJSON().toJSONString());
            LOGGER.debug("Sending settings form: {}", response);
            this.connection.addToSendQueue(response);
        }
    }

    @Override
    public <R> FormListener<R> showForm(Form<R> form) {
        int formId = this.formId++;
        io.gomint.server.gui.Form<R> implForm = (io.gomint.server.gui.Form<R>) form;

        this.forms.put(formId, implForm);

        io.gomint.server.gui.FormListener<R> formListener = new io.gomint.server.gui.FormListener<R>();

        this.formListeners.put(formId, formListener);

        // Send packet for client
        String json = implForm.toJSON().toJSONString();
        PacketModalRequest packetModalRequest = new PacketModalRequest();
        packetModalRequest.setFormId(formId);
        packetModalRequest.setJson(json);
        this.connection.addToSendQueue(packetModalRequest);

        return formListener;
    }

    @Override
    public <R> FormListener<R> settingsForm(Form<R> form) {
        if (this.serverSettingsForm != -1) {
            this.removeSettingsForm();
        }

        int formId = this.formId++;
        io.gomint.server.gui.Form<R> implForm = (io.gomint.server.gui.Form<R>) form;

        this.forms.put(formId, implForm);

        io.gomint.server.gui.FormListener<R> formListener = new io.gomint.server.gui.FormListener<R>();

        this.formListeners.put(formId, formListener);
        this.serverSettingsForm = formId;
        return formListener;
    }

    @Override
    public EntityPlayer removeSettingsForm() {
        if (this.serverSettingsForm != -1) {
            this.forms.remove(this.serverSettingsForm);
            this.formListeners.remove(this.serverSettingsForm);
            this.serverSettingsForm = -1;
        }

        return this;
    }

    public <R> void parseGUIResponse(int formId, String json) {
        // Get the listener and the form
        Form<?> form = this.forms.get(formId);
        if (form != null) {
            // Get listener
            io.gomint.server.gui.FormListener<R> formListener = (io.gomint.server.gui.FormListener<R>) this.formListeners.get(formId);

            if (this.serverSettingsForm != formId) {
                this.forms.remove(formId);
                this.formListeners.remove(formId);
            }

            if (json.equals("null")) {
                formListener.getCloseConsumer().accept(null);
            } else {
                io.gomint.server.gui.Form<R> implForm = (io.gomint.server.gui.Form<R>) form;
                R resp = implForm.parseResponse(json);
                if (resp == null) {
                    formListener.getCloseConsumer().accept(null);
                } else {
                    formListener.getResponseConsumer().accept(resp);
                }
            }
        }
    }

    @Override
    public void despawn() {
        for (Entity<?> entity : new HashSet<>(this.getAttachedEntities())) {
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).entityVisibilityManager().removeEntity(this);
            }
        }
    }

    /**
     * Add xp from a orb
     *
     * @param xpAmount which should be added
     */
    public void addXP(int xpAmount) {
        this.lastPickupXP = this.world.currentTickTime();
        this.xp(this.xp + xpAmount);
    }

    /**
     * A player can only pickup xp orbs at a rate of 1 per tick
     *
     * @return
     */
    public boolean canPickupXP() {
        return this.world.currentTickTime() - this.lastPickupXP >= Values.CLIENT_TICK_MS;
    }

    private int calculateRequiredExperienceForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + level * 2;
        }
    }

    @Override
    public float xpPercentage() {
        return this.attribute(Attribute.EXPERIENCE);
    }

    @Override
    public int xp() {
        return this.xp;
    }

    @Override
    public EntityPlayer xp(int xp) {
        // Iterate levels until we have a new xp percentage value to set
        int neededXP, tempXP = xp, level = 0;
        while (tempXP > (neededXP = calculateRequiredExperienceForLevel(level()))) {
            tempXP -= neededXP;
            level++;
        }

        this.xp = xp;
        this.attribute(Attribute.EXPERIENCE, tempXP / (float) neededXP);
        this.level(level);
        return this;
    }

    @Override
    protected boolean shouldTickHunger() {
        return this.gamemode == Gamemode.SURVIVAL;
    }

    @Override
    public int level() {
        return (int) this.attribute(Attribute.EXPERIENCE_LEVEL);
    }

    @Override
    public EntityPlayer level(int level) {
        this.attribute(Attribute.EXPERIENCE_LEVEL, level);
        return this;
    }

    @Override
    public EntityPlayer playSound(Vector location, Sound sound, byte pitch, SoundData data) {
        this.world.playSound(this, location, sound, pitch, data);
        return this;
    }

    @Override
    public EntityPlayer playSound(Vector location, Sound sound, byte pitch) {
        this.world.playSound(this, location, sound, pitch, -1);
        return this;
    }

    @Override
    public EntityPlayer sendParticle(Vector location, Particle particle) {
        this.world.sendParticle(this, location, particle, 0);
        return this;
    }

    @Override
    public EntityPlayer sendParticle(Vector location, Particle particle, ParticleData data) {
        this.world.sendParticle(this, location, particle, data);
        return this;
    }

    @Override
    public boolean allowFlight() {
        return this.adventureSettings.isCanFly();
    }

    @Override
    public EntityPlayer allowFlight(boolean value) {
        this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.CAN_FLY, value);
        this.adventureSettings.setCanFly(value);
        this.adventureSettings.update();
        return this;
    }

    @Override
    public boolean flying() {
        return this.adventureSettings.isFlying();
    }

    @Override
    public EntityPlayer flying(boolean value) {
        this.adventureSettings.setFlying(value);
        this.adventureSettings.update();
        return this;
    }

    @Override
    public EntityPlayer sendTitle(String title, String subtitle, long fadein, long duration, long fadeout, TimeUnit unit) {
        // NPE check
        if (title == null) {
            title = "";
        }

        if (subtitle != null && !Objects.equals(subtitle, "")) {
            PacketSetTitle subtitlePacket = new PacketSetTitle();
            subtitlePacket.setType(PacketSetTitle.TitleType.TYPE_SUBTITLE.getId());
            subtitlePacket.setText(subtitle);
            subtitlePacket.setFadeInTime((int) unit.toMillis(fadein) / (int) Values.CLIENT_TICK_MS);
            subtitlePacket.setStayTime((int) unit.toMillis(duration) / (int) Values.CLIENT_TICK_MS);
            subtitlePacket.setFadeOutTime((int) unit.toMillis(fadeout) / (int) Values.CLIENT_TICK_MS);
            this.connection().addToSendQueue(subtitlePacket);
        }

        PacketSetTitle titlePacket = new PacketSetTitle();
        titlePacket.setType(PacketSetTitle.TitleType.TYPE_TITLE.getId());
        titlePacket.setText(title);
        titlePacket.setFadeInTime((int) unit.toMillis(fadein) / (int) Values.CLIENT_TICK_MS);
        titlePacket.setStayTime((int) unit.toMillis(duration) / (int) Values.CLIENT_TICK_MS);
        titlePacket.setFadeOutTime((int) unit.toMillis(fadeout) / (int) Values.CLIENT_TICK_MS);
        this.connection().addToSendQueue(titlePacket);
        return this;
    }

    @Override
    public EntityPlayer sendTitle(String title) {
        return this.sendTitle(title, "", 1, 1, (long) 0.5, TimeUnit.SECONDS);
    }

    @Override
    public EntityPlayer sendTitle(String title, String subtitle) {
        return this.sendTitle(title, subtitle, 1, 1, (long) 0.5, TimeUnit.SECONDS);
    }

    public void firstSpawn() {
        // Set location
        this.connection.sendMovePlayer(this.location());

        // Set simulation speed
        PacketWorldEvent worldEvent = new PacketWorldEvent();
        worldEvent.setData(0);
        worldEvent.setEventId(LevelEvent.SIM_SPEED);
        worldEvent.setPosition(new Vector(1f, 1f, 1f));
        this.connection.addToSendQueue(worldEvent);

        // Spawn for others
        this.world().spawnEntityAt(this, this.positionX(), this.positionY(), this.positionZ(), this.yaw(), this.pitch());

        // Now its time for the join event since the player is fully loaded
        PlayerJoinEvent event = this.eventCaller.callEvent(new PlayerJoinEvent(this, ChatColor.YELLOW + this.displayName() + " joined the game."));
        if (event.cancelled()) {
            this.connection.disconnect(event.kickReason());
        } else {
            if (event.joinMessage() != null && !event.joinMessage().isEmpty()) {
                GoMint.instance().onlinePlayers().forEach((player) -> {
                    player.sendMessage(event.joinMessage());
                });
            }
        }

        this.hasCompletedLogin = true;

        // Send network chunk publisher packet after join
        this.world.syncScheduler().schedule(() -> {
            if (online()) {
                this.world.server().pluginManager().callEvent(new PlayerWorldJoinEvent(this, null));
                connection().sendNetworkChunkPublisher();
            }
        }, 250, TimeUnit.MILLISECONDS);
    }

    @Override
    public Packet createSpawnPacket(EntityPlayer receiver) {
        PacketSpawnPlayer packetSpawnPlayer = new PacketSpawnPlayer();
        packetSpawnPlayer.setUuid(this.uuid());
        packetSpawnPlayer.setName(this.name());
        packetSpawnPlayer.setEntityId(this.id());
        packetSpawnPlayer.setRuntimeEntityId(this.id());
        packetSpawnPlayer.setPlatformChatId(this.deviceInfo().deviceId());

        packetSpawnPlayer.setX(this.positionX());
        packetSpawnPlayer.setY(this.positionY());
        packetSpawnPlayer.setZ(this.positionZ());

        packetSpawnPlayer.setVelocityX(this.getMotionX());
        packetSpawnPlayer.setVelocityY(this.getMotionY());
        packetSpawnPlayer.setVelocityZ(this.getMotionZ());

        packetSpawnPlayer.setPitch(this.pitch());
        packetSpawnPlayer.setYaw(this.yaw());
        packetSpawnPlayer.setHeadYaw(this.headYaw());

        packetSpawnPlayer.setItemInHand(this.inventory().itemInHand());
        packetSpawnPlayer.setMetadataContainer(this.metadata());
        packetSpawnPlayer.setDeviceId(this.deviceInfo().deviceId());
        packetSpawnPlayer.setBuildPlatform(this.deviceInfo().OS().id());

        return packetSpawnPlayer;
    }

    @Override
    public void preSpawn(PlayerConnection connection) {

    }

    @Override
    public void postSpawn(PlayerConnection connection) {

    }

    @Override
    public boolean gliding() {
        return this.metadataContainer.getDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.GLIDING);
    }

    @Override
    public EntityPlayer gliding(boolean value) {
        this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.GLIDING, value);
        return this;
    }

    @Override
    public DeviceInfo deviceInfo() {
        return this.connection.deviceInfo();
    }

    @Override
    public InetSocketAddress address() {
        return this.connection.connection().getAddress();
    }

    @Override
    protected void checkAfterGravity() {
        float dX = this.getMotionX();
        float dY = this.getMotionY();
        float dZ = this.getMotionZ();

        // Check if we collide with some blocks when we would move that fast
        List<AxisAlignedBB> collisionList = this.world.collisionCubes(this, this.boundingBox.offsetBoundingBox(dX, dY, dZ), false);
        if (collisionList != null) {
            // Check if we would hit a y border block
            for (AxisAlignedBB axisAlignedBB : collisionList) {
                dY = axisAlignedBB.calculateYOffset(this.boundingBox, dY);
            }

            if (Math.abs(dY) <= 0.001) {
                dY = 0;
            }

            this.getTransform().motion(dX, dY, dZ);
        }
    }

    @Override
    public EntityPlayer dispatchCommand(String command, Consumer<CommandOutput> outputConsumer) {
        this.connection().server().pluginManager().commandManager().dispatchCommand(this, command, outputConsumer);
        return this;
    }

    @Override
    public EntityPlayer spawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
        this.connection.sendPlayerSpawnPosition();
        return this;
    }

    @Override
    public EntityPlayer scoreboard(io.gomint.scoreboard.Scoreboard scoreboard) {
        Precondition.safeWorldAccess(this.world, false);
        this.removeScoreboard();

        this.scoreboard = (Scoreboard) scoreboard;
        this.scoreboard.showFor(this);
        return this;
    }

    @Override
    public EntityPlayer removeScoreboard() {
        Precondition.safeWorldAccess(this.world, false);
        if (this.scoreboard != null) {
            this.scoreboard.hideFor(this);
            this.scoreboard = null;
        }

        return this;
    }

    public void setUsingItem(boolean value) {
        if (value) {
            this.actionStart = this.world.currentTickTime();
            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.ACTION, true);
        } else {
            this.actionStart = -1;
            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.ACTION, false);
        }
    }

    @Override
    protected void checkBlockCollisions() {
        List<io.gomint.world.block.Block> blockList = this.world.getCollisionBlocks(this, true);
        if (blockList != null) {
            for (io.gomint.world.block.Block block : blockList) {
                Block implBlock = (Block) block;
                implBlock.onEntityCollision(this);
            }
        }
    }

    @Override
    public void initFromNBT(NBTTagCompound compound) {
        super.initFromNBT(compound);

        this.enderChestInventory.initFromNBT(compound.getList("EnderChestInventory", false));
    }

    @Override
    public NBTTagCompound persistToNBT() {
        NBTTagCompound compound = super.persistToNBT();

        compound.addValue("EnderChestInventory", this.enderChestInventory.persistToNBT());

        return compound;
    }

    @Override
    public boolean shouldBeSeen(EntityPlayer player) {
        return player.isSpawnPlayers() && super.shouldBeSeen(player);
    }

    public boolean knowsChunk(@Nonnull ChunkAdapter adapter) {
        return this.connection.knowsChunk(adapter.longHashCode());
    }

    public boolean knowsChunk(int posX, int posZ) {
        return this.connection.knowsChunk(CoordinateUtils.toLong(posX, posZ));
    }

    @Override
    public io.gomint.permission.PermissionManager permissionManager() {
        return this.permissionManager;
    }

    @Override
    public EntityPlayer permissionManager(io.gomint.permission.PermissionManager permissionManager) {
        Class<? extends Plugin> plugin = CallerDetectorUtil.getCallerPlugin();
        LOGGER.warn("Plugin {} swapped out permission manager with {}", plugin.getName(), permissionManager.getClass().getName());

        this.permissionManager = permissionManager;
        this.isUsingDefaultPermissionManager = this.permissionManager instanceof PermissionManager;
        return this;
    }

    public EntityVisibilityManager entityVisibilityManager() {
        return this.entityVisibilityManager;
    }

    public AdventureSettings adventureSettings() {
        return this.adventureSettings;
    }

    public Entity<?> hoverEntity() {
        return this.hoverEntity;
    }

    public void hoverEntity(Entity <?>hoverEntity) {
        this.hoverEntity = hoverEntity;
    }

    public Inventory<?> craftingInventory() {
        return this.craftingInventory;
    }

    public CraftingInputInventory craftingInputInventory() {
        return this.craftingInputInventory;
    }

    public BlockPosition breakVector() {
        return this.breakVector;
    }

    public long startBreak() {
        return this.startBreak;
    }

    public long breakTime() {
        return this.breakTime;
    }

    public Set<BlockPosition> blockUpdates() {
        return this.blockUpdates;
    }

    public Location teleportPosition() {
        return this.teleportPosition;
    }

    public void breakVector(BlockPosition breakVector) {
        this.breakVector = breakVector;
    }

    public void setStartBreak(long startBreak) {
        this.startBreak = startBreak;
    }

    public void setBreakTime(long breakTime) {
        this.breakTime = breakTime;
    }

    public void setTeleportPosition(Location teleportPosition) {
        this.teleportPosition = teleportPosition;
    }

    public void setFishingHook(EntityFishingHook fishingHook) {
        this.fishingHook = fishingHook;
    }

    public void setNextMovement(Location nextMovement) {
        this.nextMovement = nextMovement;
    }

    public void setSpawnPlayers(boolean spawnPlayers) {
        this.spawnPlayers = spawnPlayers;
    }

    public EntityFishingHook getFishingHook() {
        return this.fishingHook;
    }

    @Override
    public Location spawnLocation() {
        return this.spawnLocation;
    }

    public long actionStart() {
        return this.actionStart;
    }

    public LoginPerformance loginPerformance() {
        return this.loginPerformance;
    }

    public Location getNextMovement() {
        return this.nextMovement;
    }

    public boolean isSpawnPlayers() {
        return this.spawnPlayers;
    }

    @Override
    public Scoreboard scoreboard() {
        return this.scoreboard;
    }

    @Override
    public EnderChestInventory enderChestInventory() {
        return this.enderChestInventory;
    }

    public boolean hasCompletedLogin() {
        return this.hasCompletedLogin;
    }

    public void resetActionStart() {
        this.actionStart = this.world.currentTickTime();
    }

    @Override
    public Set<String> tags() {
        return EntityTags.PLAYER;
    }

    public Inventory<?> currentOpenContainer() {
        return this.currentOpenContainer;
    }

    public long enchantmentSeed() {
        return this.enchantmentSeed;
    }

    public void generateNewEnchantmentSeed() {
        this.enchantmentSeed = ThreadLocalRandom.current().nextLong();
    }

}
