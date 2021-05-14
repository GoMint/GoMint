/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.entity;

import com.google.common.base.Preconditions;
import io.gomint.entity.BossBar;
import io.gomint.event.entity.EntityDamageEvent;
import io.gomint.event.entity.EntityTeleportEvent;
import io.gomint.event.entity.EntityVelocityEvent;
import io.gomint.math.AxisAlignedBB;
import io.gomint.math.Location;
import io.gomint.math.MathUtils;
import io.gomint.math.Vector;
import io.gomint.math.Vector2;
import io.gomint.server.entity.component.TransformComponent;
import io.gomint.server.entity.metadata.MetadataContainer;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.Packet;
import io.gomint.server.network.packet.PacketEntityMetadata;
import io.gomint.server.network.packet.PacketEntityMotion;
import io.gomint.server.network.packet.PacketEntityMovement;
import io.gomint.server.network.packet.PacketSpawnEntity;
import io.gomint.server.util.Precondition;
import io.gomint.server.util.Values;
import io.gomint.server.world.CoordinateUtils;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.Block;
import io.gomint.server.world.block.FlowingWater;
import io.gomint.server.world.block.Ladder;
import io.gomint.server.world.block.Liquid;
import io.gomint.server.world.block.StationaryWater;
import io.gomint.server.world.block.Vines;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for all entities. Defines accessors to attributes and components that are
 * common to all entities such as ID, type and transformation.
 *
 * @author BlackyPaw
 * @version 1.1
 */
public abstract class Entity<E extends io.gomint.entity.Entity<E>> implements io.gomint.entity.Entity<E> {

    private static final Logger LOGGER = LoggerFactory.getLogger( Entity.class );
    private static final AtomicLong ENTITY_ID = new AtomicLong( 0 );
    /**
     * The id of this entity
     */
    protected final long id;

    /**
     * Type of the entity
     */
    protected final EntityType type;

    /**
     * Metadata
     */
    protected final MetadataContainer metadataContainer;
    // Useful stuff for movement. Those are values for per client tick
    protected float gravity = 0.08f;
    protected float drag = 0.02f;

    /**
     * Bounding Box
     */
    protected AxisAlignedBB boundingBox;
    protected float height;

    /**
     * How high can this entity "climb" in one movement?
     */
    protected float stepHeight = 0;
    protected boolean onGround;

    /**
     * Collision states
     */
    // CHECKSTYLE:OFF
    protected boolean isCollidedVertically;
    protected boolean isCollidedHorizontally;
    protected boolean isCollided;
    protected Set<Block> collidedWith = new HashSet<>();

    /**
     * Fall distance tracking
     */
    protected float fallDistance = 0;

    /**
     * Since MC:PE movements are eye instead of foot based we need to offset by this amount
     */
    protected float eyeHeight;
    // CHECKSTYLE:ON
    protected float offsetY;
    protected int age;
    protected volatile WorldAdapter world;
    protected boolean ticking = true;
    private float width;
    private boolean stuckInBlock = false;

    /**
     * Dead status
     */
    private boolean dead;
    private TransformComponent transform;
    private float lastUpdateDT;
    private List<EntityLink> links;
    // Some tracker for "smooth" movement
    private int stuckInBlockTicks = 0;

    private io.gomint.server.entity.BossBar bossBar;

    /**
     * Hidden status
     */
    private boolean hideByDefault;
    private Set<EntityPlayer> shownFor;

    /**
     * Movement status
     */
    private int nextFullMovement = 20;
    private Location oldPosition;

    /**
     * Construct a new Entity
     *
     * @param type  The type of the Entity
     * @param world The world in which this entity is in
     */
    protected Entity( EntityType type, WorldAdapter world ) {
        this.id = ENTITY_ID.incrementAndGet();
        this.type = type;
        this.world = world;
        this.metadataContainer = new MetadataContainer();
        this.metadataContainer.putLong( MetadataContainer.DATA_INDEX, 0 );
        this.metadataContainer.putInt( MetadataContainer.DATA_VARIANT, 0 );
        this.metadataContainer.putInt( MetadataContainer.DATA_POTION_COLOR, 0 );
        this.scale( 1.0f );
        this.transform = new TransformComponent();
        this.boundingBox = new AxisAlignedBB( 0, 0, 0, 0, 0, 0 );

        // Set some default stuff
        this.collision( true );
        this.affectedByGravity( true );
        this.nameTagVisible( true );

        // Apply custom properties
        this.applyCustomProperties();
    }

