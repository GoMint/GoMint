/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.network.packet;

import io.gomint.jraknet.PacketBuffer;
import io.gomint.server.network.Protocol;

/**
 * @author BlackyPaw
 * @author geNAZt
 * @version 1.0
 */
public class PacketEntityMovement extends Packet {

    private static final byte FLAG_ON_GROUND = 0x1;
    private static final byte FLAG_TELEPORTED = 0x2;
    private static final byte FLAG_FORCE_MOVE_LOCAL_ENTITY = 0x4;

    private long entityId;
    private float x;
    private float y;
    private float z;
    private float yaw;
    private float headYaw;
    private float pitch;
    private boolean onGround;
    private boolean teleported;

    public PacketEntityMovement() {
        super( Protocol.PACKET_ENTITY_MOVEMENT );
    }

    @Override
    public void serialize( PacketBuffer buffer, int protocolID ) {
        buffer.writeUnsignedVarLong( this.entityId );

        byte flags = this.onGround ? FLAG_ON_GROUND : 0;
        if ( this.teleported ) {
            flags |= FLAG_TELEPORTED;
        }

        buffer.writeByte( flags );

        buffer.writeLFloat( this.x );
        buffer.writeLFloat( this.y );
        buffer.writeLFloat( this.z );

        writeByteRotation( this.pitch, buffer );
        writeByteRotation( this.headYaw, buffer );
        writeByteRotation( this.yaw, buffer );
    }

    @Override
    public void deserialize( PacketBuffer buffer, int protocolID ) {

    }

    public long getEntityId() {
        return this.entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return this.y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return this.z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getHeadYaw() {
        return this.headYaw;
    }

    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isTeleported() {
        return this.teleported;
    }

    public void setTeleported(boolean teleported) {
        this.teleported = teleported;
    }
}
