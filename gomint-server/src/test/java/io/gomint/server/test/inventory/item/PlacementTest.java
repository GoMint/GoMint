/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.test.inventory.item;

import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityPlayer;
import io.gomint.server.test.IntegrationTest;
import io.gomint.server.test.WorldTestUtil;
import io.gomint.server.util.ClassPath;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.Block;
import io.gomint.world.WorldType;
import io.gomint.world.block.BlockAir;
import io.gomint.world.block.data.Facing;
import io.gomint.world.generator.CreateOptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PlacementTest extends IntegrationTest {

    private WorldAdapter world;

    @Test
    @Order(1)
    public void generateNonExisting() {
        this.world = (WorldAdapter) this.server.createWorld("test", new CreateOptions().worldType(WorldType.IN_MEMORY));
        WorldTestUtil.blockUntilWorldRuns(this.world);
    }

    @Test
    @Order(2)
    public void placeAll() {
        WorldTestUtil.runInWorldThread(this.world, this::placeAll0);
    }

    private void placeAll0() throws IOException {
        Block block = this.world.blockAt(50, 50, 50);
        Block downBlock = block.side(Facing.DOWN);

        EntityPlayer player = mock(EntityPlayer.class);
        when(player.world()).thenReturn(this.world);
        when(player.location()).thenReturn(new Location(this.world, 51, 50, 50, 10, 20));

        ClassPath classPath = new ClassPath("io.gomint");
        classPath.getTopLevelClasses("io.gomint.inventory.item", classInfo -> {
            Class<? extends ItemStack> clazz = classInfo.load();
            ItemStack<?> stack = this.server.createItemStack(clazz, 1);
            if (stack != null) {
                block.blockType(BlockAir.class);

                System.out.println("Testing stack: " + stack.getClass().getName());
                Block newBlock = (Block) ((io.gomint.server.inventory.item.ItemStack<?>) stack).block();
                if (newBlock != null) {
                    this.server.blocks().replaceWithItem(newBlock, player, downBlock, block, Facing.UP, stack, new Vector(0.5f, 0, 0.5f));
                }
            } else {
                System.out.println("Class " + clazz.getName() + " did not generate a item");
            }
        });
    }

}