    /**
     * Hook which gets called when all default properties are set
     */
    protected void applyCustomProperties() {
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void setFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    @Override
    public float fallDistance() {
        return this.fallDistance;
    }

    @Override
    public float eyeHeight() {
        return this.eyeHeight;
    }

    public float offsetY() {
        return this.offsetY;
    }

    @Override
    public boolean ticking() {
        return this.ticking;
    }

    @Override
    public E ticking(boolean ticking) {
        this.ticking = ticking;
        return (E) this;
    }

    public float width() {
        return this.width;
    }

    @Override
    public boolean dead() {
        return this.dead;
    }

    public E dead(boolean dead) {
        this.dead = dead;
        return (E) this;
    }

    public List<EntityLink> links() {
        return this.links;
    }

    public Location oldPosition() {
        return this.oldPosition;
    }

    public float height() {
        return this.height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return this.id == entity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "Entity{" +
            "id=" + this.id +
            '}';
    }

    // ==================================== ACCESSORS ==================================== //

    @Override
    public long id() {
        return this.id;
    }

    /**
     * Gets the type of this entity.
     *
     * @return The type of this entity
     */
    public EntityType type() {
        return this.type;
    }

    @Override
    public WorldAdapter world() {
        return this.world;
    }

    public void world(WorldAdapter world) {
        Preconditions.checkNotNull(world, "world");
        Precondition.safeWorldAccess(world, false);
        this.world = world;
    }

    /**
     * Gets a metadata container containing all metadata values of this entity.
     *
     * @return This entity's metadata
     */
    public MetadataContainer metadata() {
        return this.metadataContainer;
    }

    // ==================================== UPDATING ==================================== //

    /**
     * Despawns this entity if it is currently spawned into any world.
     */
    @Override
    public void despawn() {
        this.dead = true;
    }

    /**
     * Updates the entity and all components attached to it.
     *
     * @param currentTimeMS The current system time in milliseconds
     * @param dT            The time that has passed since the last tick in 1/s
     */
    public void update( long currentTimeMS, float dT ) {
        if ( this.dead ) {
            return;
        }

        this.transform.update( currentTimeMS, dT );

        this.lastUpdateDT += dT;
        if ( Values.CLIENT_TICK_RATE - this.lastUpdateDT < MathUtils.EPSILON ) {
            this.age++;

            if ( !immobile() ) {
                float movX = this.getMotionX();
                float movY = this.getMotionY();
                float movZ = this.getMotionZ();

                Vector moved = null;
                if ( this.shouldMove() ) {
                    moved = this.safeMove( movX, movY, movZ );
                }

                if ( this.affectedByGravity() ) {
                    // Calc motion
                    this.transform.manipulateMotion( 0, -this.gravity, 0 );

                    // Calculate friction
                    float friction = 1 - this.drag;
                    if ( this.onGround && ( Math.abs( this.getMotionX() ) > 0.00001 || Math.abs( this.getMotionZ() ) > 0.00001 ) ) {
                        friction = this.world.blockAt( (int) this.positionX(),
                            (int) ( this.positionY() - 1 ),
                            (int) this.positionZ() ).frictionFactor() * 0.91f;
                    }

                    // Calculate new motion
                    float newMovX = this.transform.motionX() * friction;
                    float newMovY = this.transform.motionY() * ( 1 - this.drag);
                    float newMovZ = this.transform.motionZ() * friction;

                    this.transform.motion( newMovX, newMovY, newMovZ );
                    this.checkAfterGravity();
                }

                if ( moved != null ) {
                    // We did not move so we collided, set motion to 0 to escape hell
                    if ( movX != moved.x() ) {
                        this.transform.setMotionX( 0 );
                    }

                    if ( movY != moved.y() ) {
                        this.transform.setMotionY( 0 );
                    }

                    if ( movZ != moved.z() ) {
                        this.transform.setMotionZ( 0 );
                    }
                }
            }

            // Check for block collision
            this.checkBlockCollisions();

            // Check for void damage
            if ( this.positionY() < -16.0f ) {
                this.dealVoidDamage();
            }

            this.lastUpdateDT = 0;
        }

        // Check if we need to update the bounding box
        if ( this.transform.dirty() ) {
            this.boundingBox.bounds(
                this.positionX() - ( this.width / 2 ),
                this.positionY(),
                this.positionZ() - ( this.width / 2 ),
                this.positionX() + ( this.width / 2 ),
                this.positionY() + this.height,
                this.positionZ() + ( this.width / 2 )
            );

            this.transform.move( 0, 0, 0 );
        }
    }

    protected void checkBlockCollisions() {
        List<io.gomint.world.block.Block> blockList = this.world.getCollisionBlocks( this, true );
        if ( blockList != null ) {
            Vector pushedByBlocks = new Vector( 0, 0, 0 );

            for ( io.gomint.world.block.Block block : blockList ) {
                io.gomint.server.world.block.Block implBlock = (io.gomint.server.world.block.Block) block;
                implBlock.onEntityCollision( this );
                pushedByBlocks = implBlock.addVelocity( this, pushedByBlocks );
            }

            if ( pushedByBlocks.length() > 0 ) {
                pushedByBlocks = pushedByBlocks.normalize().multiply( 0.014f );
                Vector newMotion = this.transform.motion().add( pushedByBlocks );
                this.transform.motion( newMotion.x(), newMotion.y(), newMotion.z() );
                this.broadCastMotion();
            }
        }
    }

    /**
     * Allow for custom capping of gravity
     */
    protected void checkAfterGravity() {

    }

    protected boolean shouldMove() {
        return true;
    }

    /**
     * Move by given motion. This check for block collisions and pushes entity out of blocks when needed
     *
     * @param movX x axis movement
     * @param movY y axis movement
     * @param movZ z axis movement
     * @return vector with the actual done movement
     */
    public Vector safeMove( float movX, float movY, float movZ ) {
        // Security check so we don't move and collect bounding boxes like crazy
        if ( Math.abs( movX ) > 20 || Math.abs( movZ ) > 20 || Math.abs( movY ) > 20 ) {
            return Vector.ZERO;
        }

        float dX = movX;
        float dY = movY;
        float dZ = movZ;

        AxisAlignedBB oldBoundingBox = this.boundingBox.clone();

        // Check if we collide with some blocks when we would move that fast
        List<AxisAlignedBB> collisionList = this.world.collisionCubes( this, this.boundingBox.offsetBoundingBox( dX, dY, dZ ), false );
        if ( collisionList != null && !this.stuckInBlock ) {
            // Check if we would hit a y border block
            for ( AxisAlignedBB axisAlignedBB : collisionList ) {
                dY = axisAlignedBB.calculateYOffset( this.boundingBox, dY );
                if ( dY != movY ) {
                    Block block = this.world.blockAt( (int) axisAlignedBB.minX(), (int) axisAlignedBB.minY(), (int) axisAlignedBB.minZ() );
                    this.collidedWith.add( block );
                }
            }

            this.boundingBox.offset( 0, dY, 0 );

            // Check if we would hit a x border block
            for ( AxisAlignedBB axisAlignedBB : collisionList ) {
                dX = axisAlignedBB.calculateXOffset( this.boundingBox, dX );
                if ( dX != movX ) {
                    Block block = this.world.blockAt( (int) axisAlignedBB.minX(), (int) axisAlignedBB.minY(), (int) axisAlignedBB.minZ() );
                    LOGGER.debug( "Entity {} collided with {}", this, block );

                    this.collidedWith.add( block );
                }
            }

            this.boundingBox.offset( dX, 0, 0 );

            // Check if we would hit a z border block
            for ( AxisAlignedBB axisAlignedBB : collisionList ) {
                dZ = axisAlignedBB.calculateZOffset( this.boundingBox, dZ );
                if ( dZ != movZ ) {
                    Block block = this.world.blockAt( (int) axisAlignedBB.minX(), (int) axisAlignedBB.minY(), (int) axisAlignedBB.minZ() );
                    LOGGER.debug( "Entity {} collided with {}", this, block );

                    this.collidedWith.add( block );
                }
            }

            this.boundingBox.offset( 0, 0, dZ );
        } else {
            this.boundingBox.offset( dX, dY, dZ );
        }

        // Check if we can jump
        boolean notFallingFlag = ( this.onGround || ( dY != movY && movY < 0 ) );
        if ( this.stepHeight > 0 && notFallingFlag && ( movX != dX || movZ != dZ ) ) {
            float oldDX = dX;
            float oldDY = dY;
            float oldDZ = dZ;

            dX = movX;
            dY = this.stepHeight;
            dZ = movZ;

            // Save and restore old bounding box
            AxisAlignedBB oldBoundingBox1 = this.boundingBox.clone();
            this.boundingBox.bounds( oldBoundingBox );

            // Check for collision
            collisionList = this.world.collisionCubes( this, this.boundingBox.addCoordinates( dX, dY, dZ ), false );
            if ( collisionList != null ) {
                // Check if we would hit a y border block
                for ( AxisAlignedBB axisAlignedBB : collisionList ) {
                    dY = axisAlignedBB.calculateYOffset( this.boundingBox, dY );
                }

                this.boundingBox.offset( 0, dY, 0 );

                // Check if we would hit a x border block
                for ( AxisAlignedBB axisAlignedBB : collisionList ) {
                    dX = axisAlignedBB.calculateXOffset( this.boundingBox, dX );
                }

                this.boundingBox.offset( dX, 0, 0 );

                // Check if we would hit a z border block
                for ( AxisAlignedBB axisAlignedBB : collisionList ) {
                    dZ = axisAlignedBB.calculateZOffset( this.boundingBox, dZ );
                }

                this.boundingBox.offset( 0, 0, dZ );
            }

            // Check if we moved left or right
            if ( MathUtils.square( oldDX ) + MathUtils.square( oldDZ ) >= MathUtils.square( dX ) + MathUtils.square( dZ ) ) {
                // Revert this decision of moving the bounding box up
                dX = oldDX;
                dY = oldDY;
                dZ = oldDZ;
                this.boundingBox.bounds( oldBoundingBox1 );
            }
        }

        if ( dX != 0 || dY != 0 || dZ != 0 ) {
            // Move by new bounding box
            this.transform.position(
                ( this.boundingBox.minX() + this.boundingBox.maxX() ) / 2,
                this.boundingBox.minY(),
                ( this.boundingBox.minZ() + this.boundingBox.maxZ() ) / 2
            );
        }

        // Check for grounding states
        this.checkIfCollided( movX, movY, movZ, dX, dY, dZ );
        this.updateFallState( dY );

        // Check if we are stuck in a block
        if ( this.needsToBePushedOutOfBlocks() ) {
            this.checkInsideBlock();
        }

        return new Vector( dX, dY, dZ );
    }

    /**
     * Does this entity needs to be pushed out of blocks when stuck?
     *
     * @return true or false, depending of it needing to be pushed
     */
    protected boolean needsToBePushedOutOfBlocks() {
        return true;
    }

    private void updateFallState( float dY ) {
        // When we are onground again we need to deal damage
        if ( this.onGround ) {
            if ( this.fallDistance > 0 ) {
                this.fall();
            }

            this.fallDistance = 0;
        } else if ( dY < 0 ) {
            this.fallDistance -= dY;
        }
    }

    /**
     * Handle falling of entities
     */
    protected abstract E fall();

    /**
     * Check if this entity is collided
     *
     * @param movX the amount of x which the entity has moved
     * @param movY the amount of y which the entity has moved
     * @param movZ the amount of z which the entity has moved
     * @param dX   the amount of x which the entity should have moved
     * @param dY   the amount of y which the entity should have moved
     * @param dZ   the amount of z which the entity should have moved
     */
    protected void checkIfCollided( float movX, float movY, float movZ, float dX, float dY, float dZ ) {
        // Check if we collided with something
        this.isCollidedVertically = movY != dY;
        this.isCollidedHorizontally = ( movX != dX || movZ != dZ );
        this.isCollided = ( this.isCollidedHorizontally || this.isCollidedVertically );
        this.onGround = ( movY != dY && movY < 0 );
    }

    // ==================================== TRANSFORMATION ==================================== //

    private void checkInsideBlock() {
        // Check in which block we are
        int fullBlockX = MathUtils.fastFloor( this.transform.positionX() );
        int fullBlockY = MathUtils.fastFloor( this.transform.positionY() );
        int fullBlockZ = MathUtils.fastFloor( this.transform.positionZ() );

        // Are we stuck inside a block?
        Block block = this.world.blockAt( fullBlockX, fullBlockY, fullBlockZ );
        if ( block.solid() && block.intersectsWith( this.boundingBox ) ) {
            // We need to check for "smooth" movement when its a player (it climbs .5 steps in .3 -> .420 -> .468 .487 .495 .498 .499 steps
            if ( this instanceof EntityPlayer && ( this.stuckInBlockTicks++ <= 20 || ( (EntityPlayer) this ).adventureSettings().isNoClip() ) ) { // Yes we can "smooth" for up to 20 ticks, thanks mojang :D
                return;
            }

            LOGGER.debug( "Entity {}({}) [{}] @ {} is stuck in a block {} @ {} -> {}",
                this.getClass().getSimpleName(), this.id(), this.stuckInBlockTicks, this.location(), block.getClass().getSimpleName(), block.position(), block.boundingBoxes() );

            // Calc with how much force we can get out of here, this depends on how far we are in
            float diffX = this.transform.positionX() - fullBlockX;
            float diffY = this.transform.positionY() - fullBlockY;
            float diffZ = this.transform.positionZ() - fullBlockZ;

            // Random out the force
            double force = Math.random() * 0.2 + 0.1;

            // Check for free blocks
            boolean freeMinusX = !this.world.blockAt( fullBlockX - 1, fullBlockY, fullBlockZ ).solid();
            boolean freePlusX = !this.world.blockAt( fullBlockX + 1, fullBlockY, fullBlockZ ).solid();
            boolean freeMinusY = !this.world.blockAt( fullBlockX, fullBlockY - 1, fullBlockZ ).solid();
            boolean freePlusY = !this.world.blockAt( fullBlockX, fullBlockY + 1, fullBlockZ ).solid();
            boolean freeMinusZ = !this.world.blockAt( fullBlockX, fullBlockY, fullBlockZ - 1 ).solid();
            boolean freePlusZ = !this.world.blockAt( fullBlockX, fullBlockY, fullBlockZ + 1 ).solid();

            // Since we want the lowest amount of push we have to select the smallest side
            byte direction = -1;
            float lowest = 9999;

            // The -X side is free, use it for now
            if ( freeMinusX ) {
                direction = 0;
                lowest = diffX;
            }

            // Choose +X side only when free and we need to move less
            if ( freePlusX && 1 - diffX < lowest ) {
                direction = 1;
                lowest = 1 - diffX;
            }

            // Choose -Y side only when free and we need to move less
            if ( freeMinusY && diffY < lowest ) {
                direction = 2;
                lowest = diffY;
            }

            // Choose +Y side only when free and we need to move less
            if ( freePlusY && 1 - diffY < lowest ) {
                direction = 3;
                lowest = 1 - diffY;
            }

            // Choose -Z side only when free and we need to move less
            if ( freeMinusZ && diffZ < lowest ) {
                direction = 4;
                lowest = diffZ;
            }

            // Choose +Z side only when free and we need to move less
            if ( freePlusZ && 1 - diffZ < lowest ) {
                direction = 5;
            }

            // Push to the side we selected
            switch ( direction ) {
                case 0:
                    this.transform.motion( (float) -force, 0, 0 );
                    break;
                case 1:
                    this.transform.motion( (float) force, 0, 0 );
                    break;
                case 2:
                    this.transform.motion( 0, (float) -force, 0 );
                    break;
                case 3:
                    this.transform.motion( 0, (float) force, 0 );
                    break;
                case 4:
                    this.transform.motion( 0, 0, (float) -force );
                    break;
                case 5:
                    this.transform.motion( 0, 0, (float) force );
                    break;
            }

            this.stuckInBlock = true;
            this.broadCastMotion();
        } else {
            this.stuckInBlock = false;
            this.stuckInBlockTicks = 0;
        }
    }

    /**
     * Gets the entity's transform as a Transformable.
     *
     * @return The entity's transform
     */
    public Transformable getTransform() {
        return this.transform;
    }

    @Override
    public Location location() {
        return this.transform.toLocation( this.world );
    }

    /**
     * Set the given velocity
     *
     * @param velocity which should be applied to the entity
     * @param send     true when the entity should get the velocity
     */
    public E velocity(Vector velocity, boolean send ) {
        EntityVelocityEvent event = new EntityVelocityEvent( this, velocity );
        this.world.server().pluginManager().callEvent( event );
        if ( event.cancelled() ) {
            return (E) this;
        }

        LOGGER.debug( "New motion for {}: {}", this, event.velocity() );
        this.transform.motion( event.velocity().x(), event.velocity().y(), event.velocity().z() );
        this.fallDistance = 0;

        if ( send ) {
            this.broadCastMotion();
        }

        return (E) this;
    }

    /**
     * Broadcast the current motion to all surrounding (visible) entities
     */
    void broadCastMotion() {
        PacketEntityMotion motion = new PacketEntityMotion();
        motion.setEntityId( this.id() );
        motion.setVelocity( this.transform.motion() );

        this.world.sendToVisible( this.transform.position().toBlockPosition(), motion,
            entity -> entity instanceof EntityPlayer && ( (EntityPlayer) entity ).entityVisibilityManager().isVisible( Entity.this ) );
    }

    /**
     * Gets the motion of the entity on the x axis.
     *
     * @return The motion of the entity on the x axis
     */
    public float getMotionX() {
        return this.transform.motionX();
    }

    /**
     * Gets the motion of the entity on the y axis.
     *
     * @return The motion of the entity on the y axis
     */
    public float getMotionY() {
        return this.transform.motionY();
    }

    /**
     * Gets the motion of the entity on the z axis.
     *
     * @return The motion of the entity on the z axis
     */
    public float getMotionZ() {
        return this.transform.motionZ();
    }

    /**
     * Gets the position of the entity on the x axis.
     *
     * @return The position of the entity on the x axis
     */
    @Override
    public float positionX() {
        return this.transform.positionX();
    }

    /**
     * Gets the position of the entity on the y axis.
     *
     * @return The position of the entity on the y axis
     */
    @Override
    public float positionY() {
        return this.transform.positionY();
    }

    /**
     * Gets the position of the entity on the z axis.
     *
     * @return The position of the entity on the z axis
     */
    @Override
    public float positionZ() {
        return this.transform.positionZ();
    }

    /**
     * Gets the position of the entity as a vector.
     *
     * @return The position of the entity as a vector
     */
    public Vector position() {
        return this.transform.position();
    }

    /**
     * Sets the entity's position given a vector.
     *
     * @param position The position to set
     */
    public E position(Vector position ) {
        this.transform.position( position );
        this.recalcBoundingBox();
        return (E) this;
    }

    /**
     * Gets the yaw angle of the entity's body.
     *
     * @return The yaw angle of the entity's body
     */
    @Override
    public float yaw() {
        return this.transform.yaw();
    }

    /**
     * Sets the yaw angle of the entity's body.
     *
     * @param yaw The yaw angle to set
     */
    @Override
    public E yaw(float yaw ) {
        this.transform.yaw( yaw );
        return (E) this;
    }

    /**
     * Gets the yaw angle of the entity's head.
     *
     * @return The yaw angle of the entity's head
     */
    @Override
    public float headYaw() {
        return this.transform.headYaw();
    }

    /**
     * Sets the yaw angle of the entity's head.
     *
     * @param headYaw The yaw angle to set
     */
    @Override
    public E headYaw(float headYaw ) {
        this.transform.headYaw( headYaw );
        return (E) this;
    }

    /**
     * Gets the pitch angle of the entity's head.
     *
     * @return The pitch angle of the entity's head
     */
    @Override
    public float pitch() {
        return this.transform.pitch();
    }

    /**
     * Sets the pitch angle of the entity's head.
     *
     * @param pitch The pitch angle to set.
     */
    @Override
    public E pitch(float pitch ) {
        this.transform.pitch( pitch );
        return (E) this;
    }

    @Override
    public Vector direction() {
        return this.transform.direction();
    }

    @Override
    public Vector2 directionPlane() {
        return ( new Vector2( (float) -Math.cos( Math.toRadians( this.transform.yaw() ) - ( Math.PI / 2 ) ),
            (float) -Math.sin( Math.toRadians( this.transform.yaw() ) - ( Math.PI / 2 ) ) ) ).normalize();
    }

    /**
     * Gets the direction the entity's head is facing as a normalized vector.
     *
     * @return The direction vector the entity's head is facing
     */
    public Vector headDirection() {
        return this.transform.headDirection();
    }

    /**
     * Sets the entity's position given the respective coordinates on the 3 axes.
     *
     * @param positionX The x coordinate of the position
     * @param positionY The y coordinate of the position
     * @param positionZ The z coordinate of the position
     */
    public E position(float positionX, float positionY, float positionZ ) {
        this.transform.position( positionX, positionY, positionZ );
        this.recalcBoundingBox();
        return (E) this;
    }

    /**
     * Moves the entity by the given offset vector. Produces the same result as
     * <pre>
     * {@code
     * Entity.setPosition( Entity.getPosition().add( offsetX, offsetY, offsetZ ) );
     * }
     * </pre>
     *
     * @param offsetX The x component of the offset
     * @param offsetY The y component of the offset
     * @param offsetZ The z component of the offset
     */
    public E move( float offsetX, float offsetY, float offsetZ ) {
        this.transform.move( offsetX, offsetY, offsetZ );
        return (E) this;
    }

    /**
     * Moves the entity by the given offset vector. Produces the same result as
     * <pre>
     * {@code
     * Entity.setPosition( Entity.getPosition().add( offsetX, offsetY, offsetZ ) );
     * }
     * </pre>
     *
     * @param offset The offset vector to apply to the entity
     */
    public E move( Vector offset ) {
        this.transform.move( offset );
        return (E) this;
    }

    /**
     * Rotates the entity's body around the yaw axis (vertical axis).
     *
     * @param yaw The yaw value by which to rotate the entity
     */
    public E rotateYaw( float yaw ) {
        this.transform.rotateYaw( yaw );
        return (E) this;
    }

    /**
     * Rotates the entity's head around the yaw axis (vertical axis).
     *
     * @param headYaw The yaw value by which to rotate the entity's head
     */
    public E rotateHeadYaw( float headYaw ) {
        this.transform.rotateHeadYaw( headYaw );
        return (E) this;
    }

    /**
     * Rotates the entity's head around the pitch axis (transverse axis).
     *
     * @param pitch The pitch value by which to rotate the entity's head
     */
    public E rotatePitch( float pitch ) {
        this.transform.rotatePitch( pitch );
        return (E) this;
    }

    @Override
    public Chunk chunk() {
        int chunkX = CoordinateUtils.fromBlockToChunk( (int) this.positionX() );
        int chunkZ = CoordinateUtils.fromBlockToChunk( (int) this.positionZ() );

        return this.world.loadChunk( chunkX, chunkZ, true );
    }

    /**
     * Get the bounding box of the entity
     *
     * @return the current bounding box of this entity
     */
    @Override
    public AxisAlignedBB boundingBox() {
        return this.boundingBox;
    }

    /**
     * Change the size of the entity
     *
     * @param width  the new width of the entity
     * @param height the new height of the entity
     */
    protected E size(float width, float height ) {
        this.width = width;
        this.height = height;
        this.eyeHeight = (float) ( height / 2 + 0.1 );

        this.metadataContainer.putFloat( MetadataContainer.DATA_BOUNDINGBOX_WIDTH, width );
        this.metadataContainer.putFloat( MetadataContainer.DATA_BOUNDINGBOX_HEIGHT, height );

        this.recalcBoundingBox();
        return (E) this;
    }

    @Override
    public E collision(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.HAS_COLLISION, value );
        return (E) this;
    }

