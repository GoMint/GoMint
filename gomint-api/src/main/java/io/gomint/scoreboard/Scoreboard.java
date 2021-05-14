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
public interface Scoreboard {

    /**
     * Add a new display slot to this scoreboard
     *
     * @param slot          which should be shown
     * @param objectiveName which should be used
     * @param displayName   of the slot
     */
    ScoreboardDisplay addDisplay(DisplaySlot slot, String objectiveName, String displayName);

    /**
     * Add a new display slot to this scoreboard with a sort order
     *
     * @param slot          which should be shown
     * @param objectiveName which should be used
     * @param displayName   of the slot
     * @param sortOrder     which sorts the ids
     */
    ScoreboardDisplay addDisplay(DisplaySlot slot, String objectiveName, String displayName, SortOrder sortOrder);

    /**
     * Get a display from its slot
     *
     * @param slot for the display
     * @return the display or null when no display has been added to this slot
     */
    ScoreboardDisplay display(DisplaySlot slot);

    /**
     * Remove a display
     *
     * @param slot which should be removed
     */
    Scoreboard removeDisplay(DisplaySlot slot);

}
