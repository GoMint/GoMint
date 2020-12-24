/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.network.handler;

import io.gomint.entity.Entity;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.network.PlayerConnection;
import io.gomint.server.network.packet.PacketEntityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geNAZt
 * @version 1.0
 */
public class PacketEntityEventHandler implements PacketHandler<PacketEntityEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketEntityEventHandler.class);

    @Override
    public void handle(PacketEntityEvent packet, long currentTimeMillis, PlayerConnection connection) {
        LOGGER.debug("Got event id: {}", packet.getEntityId());
        switch (packet.getEventId()) {
            default:
                for (Entity entity : connection.getEntity().getAttachedEntities()) {
                    if (entity instanceof EntityPlayer) {
                        ((EntityPlayer) entity).getConnection().addToSendQueue(packet);
                    }
                }

                connection.addToSendQueue(packet);
        }
    }

}
