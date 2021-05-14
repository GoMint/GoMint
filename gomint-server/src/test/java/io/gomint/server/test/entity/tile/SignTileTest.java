/*
 * Copyright (c) 2020 Gomint team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.test.entity.tile;

import io.gomint.server.entity.tileentity.SignTileEntity;
import io.gomint.server.test.IntegrationTest;
import io.gomint.server.test.WorldTestUtil;
import io.gomint.server.world.WorldAdapter;
import io.gomint.server.world.block.Air;
import io.gomint.server.world.block.WallSign;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.WorldType;
import io.gomint.world.generator.CreateOptions;
import io.gomint.world.generator.integrated.LayeredGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author geNAZt
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SignTileTest extends IntegrationTest {

    private WorldAdapter world;

    private WallSign getBlock() {
        return this.world
            .blockAt(1, 1, 1)
            .blockType(Air.class)
            .blockType(WallSign.class);
    }

    @Test
    @Order(1)
    public void generateNonExisting() {
        this.world = (WorldAdapter) this.server.createWorld("test", new CreateOptions()
            .worldType(WorldType.IN_MEMORY)
            .generator(LayeredGenerator.class));
        WorldTestUtil.blockUntilWorldRuns(this.world);
    }

    @Test
    @Order(2)
    public void throwOnTooMuchLines() {
        WorldTestUtil.runInWorldThread(this.world, this::throwOnTooMuchLines0);
    }

    private void throwOnTooMuchLines0() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SignTileEntity tileEntity = new SignTileEntity(this.getBlock(), null);

            NBTTagCompound compound = new NBTTagCompound("");
            compound.addValue("Text", "\n\n\n\n");
            tileEntity.applyClientData(null, compound);
        });
    }

    @Test
    @Order(3)
    public void accept3Lines() {
        WorldTestUtil.runInWorldThread(this.world, this::accept3Lines0);
    }

    private void accept3Lines0() throws Exception {
        SignTileEntity tileEntity = new SignTileEntity(this.getBlock(), null);

        NBTTagCompound compound = new NBTTagCompound("");
        compound.addValue("Text", "\n\n\n");
        tileEntity.applyClientData(null, compound);
    }

    @Test
    @Order(4)
    public void throwOnTooLongLine() {
        WorldTestUtil.runInWorldThread(this.world, this::throwOnTooLongLine0);
    }

    private void throwOnTooLongLine0() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            SignTileEntity tileEntity = new SignTileEntity(this.getBlock(), null);

            NBTTagCompound compound = new NBTTagCompound("");
            compound.addValue("Text", "hjkhjkgkhjgjgjhjhjk");
            tileEntity.applyClientData(null, compound);
        });
    }

}
