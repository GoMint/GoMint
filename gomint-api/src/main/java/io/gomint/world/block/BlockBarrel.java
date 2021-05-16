/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.world.block;

import io.gomint.world.block.data.Facing;

/**
 * @author jihuayu
 * @version 1.0
 */
public interface BlockBarrel extends Block {


    //TODO: docs
    Facing face();

    //TODO: docs
    BlockBarrel face(Facing value);

    //TODO: docs
    Boolean open();

    //TODO: docs
    BlockBarrel open(Boolean value);
}
