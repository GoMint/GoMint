/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.inventory.item;

import io.gomint.inventory.item.ItemType;
import io.gomint.server.registry.RegisterInfo;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( sId = "minecraft:music_disc_chirp", id = 527 )
public class ItemMusicDiscChirp extends ItemStack implements io.gomint.inventory.item.ItemMusicDiscChirp {

    @Override
    public ItemType getItemType() {
        return ItemType.MUSIC_DISC_CHIRP;
    }

}
