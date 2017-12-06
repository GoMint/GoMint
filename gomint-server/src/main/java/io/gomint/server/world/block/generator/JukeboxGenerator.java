/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.block.generator;

import io.gomint.math.Location;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.Jukebox;

/**
 * @author geNAZt
 * @version 1.0
 */
public class JukeboxGenerator implements BlockGenerator {

    @Override
    public Jukebox generate( byte blockData, byte skyLightLevel, byte blockLightLevel, TileEntity tileEntity, Location location ) {
        Jukebox block = generate();
        block.setData( blockData, tileEntity, (WorldAdapter) location.getWorld(), location, skyLightLevel, blockLightLevel );
        return block;
    }

    @Override
    public Jukebox generate() {
        return new Jukebox();
    }

}
