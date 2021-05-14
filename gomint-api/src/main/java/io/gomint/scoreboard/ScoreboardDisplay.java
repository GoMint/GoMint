/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.scoreboard;

import io.gomint.entity.Entity;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public interface ScoreboardDisplay {

    /**
     * Get the name of this display
     *
     * @return display name
     */
    String display();

    /**
     * Get the name of the objective
     *
     * @return objective name
     */
    String objective();

    /**
     * Get the order in which the ids should be sorted
     *
     * @return the order in which the ids should be sorted
     */
    SortOrder sortOrder();

    /**
     * Add a new entity to the display
     *
     * @param entity which should be displayed
     * @param score  which should be displayed with the entity
     */
    DisplayEntry addEntity(Entity<?> entity, int score);

    /**
     * Add a new line to the display
     *
     * @param line  which should be added to the display
     * @param score which should be given to the line
     */
    DisplayEntry addLine(String line, int score);

    /**
     * Remove an entry made with {@link #addEntity(Entity, int)} or {@link #addLine(String, int)}
     *
     * @param entry which should be removed
     */
    ScoreboardDisplay removeEntry(DisplayEntry entry);

}
