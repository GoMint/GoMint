/*
 * Copyright (c) 2020 Gomint Team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.block;

import com.google.common.collect.Lists;
import io.gomint.inventory.item.ItemFenceGate;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.server.world.block.state.BooleanBlockState;
import io.gomint.server.world.block.state.DirectionBlockState;
import io.gomint.world.block.BlockFenceGate;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.Direction;
import io.gomint.world.block.data.Facing;
import io.gomint.world.block.data.LogType;

import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:fence_gate", def = true)
@RegisterInfo(sId = "minecraft:spruce_fence_gate")
@RegisterInfo(sId = "minecraft:birch_fence_gate")
@RegisterInfo(sId = "minecraft:jungle_fence_gate")
@RegisterInfo(sId = "minecraft:dark_oak_fence_gate")
@RegisterInfo(sId = "minecraft:acacia_fence_gate")
@RegisterInfo(sId = "minecraft:warped_fence_gate")
@RegisterInfo(sId = "minecraft:crimson_fence_gate")
public class FenceGate extends Block implements BlockFenceGate {

    private final DirectionBlockState DIRECTION = new DirectionBlockState(() -> new String[]{"direction"});
    private final BooleanBlockState OPEN = new BooleanBlockState(() -> new String[]{"open_bit"});
    private final BooleanBlockState IN_WALL = new BooleanBlockState(() -> new String[]{"in_wall_bit"});

    @Override
    public long breakTime() {
        return 3000;
    }

    @Override
    public boolean transparent() {
        return true;
    }

    @Override
    public float blastResistance() {
        return 15.0f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.FENCE_GATE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public Class<? extends ItemStack<?>>[] toolInterfaces() {
        return ToolPresets.AXE;
    }

    @Override
    public BlockFenceGate toggle() {
        this.OPEN.state(this, !this.open());
        return this;
    }

    @Override
    public boolean open() {
        return this.OPEN.state(this);
    }

    @Override
    public LogType type() {
        switch (this.blockId()) {
            case "minecraft:fence_gate":
                return LogType.OAK;
            case "minecraft:spruce_fence_gate":
                return LogType.SPRUCE;
            case "minecraft:birch_fence_gate":
                return LogType.BIRCH;
            case "minecraft:jungle_fence_gate":
                return LogType.DARK_OAK;
            case "minecraft:dark_oak_fence_gate":
                return LogType.JUNGLE;
            case "minecraft:acacia_fence_gate":
                return LogType.ACACIA;
            case "minecraft:warped_fence_gate":
                return LogType.WARPED;
            case "minecraft:crimson_fence_gate":
                return LogType.CRIMSON;
        }

        return LogType.OAK;
    }

    @Override
    public BlockFenceGate type(LogType logType) {
        switch (logType) {
            case OAK:
                this.blockId("minecraft:fence_gate");
                break;
            case SPRUCE:
                this.blockId("minecraft:spruce_fence_gate");
                break;
            case BIRCH:
                this.blockId("minecraft:birch_fence_gate");
                break;
            case DARK_OAK:
                this.blockId("minecraft:dark_oak_fence_gate");
                break;
            case JUNGLE:
                this.blockId("minecraft:jungle_fence_gate");
                break;
            case ACACIA:
                this.blockId("minecraft:acacia_fence_gate");
                break;
            case WARPED:
                this.blockId("minecraft:warped_fence_gate");
                break;
            case CRIMSON:
                this.blockId("minecraft:crimson_fence_gate");
                break;
        }

        return this;
    }

    @Override
    public BlockFenceGate direction(Direction direction) {
        this.DIRECTION.state(this, direction);
        return this;
    }

    @Override
    public Direction direction() {
        return this.DIRECTION.state(this);
    }

    @Override
    public BlockFenceGate open(boolean open) {
        OPEN.state(this, open);
        return this;
    }

    @Override
    public BlockFenceGate inWall(boolean inWall) {
        IN_WALL.state(this, inWall);
        return this;
    }

    @Override
    public boolean inWall() {
        return IN_WALL.state(this);
    }

    @Override
    public boolean beforePlacement(EntityLiving<?> entity, ItemStack<?> item, Facing face, Location location, Vector clickVector) {
        DIRECTION.detectFromPlacement(this, entity, item, face, clickVector);
        OPEN.detectFromPlacement(this, entity, item, face, clickVector);
        IN_WALL.detectFromPlacement(this, entity, item, face, clickVector);
        return super.beforePlacement(entity, item, face, location, clickVector);
    }

    @Override
    public boolean interact(Entity<?> entity, Facing face, Vector facePos, ItemStack<?> item) {
        this.toggle();
        return true;
    }

    @Override
    public List<ItemStack<?>> drops(ItemStack<?> itemInHand) {
        ItemFenceGate item = ItemFenceGate.create(1).type(this.type());
        return Lists.newArrayList(item);
    }
}
