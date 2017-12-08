/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.entity.projectile;

/**
 * @author geNAZt
 * @version 1.0
 */
public interface EntityArrow extends EntityProjectile {

    /**
     * Is this arrow fired with full force (critical)?
     *
     * @return true when dealing critical damage on impact, false when not
     */
    boolean isCritical();

}
