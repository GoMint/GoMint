package io.gomint.server.entity.ai;

import io.gomint.math.Vector;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.pathfinding.PathfindingEngine;
import io.gomint.server.util.IntTriple;
import io.gomint.server.world.WorldAdapter;
import io.gomint.util.Numbers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
public class AIFollowEntity extends AIState {

    private static final Logger LOGGER = LoggerFactory.getLogger( AIFollowEntity.class );

    private final WorldAdapter world;
    private final PathfindingEngine pathfinding;

    private int currentPathNode;
    private List<IntTriple> path;

    private Entity followEntity;

    /**
     * Constructs a new AIState that will belong to the given state machine.
     *
     * @param machine     The state machine the AIState being constructed belongs to
     * @param world       The world the parent entity lives in
     * @param pathfinding The pathfinding engine this entity is using
     */
    public AIFollowEntity( AIStateMachine machine, WorldAdapter world, PathfindingEngine pathfinding ) {
        super( machine );
        this.world = world;
        this.pathfinding = pathfinding;
    }

    /**
     * Set a new follow entity target
     *
     * @param entity the new entity to follow
     */
    public void setFollowEntity( Entity entity ) {
        this.followEntity = entity;
    }

    @Override
    protected void update( long currentTimeMS, float dT ) {
        // No target to follow
        if ( this.followEntity == null ) {
            return;
        }

        if ( this.path != null && this.currentPathNode < this.path.size() ) {
            Vector position = this.pathfinding.getTransform().getPosition();

            IntTriple blockPosition = new IntTriple(
                    Numbers.fastFloor( position.getX() ),
                    Numbers.fastFloor( position.getY() ),
                    Numbers.fastFloor( position.getZ() )
            );

            IntTriple node = this.path.get( this.currentPathNode );

            // Check if we need to jump
            boolean jump = node.getY() > position.getY();

            Vector direction = node.toVector().add( .5f, 0, .5f ).subtract( position ).normalize().multiply( 4.31f * dT ); // 4.31 is the normal player movement speed per second
            if ( jump ) {
                direction.setY( 1f ); // Default jump height
            }

            this.pathfinding.getTransform().setMotion( direction.getX(), direction.getY(), direction.getZ() );

            LOGGER.debug( "Current pos: " + position + "; Needed: " + node + "; Direction: " + direction );

            if ( blockPosition.equals( node ) ) {
                this.currentPathNode++;
            }
        } else if ( this.followEntity.isOnGround() ){
            LOGGER.debug( "Current follow position: " + this.followEntity.getLocation() );

            this.pathfinding.setGoal( this.followEntity.getLocation() );
            this.path = this.pathfinding.getPath();
            this.currentPathNode = 0;

            LOGGER.debug( "Path to follow entity " + this.followEntity );
            for ( IntTriple intTriple : this.path ) {
                /*EntityItem item = new EntityItem( new ItemStack( Material.BRICK ), this.world );
                this.world.spawnEntityAt( item, intTriple.toVector() );

                LOGGER.debug( intTriple.toString() );*/
            }
        }
    }

}
