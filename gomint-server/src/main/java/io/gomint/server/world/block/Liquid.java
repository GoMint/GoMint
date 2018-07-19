package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemAir;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.BlockPosition;
import io.gomint.math.Vector;
import io.gomint.server.entity.Entity;
import io.gomint.server.world.PlacementData;
import io.gomint.server.world.UpdateReason;
import io.gomint.world.Sound;
import io.gomint.world.block.BlockLiquid;
import io.gomint.world.block.BlockObsidian;
import io.gomint.world.block.data.Facing;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt
 * @version 1.0
 */
public abstract class Liquid extends Block implements BlockLiquid {

    private static Facing[] FACES_TO_CHECK = Facing.values();

    private enum FlowState {
        CAN_FLOW_DOWN,
        CAN_FLOW,
        BLOCKED
    }

    // Temporary storage for update
    private byte adjacentSources;
    private Map<BlockPosition, FlowState> flowCostVisited = new HashMap<>();

    @Override
    public float getFillHeight() {
        short data = this.getBlockData();
        if ( data >= 8 ) {
            data = 8;
        }

        if ( data == 0 ) {
            return 1f;
        }

        return ( ( data ) / 8f );
    }

    private byte getEffectiveFlowDecay( Block block ) {
        // Block changed type?
        if ( block.getType() != this.getType() ) {
            return -1;
        }

        // Get block data and cap by 8
        byte data = block.getBlockData();
        return data >= 8 ? 0 : data;
    }

