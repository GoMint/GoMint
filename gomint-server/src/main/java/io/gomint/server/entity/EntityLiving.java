package io.gomint.server.entity;

import io.gomint.entity.passive.EntityHuman;
import io.gomint.entity.potion.PotionEffect;
import io.gomint.entity.projectile.EntityProjectile;
import io.gomint.event.entity.EntityDamageByEntityEvent;
import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.event.entity.EntityHealEvent;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.server.entity.component.AIBehaviourComponent;
import io.gomint.server.entity.metadata.MetadataContainer;
import io.gomint.server.entity.pathfinding.PathfindingEngine;
import io.gomint.server.entity.potion.effect.Effect;
import io.gomint.server.inventory.InventoryHolder;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketEntityEvent;
import io.gomint.server.network.packet.PacketSpawnEntity;
import io.gomint.server.player.EffectManager;
import io.gomint.server.util.EnumConnectors;
import io.gomint.server.util.Precondition;
import io.gomint.server.util.Values;
import io.gomint.server.world.WorldAdapter;
import io.gomint.taglib.NBTTagCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Common base class for all entities that live. All living entities possess
 * an AI which is the significant characteristic that marks an entity as being
 * alive in GoMint's definition.
 *
 * @author BlackyPaw
 * @version 1.0
 */
