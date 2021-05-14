/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.scoreboard;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface DisplayEntry {

    /**
     * Set a new score for this entry
     *
     * @param score which should be used
     */
    DisplayEntry score(int score);

    /**
     * Get the score of this entry
     *
     * @return score of this entry
     */
    int score();

}
