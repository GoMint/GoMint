/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.entity.passive;

import io.gomint.entity.Entity;
import io.gomint.entity.potion.PotionEffect;
import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.event.entity.EntityHealEvent;
import io.gomint.event.player.PlayerExhaustEvent;
import io.gomint.event.player.PlayerFoodLevelChangeEvent;
import io.gomint.math.MathUtils;
import io.gomint.player.DeviceInfo;
import io.gomint.player.DeviceInfo.DeviceOS;
import io.gomint.player.DeviceInfo.UI;
import io.gomint.player.PlayerSkin;
import io.gomint.server.entity.Attribute;
import io.gomint.server.entity.AttributeInstance;
import io.gomint.server.entity.AttributeModifier;
import io.gomint.server.entity.AttributeModifierType;
import io.gomint.server.entity.EntityCreature;
import io.gomint.server.entity.EntityFlag;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.entity.EntityTags;
import io.gomint.server.entity.EntityType;
import io.gomint.server.entity.metadata.MetadataContainer;
import io.gomint.server.inventory.ArmorInventory;
import io.gomint.server.inventory.PlayerInventory;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketEntityMetadata;
import io.gomint.server.network.packet.PacketMovePlayer;
import io.gomint.server.network.packet.PacketPlayerlist;
import io.gomint.server.network.packet.PacketSpawnPlayer;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.util.Values;
import io.gomint.server.world.WorldAdapter;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Difficulty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:player")
public class EntityHuman<E extends Entity<E>> extends EntityCreature<E> implements io.gomint.entity.passive.EntityHuman<E> {

    private static final int DATA_PLAYER_BED_POSITION = 29;

    private int foodTicks;
    private float lastUpdateDT;

    // Basic information
    private String username;
    private String displayName;
    private UUID uuid;
    private String xboxId = "";

    private PlayerSkin skin;
    private String playerListName;
    private DeviceInfo deviceInfo;

    /**
     * Player inventory which needs to be inited
     */
    protected PlayerInventory inventory;

    /**
     * Constructs a new EntityLiving
     *
     * @param type  The type of the Entity
     * @param world The world in which this entity is in
     */
    protected EntityHuman(EntityType type, WorldAdapter world) {
        super(type, world);
        this.initEntity();
    }

    /**
     * Create new entity human for API
     */
    public EntityHuman() {
        super(EntityType.PLAYER, null);

        // Init inventories
        this.inventory = new PlayerInventory(this.world.server().items(), this);
        this.armorInventory = new ArmorInventory(this.world.server().items(), this);

        // Some default values
        this.uuid = UUID.randomUUID();
        this.username = "NPC: " + this.uuid.toString();
        this.displayName = this.username;
        this.metadataContainer.putString(MetadataContainer.DATA_NAMETAG, this.username);

        // Emulate a device for player list stuff
        this.deviceInfo = new DeviceInfo(DeviceOS.DEDICATED,
            "Unknown",
            UUID.randomUUID().toString(),
            UI.CLASSIC);

        this.initEntity();
    }

    @Override
    public String toString() {
        return "{\"_class\":\"EntityHuman\", " +
            "\"username\":" + (this.username == null ? "null" : "\"" + this.username + "\"") + ", " +
            "\"uuid\":" + (this.uuid == null ? "null" : this.uuid) +
            "}";
    }

    @Override
    protected E size(float width, float height) {
        super.size(width, height);

        if (height > 1.61f) {
            this.eyeHeight = 1.62f;
        }

        return (E) this;
    }

    private void initEntity() {
        this.size(0.6f, 1.8f);
        this.offsetY = this.eyeHeight + 0.0001f;
        this.stepHeight = 0.6f;

        this.metadataContainer.putByte(MetadataContainer.DATA_PLAYER_FLAGS, (byte) 0);

        this.metadataContainer.putShort(MetadataContainer.DATA_AIR, (short) 400);
        this.metadataContainer.putShort(MetadataContainer.DATA_MAX_AIRDATA_MAX_AIR, (short) 400);
        this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.BREATHING, true);