public abstract class EntityLiving<E extends io.gomint.entity.Entity<E>> extends Entity<E> implements InventoryHolder, io.gomint.entity.EntityLiving<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityLiving.class);

    // AI of the entity:
    protected AIBehaviourComponent behaviour;
    // Pathfinding engine of the entity:
    protected PathfindingEngine pathfinding;

    protected Map<String, AttributeInstance> attributes = new HashMap<>();

    private float lastUpdateDT = 0;
    private final Set<io.gomint.entity.Entity<?>> attachedEntities = new HashSet<>();

    private byte attackCoolDown = 0;

    protected int deadTimer = 0;
    private int fireTicks = 0;

    // Damage stats
    protected float lastDamage = 0;
    protected EntityDamageEvent.DamageSource lastDamageSource;
    protected io.gomint.entity.Entity<?> lastDamageEntity;

    // Effects
    private final EffectManager effectManager = new EffectManager(this);

    /**
     * Constructs a new EntityLiving
     *
     * @param type  The type of the Entity
     * @param world The world in which this entity is in
     */
    protected EntityLiving(EntityType type, WorldAdapter world) {
        super(type, world);
        this.behaviour = new AIBehaviourComponent();
        this.pathfinding = new PathfindingEngine(this.getTransform());
        this.initAttributes();

        this.metadataContainer.putFloat(MetadataContainer.DATA_SCALE, 1.0f);
    }

    private void initAttributes() {
        attribute(Attribute.ABSORPTION);
        attribute(Attribute.ATTACK_DAMAGE);
        attribute(Attribute.FOLLOW_RANGE);
        attribute(Attribute.HEALTH);
        attribute(Attribute.MOVEMENT_SPEED);
        attribute(Attribute.KNOCKBACK_RESISTANCE);
    }

    public float attribute(Attribute attribute) {
        AttributeInstance instance = this.attributes.get(attribute.getKey());
        if (instance != null) {
            return instance.getValue();
        }

        instance = attribute.create();
        this.attributes.put(instance.getKey(), instance);
        return instance.getValue();
    }

    public AttributeInstance attributeInstance(Attribute attribute) {
        return this.attributes.get(attribute.getKey());
    }

    public void attribute(Attribute attribute, float value) {
        AttributeInstance instance = this.attributes.get(attribute.getKey());
        if (instance != null) {
            instance.setValue(value);
        }
    }

    @Override
    protected E fall() {
        // Check for jump potion
        float distanceReduce = 0.0f;
        int jumpAmplifier = effect(PotionEffect.JUMP);
        if (jumpAmplifier != -1) {
            distanceReduce = jumpAmplifier + 1;
        }

        float damage = MathUtils.fastFloor(this.fallDistance - 3f - distanceReduce);
        if (damage > 0) {
            this.attack(damage, EntityDamageEvent.DamageSource.FALL);
        }

        return (E) this;
    }

    // ==================================== UPDATING ==================================== //

    @Override
    public void update(long currentTimeMS, float dT) {
        if (!(this.dead() || this.health() <= 0)) {
            super.update(currentTimeMS, dT);
            this.behaviour.update(currentTimeMS, dT);
        }

        // Check if last hit entity is still alive
        if (this.lastDamageEntity != null && this.lastDamageEntity.dead()) {
            this.lastDamageEntity = null;
        }

        // Only update when alive
        if (!(this.dead() || this.health() <= 0)) {
            // Update effects
            this.effectManager.update(currentTimeMS, dT);
        }

        // Check for client tick stuff
        this.lastUpdateDT += dT;
        if (Values.CLIENT_TICK_RATE - this.lastUpdateDT < MathUtils.EPSILON) {
            // Calc death stuff
            if (this.health() <= 0) {
                if (this.deadTimer > 0 && this.deadTimer-- > 1) {
                    despawn();
                    this.deadTimer = 0;
                }
            } else {
                this.deadTimer = 0;
            }

            if (this.dead() || this.health() <= 0) {
                this.lastUpdateDT = 0;
                return;
            }

            // Reset attack cooldown
            if (this.attackCoolDown > 0) {
                this.attackCoolDown--;
            }

            // Fire?
            if (this.fireTicks > 0) {
                if (this.fireTicks % 20 == 0) {
                    EntityDamageEvent damageEvent = new EntityDamageEvent(this, EntityDamageEvent.DamageSource.ON_FIRE, 1.0f);
                    damage(damageEvent);
                }

                this.fireTicks--;
                if (this.fireTicks == 0) {
                    burning(false);
                }
            }

            io.gomint.server.world.block.Block standingIn = this.world.blockAt(this.position().toBlockPosition());
            standingIn.onEntityStanding(this);

            this.lastUpdateDT = 0;
        }
    }

    /**
     * Set health without checking for correct death cause
     *
     * @param amount of health which should be set. <= 0 kills the entity
     */
    public void setHealthInternal(float amount) {
        if (amount <= 0) {
            this.kill();
        }

        AttributeInstance attributeInstance = this.attributes.get(Attribute.HEALTH.getKey());
        attributeInstance.setValue(Math.min(attributeInstance.getMaxValue(), Math.max(attributeInstance.getMinValue(), amount)));
    }

    @Override
    public E health(float amount) {
        if (amount <= 0) {
            this.lastDamageSource = EntityDamageEvent.DamageSource.API;
            this.lastDamage = 999999;
            this.lastDamageEntity = null;
        }

        this.setHealthInternal(amount);
        return (E) this;
    }

    @Override
    public float health() {
        return this.attribute(Attribute.HEALTH);
    }

    @Override
    public Packet createSpawnPacket(EntityPlayer receiver) {
        // Broadcast spawn entity packet:
        PacketSpawnEntity packet = new PacketSpawnEntity();
        packet.setEntityId(this.id);
        packet.setEntityType(this.type);
        packet.setX(this.positionX());
        packet.setY(this.positionY());
        packet.setZ(this.positionZ());
        packet.setVelocityX(this.getMotionX());
        packet.setVelocityY(this.getMotionY());
        packet.setVelocityZ(this.getMotionZ());
        packet.setPitch(this.pitch());
        packet.setYaw(this.yaw());
        packet.setHeadYaw(this.headYaw());
        packet.setAttributes(this.attributes.values());
        packet.setMetadata(this.metadata());
        return packet;
    }

    @Override
    public E absorptionHearts(float amount) {
        AttributeInstance attributeInstance = this.attributes.get(Attribute.ABSORPTION.getKey());
        attributeInstance.setValue(MathUtils.clamp(amount, attributeInstance.getMinValue(), attributeInstance.getMaxValue()));
        return (E) this;
    }

    @Override
    public float absorptionHearts() {
        return this.attribute(Attribute.ABSORPTION);
    }

    @Override
    public E maxHealth(float amount) {
        this.attributeInstance(Attribute.HEALTH).setMaxValue(amount);
        return (E) this;
    }

    @Override
    public float maxHealth() {
        return this.attributeInstance(Attribute.HEALTH).getMaxValue();
    }

    @Override
    public E heal(float amount, EntityHealEvent.Cause cause) {
        EntityHealEvent event = new EntityHealEvent(this, amount, cause);
        this.world.server().pluginManager().callEvent(event);

        if (!event.cancelled()) {
            this.health(this.health() + amount);
        }

        return (E) this;
    }

    @Override
    public E attack(float damage, EntityDamageEvent.DamageSource source) {
        EntityDamageEvent damageEvent = new EntityDamageEvent(this, source, damage);
        this.damage(damageEvent);
        return (E) this;
    }

    @Override
    public boolean damage(EntityDamageEvent damageEvent) {
        // Don't damage dead entities
        if (this.health() <= 0) {
            return false;
        }

        // Check for effect blocking
        if (hasEffect(PotionEffect.FIRE_RESISTANCE) && (
            damageEvent.damageSource() == EntityDamageEvent.DamageSource.FIRE ||
                damageEvent.damageSource() == EntityDamageEvent.DamageSource.LAVA ||
                damageEvent.damageSource() == EntityDamageEvent.DamageSource.ON_FIRE
        )) {
            return false;
        }

        // Armor calculations
        float damage = applyArmorReduction(damageEvent, false);
        damage = applyEffectReduction(damageEvent, damage);

        // Absorption
        float absorptionHearts = this.absorptionHearts();
        if (absorptionHearts > 0) {
            damage = Math.max(damage - absorptionHearts, 0f);
        }

        // Check for attack timer
        if (this.attackCoolDown > 0 && damage <= this.lastDamage) {
            return false;
        }

        // Call event
        damageEvent.finalDamage(damage);
        if (!super.damage(damageEvent)) {
            return false;
        }

        // Did the final damage change?
        float damageToBeDealt;
        if (damage != damageEvent.finalDamage()) {
            damageToBeDealt = damageEvent.finalDamage();
        } else {
            damageToBeDealt = applyArmorReduction(damageEvent, true);
            damageToBeDealt = applyEffectReduction(damageEvent, damageToBeDealt);

            absorptionHearts = this.absorptionHearts();
            if (absorptionHearts > 0) {
                float oldDamage = damageToBeDealt;
                damageToBeDealt = Math.max(damage - absorptionHearts, 0f);
                this.absorptionHearts(absorptionHearts - (oldDamage - damageToBeDealt));
            }
        }

        float health = MathUtils.fastCeil(this.health() - damageToBeDealt);

        // Send animation
        if (health > 0) {
            PacketEntityEvent entityEvent = new PacketEntityEvent();
            entityEvent.setEntityId(this.id);
            entityEvent.setEventId(EntityEvent.HURT.getId());

            for (io.gomint.entity.Entity<?> attachedEntity : this.attachedEntities) {
                EntityPlayer entityPlayer = (EntityPlayer) attachedEntity;
                entityPlayer.connection().addToSendQueue(entityEvent);
            }

            if (this instanceof EntityPlayer) {
                ((EntityPlayer) this).connection().addToSendQueue(entityEvent);
            }
        }

        if (damageEvent instanceof EntityDamageByEntityEvent) {
            // Knockback
            Entity<?> entity = (Entity<?>) ((EntityDamageByEntityEvent) damageEvent).attacker();
            float diffX = this.positionX() - entity.positionX();
            float diffZ = this.positionZ() - entity.positionZ();

            float distance = (float) Math.sqrt(diffX * diffX + diffZ * diffZ);
            if (distance > 0.0) {
                float baseModifier = 0.4F;

                distance = 1 / distance;

                Vector motion = this.velocity();
                motion = motion.divide(2f, 2f, 2f);
                motion = motion.add(
                    (diffX * distance * baseModifier),
                    baseModifier,
                    (diffZ * distance * baseModifier)
                );

                if (motion.y() > baseModifier) {
                    motion.y(baseModifier);
                }

                this.velocity(motion, true);
            }
        }

        this.lastDamage = damage;
        this.lastDamageSource = damageEvent.damageSource();
        this.lastDamageEntity = (damageEvent instanceof EntityDamageByEntityEvent) ? ((EntityDamageByEntityEvent) damageEvent).attacker() : null;

        // Set health
        this.setHealthInternal(health <= 0 ? 0 : health);

        this.attackCoolDown = 10;
        return true;
    }
    
    protected boolean isLastDamageCausedByPlayer() {
        var lastDamageEntity = this.lastDamageEntity;
        return lastDamageEntity instanceof EntityHuman || 
            (lastDamageEntity instanceof EntityProjectile 
                && ((EntityProjectile<?>) lastDamageEntity).shooter() instanceof EntityHuman);
    }

    protected float applyEffectReduction(EntityDamageEvent damageEvent, float damage) {
        // Starve is absolute damage
        if (damageEvent.damageSource() == EntityDamageEvent.DamageSource.STARVE) {
            return damage;
        }

        int damageResistanceAmplifier = effect(PotionEffect.DAMAGE_RESISTANCE);
        if (damageResistanceAmplifier != -1 && damageEvent.damageSource() != EntityDamageEvent.DamageSource.VOID) {
            float maxReductionDiff = 25f - ((damageResistanceAmplifier + 1) * 5);
            float amplifiedDamage = damage * maxReductionDiff;
            damage = amplifiedDamage / 25.0F;
        }

        return Math.max(damage, 0.0f);
    }

    /**
     * Reset fire status on kill
     */
    protected void kill() {
        this.deadTimer = 20;

        // Send animation
        PacketEntityEvent entityEvent = new PacketEntityEvent();
        entityEvent.setEntityId(this.id);
        entityEvent.setEventId(EntityEvent.DEATH.getId());

        for (io.gomint.entity.Entity<?> attachedEntity : this.attachedEntities) {
            EntityPlayer entityPlayer = (EntityPlayer) attachedEntity;
            entityPlayer.connection().addToSendQueue(entityEvent);
        }

        this.fireTicks = 0;
        burning(false);

        this.effectManager.removeAll();
    }

    /**
     * Apply reduction based on the armor value of a entity
     *
     * @param damageEvent which wants to deal damage
     * @param damageArmor should we damage the armor?
     * @return damage left over after removing armor reductions
     */
    protected float applyArmorReduction(EntityDamageEvent damageEvent, boolean damageArmor) {
        return damageEvent.damage();
    }

    @Override
    public void attach(EntityPlayer player) {
        Precondition.safeWorldAccess(this.world, false);

        this.attachedEntities.add(player);
        this.effectManager.sendForPlayer(player);
    }

    @Override
    public void detach(EntityPlayer player) {
        Precondition.safeWorldAccess(this.world, false);

        this.attachedEntities.remove(player);
    }

    public void resetAttributes() {
        for (AttributeInstance instance : this.attributes.values()) {
            instance.reset();
        }
    }

    @Override
    void dealVoidDamage() {
        EntityDamageEvent damageEvent = new EntityDamageEvent(this,
            EntityDamageEvent.DamageSource.VOID, 4.0F);
        this.damage(damageEvent);
    }

    @Override
    public E burning(long duration, TimeUnit unit) {
        int newFireTicks = (int) (unit.toMillis(duration) / Values.CLIENT_TICK_MS);
        if (newFireTicks > this.fireTicks) {
            this.fireTicks = newFireTicks;
            burning(true);
        } else if (newFireTicks == 0) {
            this.fireTicks = 0;
            burning(false);
        }

        return (E) this;
    }

    @Override
    public E extinguish() {
        this.burning(0, TimeUnit.SECONDS);
        return (E) this;
    }

    @Override
    public io.gomint.entity.potion.Effect effect(PotionEffect effect, int amplifier, long duration, TimeUnit timeUnit) {
        byte effectId = (byte) EnumConnectors.POTION_EFFECT_CONNECTOR.convert(effect).getId();
        Effect effectInstance = this.world.server().effects().generate(effectId, amplifier,
            this.world.currentTickTime() + timeUnit.toMillis(duration), this.effectManager);

        if (effectInstance != null) {
            this.effectManager.addEffect(effectId, effectInstance);
        }

        return effectInstance;
    }

    @Override
    public boolean hasEffect(PotionEffect effect) {
        byte effectId = (byte) EnumConnectors.POTION_EFFECT_CONNECTOR.convert(effect).getId();
        return this.effectManager.hasEffect(effectId);
    }

    @Override
    public int effect(PotionEffect effect) {
        byte effectId = (byte) EnumConnectors.POTION_EFFECT_CONNECTOR.convert(effect).getId();
        return this.effectManager.getEffectAmplifier(effectId);
    }

    @Override
    public E removeEffect(PotionEffect effect) {
        byte effectId = (byte) EnumConnectors.POTION_EFFECT_CONNECTOR.convert(effect).getId();
        this.effectManager.removeEffect(effectId);
        return (E) this;
    }

    @Override
    public E removeAllEffects() {
        this.effectManager.removeAll();
        return (E) this;
    }

    @Override
    public float movementSpeed() {
        return this.attribute(Attribute.MOVEMENT_SPEED);
    }

    @Override
    public E movementSpeed(float value) {
        this.attribute(Attribute.MOVEMENT_SPEED, value);
        return (E) this;
    }

    @Override
    public void initFromNBT(NBTTagCompound compound) {
        super.initFromNBT(compound);

        if (compound.containsKey("AbsorptionAmount")) {
            this.absorptionHearts(compound.getFloat("AbsorptionAmount", 0.0f));
        }

        this.effectManager.initFromNBT(compound);

        List<Object> nbtAttributes = compound.getList("Attributes", false);
        if (nbtAttributes != null) {
            for (Object attribute : nbtAttributes) {
                NBTTagCompound nbtAttribute = (NBTTagCompound) attribute;
                String name = nbtAttribute.getString("Name", null);
                if (name != null) {
                    AttributeInstance instance = null;

                    for (Attribute value : Attribute.values()) {
                        if (value.getKey().equals(name)) {
                            instance = value.create();
                        }
                    }

                    if (instance != null) {
                        instance.initFromNBT(nbtAttribute);
                        this.attributes.put(name, instance);
                    }
                }
            }
        }

        this.deadTimer = compound.getShort("DeathTime", (short) 0);
        this.attackCoolDown = compound.getShort("HurtTime", (short) 0).byteValue();
    }

    @Override
    public NBTTagCompound persistToNBT() {
        NBTTagCompound compound = super.persistToNBT();

        if (this.absorptionHearts() > 0) {
            compound.addValue("AbsorptionAmount", this.absorptionHearts());
        }

        if (this.effectManager.hasActiveEffect()) {
            this.effectManager.persistToNBT(compound);
        }

        List<NBTTagCompound> nbtAttributes = new ArrayList<>();
        for (Map.Entry<String, AttributeInstance> entry : this.attributes.entrySet()) {
            nbtAttributes.add(entry.getValue().persistToNBT());
        }

        compound.addValue("Attributes", nbtAttributes);
        compound.addValue("DeathTime", (short) this.deadTimer);
        compound.addValue("HurtTime", (short) this.attackCoolDown);

        return compound;
    }

    public Set<io.gomint.entity.Entity<?>> getAttachedEntities() {
        return this.attachedEntities;
    }

    @Override
    public EntityDamageEvent.DamageSource lastDamageSource() {
        return this.lastDamageSource;
    }

    @Override
    public io.gomint.entity.Entity<?> lastDamageEntity() {
        return this.lastDamageEntity;
    }

}
