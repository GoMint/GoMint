/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.test.registry;

import io.gomint.inventory.item.ItemDoubleStoneSlab;
import io.gomint.inventory.item.ItemPrismarineDoubleSlab;
import io.gomint.inventory.item.ItemStack;
import io.gomint.server.test.IntegrationTest;
import io.gomint.server.test.WorldTestUtil;
import io.gomint.server.world.WorldAdapter;
import io.gomint.world.WorldType;
import io.gomint.world.block.BlockPrismarineSlab;
import io.gomint.world.block.data.PrismarineType;
import io.gomint.world.generator.CreateOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeperateStatesRegistry extends IntegrationTest {

    private WorldAdapter world;

    @Test
    @Order(1)
    public void generateNonExisting() {
        this.world = (WorldAdapter) this.server.createWorld("test", new CreateOptions().worldType(WorldType.IN_MEMORY));
        WorldTestUtil.blockUntilWorldRuns(this.world);
    }

    @Test
    @Order(2)
    public void testPrimarineSlabBlock() {
        this.world
            .blockAt(1,1,1)
            .blockType(BlockPrismarineSlab.class)
            .type(PrismarineType.DARK);

        Assertions.assertTrue(BlockPrismarineSlab.class.isAssignableFrom(this.world.blockAt(1,1,1).getClass()));
    }

    @Test
    @Order(3)
    public void testPrimarineSlabItem() {
        ItemStack<?> item = this.server.items().create("minecraft:real_double_stone_slab2", (short) 3, (byte) 1, null);
        Assertions.assertTrue(ItemPrismarineDoubleSlab.class.isAssignableFrom(item.getClass()));

        item = this.server.items().create("minecraft:real_double_stone_slab2", (short) 0, (byte) 1, null);
        Assertions.assertTrue(ItemDoubleStoneSlab.class.isAssignableFrom(item.getClass()));
    }

}