        // Sleeping stuff
        this.playerFlag(EntityFlag.SLEEPING, false);
        this.metadataContainer.putPosition(DATA_PLAYER_BED_POSITION, 0, 0, 0);

        // Exhaustion, saturation and food
        attribute(Attribute.HUNGER);
        attribute(Attribute.SATURATION);
        attribute(Attribute.EXHAUSTION);
        attribute(Attribute.EXPERIENCE_LEVEL);
        attribute(Attribute.EXPERIENCE);

        this.nameTagAlwaysVisible(true);
        this.canClimb(true);

        this.playerListName = this.name();
    }

    @Override
    public E playerListName(String newPlayerListName) {
        if (newPlayerListName == null) {
            return (E) this;
        }

        this.playerListName = newPlayerListName;
        this.updatePlayerList();
        return (E) this;
    }

    @Override
    public String name() {
        return this.username;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public void update(long currentTimeMS, float dT) {
        super.update(currentTimeMS, dT);

        if (this.dead() || this.health() <= 0) {
            return;
        }

        // Food tick
        this.lastUpdateDT += dT;
        if (Values.CLIENT_TICK_RATE - this.lastUpdateDT < MathUtils.EPSILON) {
            if (!this.dead() && this.shouldTickHunger()) {
                AttributeInstance hungerInstance = this.attributeInstance(Attribute.HUNGER);
                float hunger = hungerInstance.getValue();
                float health = -1;

                Difficulty difficulty = this.world.difficulty();
                if (difficulty == Difficulty.PEACEFUL && this.foodTicks % 10 == 0) {
                    if (hunger < hungerInstance.getMaxValue()) {
                        this.addHunger(1.0f);
                    }

                    if (this.foodTicks % 20 == 0) {
                        health = this.health();
                        if (health < this.maxHealth()) {
                            this.heal(1, EntityHealEvent.Cause.SATURATION);
                        }
                    }
                }

                if (this.foodTicks == 0) {
                    // Check for regeneration
                    if (hunger >= 18) {
                        if (health == -1) {
                            health = this.health();
                        }

                        if (health < this.maxHealth()) {
                            this.heal(1, EntityHealEvent.Cause.SATURATION);
                            this.exhaust(3f, PlayerExhaustEvent.Cause.REGENERATION);
                        }
                    } else if (hunger <= 0) {
                        if (health == -1) {
                            health = this.health();
                        }

                        if ((health > 10 && difficulty == Difficulty.NORMAL) ||
                            (difficulty == Difficulty.HARD && health > 1)) {
                            EntityDamageEvent damageEvent = new EntityDamageEvent(this, EntityDamageEvent.DamageSource.STARVE, 1f);
                            this.damage(damageEvent);
                        }
                    }
                }

                this.foodTicks++;

                if (this.foodTicks >= 80) {
                    this.foodTicks = 0;
                }
            }

            // Breathing
            // Check for block stuff
            boolean breathing = !this.isInsideLiquid() || this.hasEffect(PotionEffect.WATER_BREATHING);
            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.BREATHING, breathing);

            short air = this.metadataContainer.getShort(MetadataContainer.DATA_AIR);
            short maxAir = this.metadataContainer.getShort(MetadataContainer.DATA_MAX_AIRDATA_MAX_AIR);

            if (!breathing) {
                if (--air < 0) {
                    EntityDamageEvent damageEvent = new EntityDamageEvent(this, EntityDamageEvent.DamageSource.DROWNING, 2.0f);
                    damage(damageEvent);
                } else {
                    this.metadataContainer.putShort(MetadataContainer.DATA_AIR, air);
                }
            } else {
                if (air != maxAir) {
                    this.metadataContainer.putShort(MetadataContainer.DATA_AIR, maxAir);
                }
            }

            this.lastUpdateDT = 0;
        }
    }

    protected boolean shouldTickHunger() {
        return false;
    }

    /**
     * Set a player flag
     *
     * @param flag  which should be set
     * @param value to what it should be set, true or false
     */
    public E playerFlag(EntityFlag flag, boolean value) {
        this.metadataContainer.setDataFlag(MetadataContainer.DATA_PLAYER_FLAGS, flag, value);
        return (E) this;
    }

    @Override
    public float exhaustion() {
        return this.attribute(Attribute.EXHAUSTION);
    }

    @Override
    public E exhaustion(float amount) {
        this.attribute(Attribute.EXHAUSTION, amount);
        return (E) this;
    }

    @Override
    public float saturation() {
        return this.attribute(Attribute.SATURATION);
    }

    /**
     * Add to the current saturation level
     *
     * @param amount which should be added to the saturation
     */
    public E addSaturation(float amount) {
        AttributeInstance instance = this.attributeInstance(Attribute.SATURATION);
        return this.saturation(instance.getValue() + amount);
    }

    @Override
    public E saturation(float amount) {
        AttributeInstance instance = this.attributeInstance(Attribute.SATURATION);
        float maxVal = instance.getMaxValue();
        float minVal = instance.getMinValue();
        this.attribute(Attribute.SATURATION, MathUtils.clamp(amount, minVal, maxVal));
        return (E) this;
    }

    @Override
    public float hunger() {
        return this.attribute(Attribute.HUNGER);
    }

    /**
     * Add to the current hunger level
     *
     * @param amount which should be added to the hunger
     */
    public E addHunger(float amount) {
        return this.hunger(this.hunger() + amount);
    }

    public boolean isHungry() {
        AttributeInstance instance = this.attributeInstance(Attribute.HUNGER);
        return instance.getValue() < instance.getMaxValue();
    }

    @Override
    public E hunger(float amount) {
        AttributeInstance instance = this.attributeInstance(Attribute.HUNGER);
        float old = instance.getValue();
        this.attribute(Attribute.HUNGER, MathUtils.clamp(amount, instance.getMinValue(), instance.getMaxValue()));

        if ((old < 17 && amount >= 17) ||
            (old < 6 && amount >= 6) ||
            (old > 0 && amount == 0)) {
            this.foodTicks = 0;
        }

        return (E) this;
    }

    /**
     * Override for the EntityPlayer implementation
     *
     * @param amount of exhaustion
     * @param cause  of the exhaustion
     */
    public E exhaust(float amount, PlayerExhaustEvent.Cause cause) {
        return this.exhaust(amount);
    }

    /**
     * Exhaust for a specific amount
     *
     * @param amount of exhaust
     */
    public E exhaust(float amount) {
        float exhaustion = this.exhaustion() + amount;

        // When exhaustion is over 4 we decrease saturation
        while (exhaustion >= 4) {
            exhaustion -= 4;

            float saturation = this.saturation();
            if (saturation > 0) {
                saturation = Math.max(0, saturation - 1);
                this.saturation(saturation);
            } else {
                float hunger = this.hunger();
                if (hunger > 0) {
                    if (this instanceof EntityPlayer) {
                        PlayerFoodLevelChangeEvent foodLevelChangeEvent = new PlayerFoodLevelChangeEvent(
                            (io.gomint.entity.EntityPlayer) this, -1
                        );

                        this.world.server().pluginManager().callEvent(foodLevelChangeEvent);
                        if (!foodLevelChangeEvent.cancelled()) {
                            hunger = Math.max(0, hunger - 1);
                            this.hunger(hunger);
                        } else {
                            ((EntityPlayer) this).resendAttributes();
                        }
                    } else {
                        hunger = Math.max(0, hunger - 1);
                        this.hunger(hunger);
                    }
                }
            }
        }

        return this.exhaustion(exhaustion);
    }

    @Override
    public E sprinting(boolean value) {
        // Alter movement speed if needed
        if (value != sprinting()) {
            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SPRINTING, value);
            AttributeInstance movementSpeed = this.attributeInstance(Attribute.MOVEMENT_SPEED);
            if (value) {
                movementSpeed.setModifier(AttributeModifier.SPRINT, AttributeModifierType.ADDITION_MULTIPLY, 0.3f);
            } else {
                movementSpeed.removeModifier(AttributeModifier.SPRINT);
            }
        }

        return (E) this;
    }

    @Override
    public boolean sprinting() {
        return this.metadataContainer.getDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SPRINTING);
    }

    @Override
    public E sneaking(boolean value) {
        if (value != sneaking()) {
            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SNEAKING, value);
            if (value) {
                this.size(0.6f, 1.62f);
            } else {
                this.size(0.6f, 1.8f);
            }
        }

        return (E) this;
    }

    @Override
    public boolean sneaking() {
        return this.metadataContainer.getDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SNEAKING);
    }

    @Override
    public E swimming(boolean value) {
        if (value != swimming()) {
            if (value) {
                this.size(0.6f, 0.6f);
            } else {
                this.size(0.6f, 1.8f);
            }

            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SWIMMING, value);
        }

        return (E) this;
    }

    @Override
    public boolean swimming() {
        return this.metadataContainer.getDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SWIMMING);
    }

    @Override
    public E spinning(boolean value) {
        if (value != spinning()) {
            if (value) {
                this.size(0.6f, 0.6f);
            } else {
                this.size(0.6f, 1.8f);
            }

            this.metadataContainer.setDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SPINNING, value);
        }

        return (E) this;
    }

    @Override
    public boolean spinning() {
        return this.metadataContainer.getDataFlag(MetadataContainer.DATA_INDEX, EntityFlag.SPINNING);
    }

    @Override
    protected void kill() {
        super.kill();
    }

    @Override
    public io.gomint.player.PlayerSkin skin() {
        return this.skin;
    }

    @Override
    public String xboxID() {
        return this.xboxId;
    }

    @Override
    public String displayName() {
        return this.displayName;
    }

    @Override
    public E displayName(String displayName) {
        this.displayName = displayName;
        return (E) this;
    }

    @Override
    public E skin(PlayerSkin skin) {
        if (this.skin != null) {
            this.skin = skin;
            this.updatePlayerList();
        } else {
            this.skin = skin;
        }

        return (E) this;
    }

    private void updatePlayerList() {
        List<PacketPlayerlist.Entry> singleEntry = Collections.singletonList(new PacketPlayerlist.Entry(EntityHuman.this));

        PacketPlayerlist packetPlayerlist = new PacketPlayerlist();
        packetPlayerlist.setMode((byte) 0);
        packetPlayerlist.setEntries(singleEntry);

        PacketPlayerlist removeFromList = null;
        if (!(this instanceof EntityPlayer)) {
            removeFromList = new PacketPlayerlist();
            removeFromList.setMode((byte) 1);
            removeFromList.setEntries(singleEntry);
        }

        for (io.gomint.entity.EntityPlayer player : this.world.server().onlinePlayers()) {
            EntityPlayer other = (EntityPlayer) player;
            other.connection().addToSendQueue(packetPlayerlist);
            if (removeFromList != null) {
                other.connection().addToSendQueue(removeFromList);
            }
        }
    }

    @Override
    public PlayerInventory inventory() {
        return this.inventory;
    }

    /**
     * Init data from players
     *
     * @param username    of the player
     * @param displayName which should be the same as the username
     * @param xboxId      of the account, empty string if in offline mode
     * @param uuid        of this player
     */
    public void setPlayerData(String username, String displayName, String xboxId, UUID uuid) {
        this.username = username;
        this.displayName = displayName;
        this.xboxId = xboxId;
        this.uuid = uuid;
        this.playerListName = username;

        this.metadataContainer.putString(MetadataContainer.DATA_NAMETAG, this.username);
    }

    @Override
    public Packet createSpawnPacket(EntityPlayer receiver) {
        PacketSpawnPlayer packetSpawnPlayer = new PacketSpawnPlayer();
        packetSpawnPlayer.setUuid(this.uuid());
        packetSpawnPlayer.setName(this.username);
        packetSpawnPlayer.setEntityId(this.id());
        packetSpawnPlayer.setRuntimeEntityId(this.id());
        packetSpawnPlayer.setPlatformChatId(this.uuid().toString());

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
        packetSpawnPlayer.setDeviceId("");
        packetSpawnPlayer.setBuildPlatform(0);

        return packetSpawnPlayer;
    }

    @Override
    public void preSpawn(PlayerConnection connection) {
        PacketPlayerlist packetPlayerlist = new PacketPlayerlist();
        packetPlayerlist.setMode((byte) 0);
        packetPlayerlist.setEntries(new ArrayList<PacketPlayerlist.Entry>() {{
            add(new PacketPlayerlist.Entry(EntityHuman.this));
        }});

        connection.addToSendQueue(packetPlayerlist);
    }

    @Override
    public void postSpawn(PlayerConnection connection) {
        // TODO: Remove this, its a client bug in 1.2.13
        PacketEntityMetadata metadata = new PacketEntityMetadata();
        metadata.setEntityId(this.id());
        metadata.setMetadata(this.metadataContainer);
        metadata.setTick(this.world.currentTickTime() / (int) Values.CLIENT_TICK_MS);
        connection.addToSendQueue(metadata);

        PacketPlayerlist packetPlayerlist = new PacketPlayerlist();
        packetPlayerlist.setMode((byte) 1);
        packetPlayerlist.setEntries(new ArrayList<>() {{
            add(new PacketPlayerlist.Entry(EntityHuman.this));
        }});

        connection.addToSendQueue(packetPlayerlist);
    }

    @Override
    public void initFromNBT(NBTTagCompound compound) {
        super.initFromNBT(compound);

        this.inventory.initFromNBT(compound.getList("Inventory", false));
    }

    @Override
    public NBTTagCompound persistToNBT() {
        NBTTagCompound compound = super.persistToNBT();

        compound.addValue("Inventory", this.inventory.persistToNBT());

        // Player inventory
        return compound;
    }

    @Override
    public boolean needsFullMovement() {
        return true; // Due to a "bug" in 1.14.30 there needs to be a PlayerMove packet sent which only has absolute coordinates
    }

    @Override
    public Packet getMovementPacket() {
        PacketMovePlayer packetMovePlayer = new PacketMovePlayer();
        packetMovePlayer.setEntityId(this.id());

        packetMovePlayer.setX(this.positionX());
        packetMovePlayer.setY(this.positionY() + this.offsetY());
        packetMovePlayer.setZ(this.positionZ());

        packetMovePlayer.setYaw(this.yaw());
        packetMovePlayer.setHeadYaw(this.headYaw());
        packetMovePlayer.setPitch(this.pitch());

        packetMovePlayer.setOnGround(this.onGround());
        packetMovePlayer.setMode(PacketMovePlayer.MovePlayerMode.NORMAL);
        packetMovePlayer.setTick(this.world.currentTickTime() / (int) Values.CLIENT_TICK_MS);

        return packetMovePlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityHuman<?> that = (EntityHuman<?>) o;
        return this.uuid.getMostSignificantBits() == that.uuid.getMostSignificantBits() &&
                this.uuid.getLeastSignificantBits() == that.uuid.getLeastSignificantBits();
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public String playerListName() {
        return this.playerListName;
    }

    /**
     * Get information about the device the player is using
     *
     * @return device information from this player
     */
    public DeviceInfo deviceInfo() {
        return this.deviceInfo;
    }

    @Override
    public Set<String> tags() {
        return EntityTags.HUMAN;
    }

}
