/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.scoreboard;

/**
 * @author geNAZt
 * @version 1.0
 */
public class DisplayEntry implements io.gomint.scoreboard.DisplayEntry {

    private final Scoreboard scoreboard;
    private final long scoreId;

    public DisplayEntry(Scoreboard scoreboard, long scoreId) {
        this.scoreboard = scoreboard;
        this.scoreId = scoreId;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public long getScoreId() {
        return scoreId;
    }

    @Override
    public void setScore( int score ) {
        this.scoreboard.updateScore( this.scoreId, score );
    }

    @Override
    public int getScore() {
        return this.scoreboard.getScore( this.scoreId );
    }

}
