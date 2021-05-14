/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.test.world;

import io.gomint.server.test.IntegrationTest;
import io.gomint.server.test.WorldTestUtil;
import io.gomint.server.util.collection.FixedReadOnlyMap;
import io.gomint.server.world.BlockRuntimeIDs;
import io.gomint.server.world.block.Block;
import io.gomint.server.world.block.Log;
import io.gomint.world.World;
import io.gomint.world.WorldType;
import io.gomint.world.block.data.Axis;
import io.gomint.world.block.data.LogType;
import io.gomint.world.generator.CreateOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.HashMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldTest extends IntegrationTest {

    private World world;

    @Test
    @Order(1)
    public void generateNonExisting() {
        this.world = this.server.createWorld("test", new CreateOptions().worldType(WorldType.IN_MEMORY));
        WorldTestUtil.blockUntilWorldRuns(this.world);
    }

    @Test
    @Order(2)
    public void setLog() {
        WorldTestUtil.runInWorldThread(this.world, this::setLog0);
    }

    public void setLog0() {
        Block block = this.world.blockAt(50, 50, 50);
        Log log = block.blockType(Log.class);

        // Normal Y oak
        testLogState(log, Axis.Y, LogType.OAK, false, false,
            "minecraft:log", new HashMap<>() {{
                put("old_log_type", "oak");
                put("pillar_axis", "y");
            }});

        // Stripped X acacia
        testLogState(log, Axis.X, LogType.ACACIA, true, false,
            "minecraft:stripped_acacia_log", new HashMap<>() {{
                put("pillar_axis", "x");
            }});

        testLogState(log, Axis.Z, LogType.CRIMSON, true, false,
            "minecraft:stripped_crimson_stem", new HashMap<>() {{
                put("pillar_axis", "z");
            }});

        testLogState(log, Axis.Z, LogType.CRIMSON, true, true,
            "minecraft:stripped_crimson_hyphae", new HashMap<>() {{
                put("pillar_axis", "z");
            }});

        testLogState(log, Axis.Z, LogType.JUNGLE, true, true,
            "minecraft:wood", new HashMap<>() {{
                put("pillar_axis", "z");
                put("wood_type", "jungle");
                put("stripped_bit", (byte) 1);
            }});
    }

    private void testLogState(Log log, Axis axis, LogType logType, boolean stripped, boolean barkOnAllSides, String blockId, HashMap<String, Object> neededStates) {
        // Set the state on the block instance
        log.axis(axis);
        log.type(logType);
        log.stripped(stripped);
        log.barkOnAllSides(barkOnAllSides);
        int currentRuntimeId = log.runtimeId();

        int neededRuntimeId = BlockRuntimeIDs.toBlockIdentifier(blockId, new FixedReadOnlyMap(neededStates.entrySet())).runtimeId();

        Assertions.assertEquals(neededRuntimeId, currentRuntimeId, "Wanted and current runtime id differ");
    }

}
