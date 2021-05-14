/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.testplugin.scoreboard;

import io.gomint.ChatColor;
import io.gomint.GoMint;
import io.gomint.entity.EntityPlayer;
import io.gomint.math.MathUtils;
import io.gomint.testplugin.TestPlugin;
import io.gomint.scoreboard.DisplayEntry;
import io.gomint.scoreboard.DisplaySlot;
import io.gomint.scoreboard.Scoreboard;
import io.gomint.scoreboard.ScoreboardDisplay;
import io.gomint.world.Chunk;

import java.text.NumberFormat;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt
 * @version 1.0
 */
public class DebugScoreboard {

    private final ScoreboardDisplay display;
    private final EntityPlayer player;

    private DisplayEntry tpsEntry;
    private DisplayEntry chunkEntry;
    private DisplayEntry worldTimeEntry;

    // Performance caches
    private final NumberFormat format;
    private double oldTPS = 0;
    private int chunkX;
    private int chunkZ;

    public DebugScoreboard( TestPlugin plugin, EntityPlayer player ) {
        this.player = player;

        Scoreboard scoreboard = GoMint.instance().createScoreboard();
        this.display = scoreboard.addDisplay( DisplaySlot.SIDEBAR, "debug", ChatColor.GREEN + "Go" + ChatColor.GRAY + "Mint" );

        this.display.addLine( " ", 0 );
        this.display.addLine( ChatColor.GOLD + "TPS     ", 1 );
        this.tpsEntry = this.display.addLine( ChatColor.RED + "0.00", 2 );

        this.display.addLine( "  ", 3 );
        this.chunkEntry = this.display.addLine( "0 / 0", 4 );

        this.display.addLine( "  ", 5 );
        this.worldTimeEntry = this.display.addLine( "00:00:00", 6 );

        // Performance things
        this.format = NumberFormat.getNumberInstance();
        this.format.setMaximumFractionDigits( 2 );
        this.format.setMinimumFractionDigits( 2 );

        // Schedule updated for this scoreboard
        plugin.scheduler().scheduleAsync( this::update, 50, 50, TimeUnit.MILLISECONDS );

        // Add player to scoreboard
        player.scoreboard( scoreboard );
    }

    private void update() {
        this.updateTPS();
        this.updateChunk();
        this.updateWorldTime();
    }

    private void updateWorldTime() {
        Duration time = this.player.world().time();

        int seconds = (int) time.getSeconds();
        int minutes = MathUtils.fastFloor(seconds / 60f);
        seconds -= minutes * 60;
        int hours = MathUtils.fastFloor(minutes / 60f);
        minutes -= hours * 60;

        StringBuilder timeString = new StringBuilder();
        if (hours < 10) {
            timeString.append("0");
        }

        timeString.append(hours).append(":");

        if (minutes < 10) {
            timeString.append("0");
        }

        timeString.append(minutes).append(":");

        if (seconds < 10) {
            timeString.append("0");
        }

        timeString.append(seconds);

        this.display.removeEntry( this.worldTimeEntry );
        this.worldTimeEntry = this.display.addLine( timeString.toString(), 6 );
    }

    private void updateChunk() {
        Chunk chunk = this.player.chunk();
        if ( chunk.x() != this.chunkX || chunk.z() != this.chunkZ ) {
            // Remove the old entry
            this.display.removeEntry( this.chunkEntry );
            this.chunkEntry = this.display.addLine( chunk.x() + " / " + chunk.z(), 4 );

            // Update cache
            this.chunkZ = chunk.z();
            this.chunkX = chunk.x();
        }
    }

    private void updateTPS() {
        double tps = this.player.world().tps();
        if ( this.oldTPS != tps) {
            // Remove the old entry
            this.display.removeEntry( this.tpsEntry );
            this.tpsEntry = this.display.addLine( ChatColor.GREEN + this.format.format( tps ), 2 );

            // Update cache
            this.oldTPS = tps;
        }
    }

}