    @Override
    public boolean collision() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.HAS_COLLISION );
    }

    @Override
    public boolean immobile() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.IMMOBILE );
    }

    @Override
    public E immobile(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.IMMOBILE, value );
        return (E) this;
    }

    @Override
    public boolean affectedByGravity() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.AFFECTED_BY_GRAVITY );
    }

    @Override
    public E affectedByGravity(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.AFFECTED_BY_GRAVITY, value );
        return (E) this;
    }

    @Override
    public String nameTag() {
        return this.metadataContainer.getString( MetadataContainer.DATA_NAMETAG );
    }

    @Override
    public E nameTag(String nameTag ) {
        this.metadataContainer.putString( MetadataContainer.DATA_NAMETAG, nameTag );
        return (E) this;
    }

    @Override
    public boolean nameTagAlwaysVisible() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.ALWAYS_SHOW_NAMETAG );
    }

    @Override
    public E nameTagAlwaysVisible(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.ALWAYS_SHOW_NAMETAG, value );
        return (E) this;
    }

    @Override
    public boolean nameTagVisible() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.CAN_SHOW_NAMETAG );
    }

    @Override
    public E nameTagVisible(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.CAN_SHOW_NAMETAG, value );
        return (E) this;
    }

    @Override
    public boolean burning() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.ONFIRE );
    }

    @Override
    public E burning(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.ONFIRE, value );
        return (E) this;
    }

    @Override
    public boolean invisible() {
        return this.metadataContainer.getDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.INVISIBLE );
    }

    @Override
    public E invisible(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.INVISIBLE, value );
        return (E) this;
    }

    /**
     * Create the packet for display in the client
     *
     * @param receiver which should get this entity
     * @return packet for spawning this entity
     */
    public Packet createSpawnPacket( EntityPlayer receiver ) {
        PacketSpawnEntity spawnEntity = new PacketSpawnEntity();
        spawnEntity.setEntityId( this.id() );
        spawnEntity.setMetadata( this.metadataContainer );
        spawnEntity.setX( this.transform.positionX() );
        spawnEntity.setY( this.transform.positionY() + this.eyeHeight );
        spawnEntity.setZ( this.transform.positionZ() );
        spawnEntity.setEntityType( this.type() );
        spawnEntity.setVelocityX( this.transform.motionX() );
        spawnEntity.setVelocityY( this.transform.motionY() );
        spawnEntity.setVelocityZ( this.transform.motionZ() );
        spawnEntity.setYaw( this.transform.yaw() );
        spawnEntity.setHeadYaw( this.transform.headYaw() );
        spawnEntity.setPitch( this.transform.pitch() );
        return spawnEntity;
    }

    public void sendData( EntityPlayer player ) {
        PacketEntityMetadata metadataPacket = new PacketEntityMetadata();
        metadataPacket.setEntityId( this.id() );
        metadataPacket.setMetadata( this.metadataContainer );
        metadataPacket.setTick( this.world.currentTickTime() / (int) Values.CLIENT_TICK_MS );
        player.connection().addToSendQueue( metadataPacket );
    }

    @Override
    public boolean onGround() {
        return this.onGround;
    }

    public E canClimb(boolean value ) {
        this.metadataContainer.setDataFlag( MetadataContainer.DATA_INDEX, EntityFlag.CAN_CLIMB, value );
        return (E) this;
    }

    @Override
    public Vector velocity() {
        return this.transform.motion();
    }

    @Override
    public E velocity(Vector velocity ) {
        this.velocity( velocity, true );
        return (E) this;
    }

    protected boolean isOnLadder() {
        Location location = this.location();
        Block block = location.world().blockAt( location.toBlockPosition() );
        return block instanceof Ladder || block instanceof Vines;
    }

    public boolean isInsideLiquid() {
        Location eyeLocation = this.location().add( 0, this.eyeHeight, 0 );
        Block block = eyeLocation.world().blockAt( eyeLocation.toBlockPosition() );
        if ( block instanceof StationaryWater || block instanceof FlowingWater ) {
            float yLiquid = (float) ( block.position().y() + 1 + ( ( (Liquid<?>) block ).fillHeight() - 0.12 ) );
            return eyeLocation.y() < yLiquid;
        }

        return false;
    }

    /**
     * Decides if an entity can be attacked with an item (normal player attack)
     *
     * @return true if this entity can be attacked with an item, false if not
     */
    boolean canBeAttackedWithAnItem() {
        return true;
    }

    /**
     * Decides if this entity can get attacked by the given entity.
     *
     * @param entity which wants to attack
     * @return true when it can't be attacked from the entity given, false if it can
     */
    boolean isInvulnerableFrom( Entity<?> entity ) {
        return false;
    }

    /**
     * Deal damage to this entity
     *
     * @param damageEvent which holds all data needed for damaging this entity
     * @return true if the entity took damage, false if not
     */
    public boolean damage( EntityDamageEvent damageEvent ) {
        // Don't damage dead entities
        if ( this.dead ) {
            return false;
        }

        // First of all we call the event
        this.world.server().pluginManager().callEvent( damageEvent );
        return !damageEvent.cancelled();
    }

    @Override
    public E interact( io.gomint.entity.EntityPlayer player, Vector clickVector ) {
        return (E) this;
    }

    public void attach( EntityPlayer player ) {

    }

    public void detach( EntityPlayer player ) {

    }

    private void recalcBoundingBox() {
        // Only recalc on spawned entities
        if ( this.world == null ) {
            return;
        }

        // Update bounding box
        Location location = this.location();
        boundingBox().bounds(
            location.x() - ( this.width() / 2 ),
            location.y(),
            location.z() - ( this.width() / 2 ),
            location.x() + ( this.width() / 2 ),
            location.y() + this.height(),
            location.z() + ( this.width() / 2 )
        );
    }

    public void setAndRecalcPosition( Location to ) {
        position( to );
        pitch( to.pitch() );
        yaw( to.yaw() );
        headYaw( to.headYaw() );
        this.recalcBoundingBox();
    }

    void dealVoidDamage() {
        despawn();
    }

    public void onCollideWithPlayer( EntityPlayer player ) {

    }

    @Override
    public E resetFallDistance() {
        this.fallDistance = 0;
        return (E) this;
    }

    public void multiplyFallDistance( float v ) {
        this.fallDistance *= v;
    }

    @Override
    public E spawn( Location location ) {
        // Check if already spawned
        if ( this.world != null ) {
            throw new IllegalStateException( "Entity already spawned" );
        }
        Precondition.safeWorldAccess(location.world(), false);

        this.world = (WorldAdapter) location.world();
        this.world.spawnEntityAt( this, location.x(), location.y(), location.z(), location.yaw(), location.pitch() );
        this.setupAI();

        return (E) this;
    }

    protected void setupAI() {

    }

    public E teleport(Location to, EntityTeleportEvent.Cause cause) {
        Precondition.safeWorldAccess(this.world, false);
        EntityTeleportEvent entityTeleportEvent = new EntityTeleportEvent(this, this.location(), to, cause);
        this.world.server().pluginManager().callEvent(entityTeleportEvent);
        if (entityTeleportEvent.cancelled()) {
            return (E) this;
        }

        WorldAdapter currentWorld = this.world();


        if (!to.world().equals(currentWorld)) {
            currentWorld.removeEntity(this);
            WorldAdapter newWorld = (WorldAdapter) to.world();
            newWorld.syncScheduler().execute(() -> {
                this.world(newWorld);
                this.setAndRecalcPosition(to);
                newWorld.spawnEntityAt(this, to.x(), to.y(), to.z(), to.yaw(), to.pitch());
            });
        } else {
            this.setAndRecalcPosition(to);
        }

        this.fallDistance = 0;
        return (E) this;
    }

    @Override
    public E teleport( Location to ) {
        this.teleport( to, EntityTeleportEvent.Cause.CUSTOM );
        return (E) this;
    }

    @Override
    public E age(long duration, TimeUnit unit ) {
        this.age = MathUtils.fastFloor( unit.toMillis( duration ) / Values.CLIENT_TICK_MS );
        return (E) this;
    }

    @Override
    public BossBar bossBar() {
        return this.bossBar != null ? this.bossBar : ( this.bossBar = new io.gomint.server.entity.BossBar( this ) );
    }

    /**
     * Pre spawn packets when needed
     *
     * @param connection which is used to spawn this entity to
     */
    public void preSpawn( PlayerConnection connection ) {

    }

    /**
     * Post spawn packets when needed
     *
     * @param connection which is used to spawn this entity to
     */
    public void postSpawn( PlayerConnection connection ) {

    }

    @Override
    public float scale() {
        return this.metadataContainer.getFloat( MetadataContainer.DATA_SCALE );
    }

    @Override
    public E scale(float scale ) {
        this.metadataContainer.putFloat( MetadataContainer.DATA_SCALE, scale );
        return (E) this;
    }

    @Override
    public E hiddenByDefault(boolean value ) {
        if ( this instanceof EntityPlayer ) {
            return (E) this;
        }

        if ( !this.hideByDefault && value && this.world != null ) {
            // Despawn
            for ( io.gomint.entity.EntityPlayer entityPlayer : this.world.onlinePlayers() ) {
                EntityPlayer entityPlayer1 = (EntityPlayer) entityPlayer;
                entityPlayer1.entityVisibilityManager().removeEntity( this );
            }
        }

        this.shownFor = new HashSet<>();
        this.hideByDefault = value;
        return (E) this;
    }

    @Override
    public E showFor( io.gomint.entity.EntityPlayer player ) {
        ( (EntityPlayer) player ).entityVisibilityManager().addEntity( this );

        if ( this.shownFor != null ) {
            this.shownFor.add( (EntityPlayer) player );
        }

        return (E) this;
    }

    @Override
    public E hideFor( io.gomint.entity.EntityPlayer player ) {
        ( (EntityPlayer) player ).entityVisibilityManager().removeEntity( this );

        if ( this.shownFor != null ) {
            this.shownFor.remove( player );
        }

        return (E) this;
    }

    /**
     * Check if a player can see this entity
     *
     * @param player for which we check
     * @return true when visible, false when not
     */
    public boolean canSee( EntityPlayer player ) {
        return !this.hideByDefault || player.entityVisibilityManager().isVisible( this );
    }

    /**
     * Check if a player should be able to see this entity
     *
     * @param player for which we check
     * @return true when it should be visible, false when not
     */
    public boolean shouldBeSeen( EntityPlayer player ) {
        return !this.hideByDefault || this.shownFor.contains( player );
    }

    /**
     * Take over configuration from the given compound
     *
     * @param compound which holds entity data
     */
    public void initFromNBT( NBTTagCompound compound ) {
        // Read position
        List<Object> pos = compound.getList( "Pos", false );
        if ( pos != null ) {
            // Data in the array are three floats (x, y, z)
            float x = MathUtils.ensureFloat( pos.get( 0 ) );
            float y = MathUtils.ensureFloat( pos.get( 1 ) );
            float z = MathUtils.ensureFloat( pos.get( 2 ) );

            this.position( x, y, z );
        }

        // Now we read motion
        List<Object> motion = compound.getList( "Motion", false );
        if ( motion != null ) {
            // Data in the array are three floats (x, y, z)
            float x = MathUtils.ensureFloat( motion.get( 0 ) );
            float y = MathUtils.ensureFloat( motion.get( 1 ) );
            float z = MathUtils.ensureFloat( motion.get( 2 ) );

            this.transform.motion( x, y, z );
        }

        // Read rotation
        List<Object> rotation = compound.getList( "Rotation", false );
        if ( rotation != null ) {
            float yaw = (float) rotation.get( 0 );
            float pitch = (float) rotation.get( 1 );

            this.yaw( yaw );
            this.pitch( pitch );
        }

        // Fall distance
        this.fallDistance = compound.getFloat( "FallDistance", 0f );
        this.affectedByGravity( compound.getByte( "NoGravity", (byte) 0 ) == 0 );
        this.onGround = compound.getByte( "OnGround", (byte) 1 ) == 1;

        // Age is different for baby entities (those have negative age stored as integer)
        boolean isBaby = compound.getByte( "IsBaby", (byte) 0 ) == 1;
        if ( isBaby ) {
            this.age = compound.getInteger( "Age", -1500 );
        } else {
            this.age = compound.getShort( "Age", (short) 0 );
        }
    }

    public NBTTagCompound persistToNBT() {
        NBTTagCompound compound = new NBTTagCompound( "" );

        // Store ID
        compound.addValue( "identifier", this.type.getPersistantId() );

        // Store position
        List<Float> pos = new ArrayList<>();
        pos.add( this.positionX() );
        pos.add( this.positionY() );
        pos.add( this.positionZ() );
        compound.addValue( "Pos", pos );

        // Store motion
        List<Float> motion = new ArrayList<>();
        motion.add( this.getMotionX() );
        motion.add( this.getMotionY() );
        motion.add( this.getMotionZ() );
        compound.addValue( "Motion", motion );

        // Store rotation
        List<Float> rotation = new ArrayList<>();
        rotation.add( this.yaw() );
        rotation.add( this.pitch() );
        compound.addValue( "Rotation", rotation );

        // Fall distance
        compound.addValue( "FallDistance", this.fallDistance );
        compound.addValue( "NoGravity", (byte) ( this.affectedByGravity() ? 0 : 1 ) );
        compound.addValue( "OnGround", (byte) ( this.onGround() ? 1 : 0 ) );

        // Age
        if ( this.age < 0 ) {
            compound.addValue( "Age", this.age );
            compound.addValue( "IsBaby", (byte) 1 );
        } else {
            compound.addValue( "Age", (short) this.age );
            compound.addValue( "IsBaby", (byte) 0 );
        }

        return compound;
    }

    public boolean isMotionSendingEnabled() {
        return false;
    }

    public boolean needsFullMovement() {
        if ( this.nextFullMovement-- == 0 || this.oldPosition == null ||
            this.oldPosition.subtract( this.position() ).length() > 20 ) {
            this.nextFullMovement = 20;
            return true;
        }

        return false;
    }

    public void updateOldPosition() {
        this.oldPosition = this.location();
    }

    public Packet getMovementPacket() {
        PacketEntityMovement packetEntityMovement = new PacketEntityMovement();
        packetEntityMovement.setEntityId( this.id() );

        packetEntityMovement.setX( this.positionX() );
        packetEntityMovement.setY( this.positionY() + this.offsetY() );
        packetEntityMovement.setZ( this.positionZ() );

        packetEntityMovement.setYaw( this.yaw() );
        packetEntityMovement.setHeadYaw( this.headYaw() );
        packetEntityMovement.setPitch( this.pitch() );

        packetEntityMovement.setOnGround( this.onGround() );

        return packetEntityMovement;
    }

}