    @Override
    public Vector getFlowVector() {
        // Create a new vector and get the capped flow decay of this block
        Vector vector = Vector.ZERO;
        byte decay = this.getEffectiveFlowDecay( this );

        // Check all 4 sides if we can flow into that
        for ( Facing face : Facing.values() ) {
            Block other = this.getSide( face );
            byte blockDecay = this.getEffectiveFlowDecay( other );

            // Do we have decay?
            if ( blockDecay < 0 ) {
                // We don't, check if we can flow replacing the block
                if ( !other.canBeFlowedInto() ) {
                    continue;
                }

                // Check if the block under this is a water block so we can connect those two
                blockDecay = this.getEffectiveFlowDecay( this.world.getBlockAt( other.getLocation().toBlockPosition().add( 0, -1, 0 ) ) );
                if ( blockDecay >= 0 ) {
                    byte realDecay = (byte) ( blockDecay - ( decay - 8 ) );
                   vector = vector.add( ( other.getLocation().getX() - this.getLocation().getX() ) * (float) realDecay,
                        ( other.getLocation().getY() - this.getLocation().getY() ) * (float) realDecay,
                        ( other.getLocation().getZ() - this.getLocation().getZ() ) * (float) realDecay );
                }
            } else {
                // Check if we need to update the other blocks decay
                byte realDecay = (byte) ( blockDecay - decay );
                vector = vector.add( ( other.getLocation().getX() - this.getLocation().getX() ) * (float) realDecay,
                    ( other.getLocation().getY() - this.getLocation().getY() ) * (float) realDecay,
                    ( other.getLocation().getZ() - this.getLocation().getZ() ) * (float) realDecay );
            }
        }

        if ( this.getBlockData() >= 8 ) {
            BlockPosition pos = this.getLocation().toBlockPosition();
            if ( !this.canFlowInto( this.world.getBlockAt( pos.add( 0, 0, -1 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( 0, 0, 1 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( -1, 0, 0 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( 1, 0, 0 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( 0, 1, -1 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( 0, 1, 1 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( -1, 1, 0 ) ) ) ||
                !this.canFlowInto( this.world.getBlockAt( pos.add( 1, 1, 0 ) ) ) ) {
                vector = vector.normalize().add( 0, -6, 0 );
            }
        }

        return vector.normalize();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public void stepOn( Entity entity ) {
        // Reset fall distance
        entity.resetFallDistance();
    }

    @Override
    public boolean canBeReplaced( ItemStack item ) {
        return true;
    }

    public abstract int getTickDiff();

    public abstract boolean isFlowing();

    @Override
    public PlacementData calculatePlacementData( Entity entity, ItemStack item, Vector clickVector ) {
        PlacementData data = super.calculatePlacementData( entity, item, clickVector );
        return data.setMetaData( (byte) 0 );
    }

    @Override
    public long update( UpdateReason updateReason, long currentTimeMS, float dT ) {
        if ( !isFlowing() ) {
            return -1;
        }

        if ( updateReason == UpdateReason.BLOCK_ADDED ||
            updateReason == UpdateReason.NEIGHBOUR_UPDATE ||
            updateReason == UpdateReason.RANDOM ) {
            if ( isUpdateScheduled() ) {
                return -1;
            }

            return currentTimeMS + getTickDiff(); // Water updates every 5 client ticks
        }

        BlockPosition pos = this.location.toBlockPosition();
        int decay = this.getFlowDecay( this );

        // Check for own decay updates
        this.checkOwnDecay( pos, decay );

        // Check for spread
        this.checkSpread( pos, decay );

        return currentTimeMS + getTickDiff(); // Water updates every 5 client ticks
    }

    private void checkSpread( BlockPosition pos, int decay ) {
        if ( decay >= 0 && pos.getY() > 0 ) {
            Block bottomBlock = this.world.getBlockAt( pos.add( BlockPosition.DOWN ) );
            this.flowIntoBlock( bottomBlock, decay | 0x08 );
            if ( decay == 0 || !bottomBlock.canBeFlowedInto() ) {
                int adjacentDecay;
                if ( decay >= 8 ) {
                    adjacentDecay = 1;
                } else {
                    adjacentDecay = decay + this.getFlowDecayPerBlock();
                }

                if ( adjacentDecay < 8 ) {
                    boolean[] flags = this.getOptimalFlowDirections();
                    if ( flags[0] ) {
                        this.flowIntoBlock( this.world.getBlockAt( pos.add( BlockPosition.WEST ) ), adjacentDecay );
                    }

                    if ( flags[1] ) {
                        this.flowIntoBlock( this.world.getBlockAt( pos.add( BlockPosition.EAST ) ), adjacentDecay );
                    }

                    if ( flags[2] ) {
                        this.flowIntoBlock( this.world.getBlockAt( pos.add( BlockPosition.NORTH ) ), adjacentDecay );
                    }

                    if ( flags[3] ) {
                        this.flowIntoBlock( this.world.getBlockAt( pos.add( BlockPosition.SOUTH ) ), adjacentDecay );
                    }
                }
            }

            this.checkForHarden();
        }
    }

    /**
     * Method to check for block hardening when two liquids collide
     */
    protected void checkForHarden() {
    }

    private void checkOwnDecay( BlockPosition pos, int decay ) {
        // Check for decay updates of own block
        if ( decay > 0 ) {
            byte smallestFlowDecay = -100;
            this.adjacentSources = 0;

            // Check for neighbour decay
            smallestFlowDecay = this.getSmallestFlowDecay( this.world.getBlockAt( pos.add( BlockPosition.NORTH ) ), smallestFlowDecay );
            smallestFlowDecay = this.getSmallestFlowDecay( this.world.getBlockAt( pos.add( BlockPosition.SOUTH ) ), smallestFlowDecay );
            smallestFlowDecay = this.getSmallestFlowDecay( this.world.getBlockAt( pos.add( BlockPosition.WEST ) ), smallestFlowDecay );
            smallestFlowDecay = this.getSmallestFlowDecay( this.world.getBlockAt( pos.add( BlockPosition.EAST ) ), smallestFlowDecay );

            int newDecay = smallestFlowDecay + this.getFlowDecayPerBlock();
            if ( newDecay >= 8 || smallestFlowDecay < 0 ) { // There is no neighbour?
                // Always stop flowing => decay to air
                newDecay = -1;
            }

            // Check if there is a block on top (flowing from top downwards)
            int topFlowDecay = this.getFlowDecay( this.world.getBlockAt( pos.add( BlockPosition.UP ) ) );
            if ( topFlowDecay >= 0 ) {
                newDecay = topFlowDecay | 0x08;
            }

            // Did we hit a bottom block and are surrounded by other source blocks? -> convert to source block
            if ( this.adjacentSources >= 2 && this instanceof FlowingWater ) {
                Block bottomBlock = this.world.getBlockAt( pos.add( BlockPosition.DOWN ) );
                if ( bottomBlock.isSolid() || ( bottomBlock instanceof FlowingWater && bottomBlock.getBlockData() == 0 ) ) {
                    newDecay = 0;
                }
            }

            // Do we need to update water decay value?
            if ( newDecay != decay ) {
                decay = newDecay;

                // Did we fully decay?
                boolean decayed = decay < 0;
                if ( decayed ) {
                    this.setType( Air.class );
                } else {
                    this.setBlockData( (byte) decay );
                    this.updateBlock();
                }
            }
        }
    }

    private short calculateFlowCost( BlockPosition pos, short accumulatedCost, short maxCost, Facing origin, Facing current ) {
        short cost = 1000;
        for ( Facing facing : FACES_TO_CHECK ) {
            if ( facing == current || facing == origin ) {
                continue;
            }

            Block other = this.world.getBlockAt( pos );
            Block sideBock = other.getSide( facing );
            BlockPosition checkingPos = sideBock.getLocation().toBlockPosition();

            if ( !this.flowCostVisited.containsKey( checkingPos ) ) {
                if ( !this.canFlowInto( sideBock ) ) {
                    this.flowCostVisited.put( checkingPos, FlowState.BLOCKED );
                } else if ( ( (Block) this.world.getBlockAt( checkingPos.add( BlockPosition.DOWN ) ) ).canBeFlowedInto() ) {
                    this.flowCostVisited.put( checkingPos, FlowState.CAN_FLOW_DOWN );
                } else {
                    this.flowCostVisited.put( checkingPos, FlowState.CAN_FLOW );
                }
            }

            FlowState status = this.flowCostVisited.get( checkingPos );
            if ( status == FlowState.BLOCKED ) {
                continue;
            } else if ( status == FlowState.CAN_FLOW_DOWN ) {
                return accumulatedCost;
            }

            if ( accumulatedCost >= maxCost ) {
                continue;
            }

            short realCost = this.calculateFlowCost( checkingPos, (short) ( accumulatedCost + 1 ), maxCost, origin, facing );
            if ( realCost < cost ) {
                cost = realCost;
            }
        }

        return cost;
    }

    private boolean[] getOptimalFlowDirections() {
        short[] flowCost = new short[]{
            1000,
            1000,
            1000,
            1000
        };

        short maxCost = (short) ( 4 / this.getFlowDecayPerBlock() );
        int j = 0;
        for ( Facing face : FACES_TO_CHECK ) {
            Block other = this.getSide( face );
            if ( !this.canFlowInto( other ) ) {
                this.flowCostVisited.put( other.getLocation().toBlockPosition(), FlowState.BLOCKED );
            } else if ( ( (Block) this.world.getBlockAt( this.getLocation().toBlockPosition().add( BlockPosition.DOWN ) ) ).canBeFlowedInto() ) {
                this.flowCostVisited.put( other.getLocation().toBlockPosition(), FlowState.CAN_FLOW_DOWN );
                flowCost[j] = maxCost = 0;
            } else if ( maxCost > 0 ) {
                BlockPosition pos = other.getLocation().toBlockPosition();
                this.flowCostVisited.put( pos, FlowState.CAN_FLOW );
                flowCost[j] = this.calculateFlowCost( pos, (short) 1, maxCost, face, face );
                maxCost = (short) Math.min( maxCost, flowCost[j] );
            }

            j++;
        }

        this.flowCostVisited.clear();
        double minCost = Double.MAX_VALUE;
        for ( int i = 0; i < 4; i++ ) {
            double d = flowCost[i];
            if ( d < minCost ) {
                minCost = d;
            }
        }

        boolean[] isOptimalFlowDirection = new boolean[4];
        for ( int i = 0; i < 4; ++i ) {
            isOptimalFlowDirection[i] = ( flowCost[i] == minCost );
        }

        return isOptimalFlowDirection;
    }

    /**
     * Let the block be replaced by the new liquid
     *
     * @param block which should be replaced with a liquid
     * @param newFlowDecay decay value of the liquid
     */
    protected void flowIntoBlock( Block block, int newFlowDecay ) {
        if ( this.canFlowInto( block ) && !( block instanceof Liquid ) ) {
            if ( block.getBlockId() > 0 ) {
                this.world.breakBlock( block.getLocation().toBlockPosition(), block.getDrops( ItemAir.create( 0 ) ), false );
            }

            PlacementData data = new PlacementData( this.getBlockId(), (byte) newFlowDecay, null );
            block.setBlockFromPlacementData( data );
        }
    }

    private byte getSmallestFlowDecay( Block block, byte decay ) {
        byte blockDecay = this.getFlowDecay( block );

        if ( blockDecay < 0 ) {
            return decay;
        } else if ( blockDecay == 0 ) {
            ++this.adjacentSources;
        } else if ( blockDecay >= 8 ) {
            blockDecay = 0;
        }

        return ( decay >= 0 && blockDecay >= decay ) ? decay : blockDecay;
    }

    protected byte getFlowDecayPerBlock() {
        return 1; // 1 for water, 2 for lava
    }

    private byte getFlowDecay( Block block ) {
        if ( block.getType() != this.getType() ) {
            return -1;
        }

        return block.getBlockData();
    }

    @Override
    public boolean canBeFlowedInto() {
        return true;
    }

    private boolean canFlowInto( Block block ) {
        return block.canBeFlowedInto() && !( block instanceof Liquid && block.getBlockData() == 0 );
    }

    @Override
    public Vector addVelocity( Entity entity, Vector pushedByBlocks ) {
        return pushedByBlocks.add( this.getFlowVector() );
    }

    /**
     * Called when two liquids collide
     *
     * @param colliding with which we collide
     * @param result of the collision
     */
    protected void liquidCollide( Block colliding, Class<? extends Block> result ) {
        this.setType( result );
        this.world.playSound( this.location.add( 0.5f, 0.5f, 0.5f ), Sound.FIZZ, (byte) 0 );
    }

}
