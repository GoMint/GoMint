/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.util;

import io.gomint.world.block.data.Direction;
import io.gomint.world.block.data.Facing;

/**
 * @author geNAZt
 * @version 1.0
 */
public enum Bearing {

    SOUTH( 0 ),
    WEST( 1 ),
    NORTH( 2 ),
    EAST( 3 );

    private final int direction;

    Bearing(int direction) {
        this.direction = direction;
    }

    public int getDirection() {
        return this.direction;
    }

    public Direction toDirection() {
        switch ( this ) {
            case SOUTH:
                return Direction.SOUTH;
            case NORTH:
                return Direction.NORTH;
            case EAST:
                return Direction.EAST;
            case WEST:
                return Direction.WEST;
        }

        return Direction.EAST;
    }

    public Facing toBlockFace() {
        switch ( this ) {
            case SOUTH:
                return Facing.SOUTH;
            case NORTH:
                return Facing.NORTH;
            case EAST:
                return Facing.EAST;
            case WEST:
                return Facing.WEST;
        }

        return Facing.EAST;
    }

    public Bearing opposite() {
        switch ( this ) {
            case SOUTH:
                return NORTH;
            case NORTH:
                return SOUTH;
            case EAST:
                return WEST;
            case WEST:
                return EAST;
        }

        return EAST;
    }

    /**
     * Get the correct bearing from the given angle
     *
     * @param angle which should be converted
     * @return bearing value
     */
    public static Bearing fromAngle( float angle ) {
        // Normalize angle
        angle += 180;// Why -90?
        angle %= 360;

        if ( angle < 0 ) {
            angle += 360.0;
        }

        // Check for south, west, north, east
        if ( ( 0 <= angle && angle < 45 ) || ( 315 <= angle && angle < 360 ) ) {
            return NORTH;
        }

        if ( 45 <= angle && angle < 135 ) {
            return EAST;
        }

        if ( 135 <= angle && angle < 225 ) {
            return SOUTH;
        }

        return WEST;
    }

}
