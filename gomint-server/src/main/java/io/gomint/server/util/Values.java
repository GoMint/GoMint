package io.gomint.server.util;

import java.util.concurrent.TimeUnit;

/**
 * Random static values used all over the place for minecraft magic values
 *
 * @author geNAZt
 * @version 1.0
 */
public class Values {

    public static final float CLIENT_TICK_MS = 50f;
    public static final float CLIENT_TICK_RATE = TimeUnit.MILLISECONDS.toNanos( 50 ) / (float) TimeUnit.SECONDS.toNanos( 1 );

    // Day night cycle
    public static final float FULL_DAY_CYCLE = 24000f;
    public static final float CYCLE_TICKS_PER_SECOND = FULL_DAY_CYCLE / TimeUnit.HOURS.toSeconds(24);
    public static final float MAX_SYNC_DELAY = 10f * 20f; // 10 seconds (in ticks)
    public static final float TICKS_ON_ZERO = 18000f;
    public static final float SECONDS_ON_ZERO = TimeUnit.HOURS.toSeconds(6);

    // Inventory slot offsets
    public static final int CRAFTING_INPUT_SMALL_OFFSET = 28;
    public static final int CRAFTING_INPUT_OFFSET = 32;
    public static final int OUTPUT_OFFSET = 50;

    public static final int LATEST_CHUNK_VERSION = 21;

}
