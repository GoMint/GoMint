/*
 * Copyright (c) 2018 Gomint Team
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.server.world.block.state.BooleanBlockState;
import io.gomint.server.world.block.state.DirectionBlockState;
import io.gomint.world.block.BlockType;

import io.gomint.server.registry.RegisterInfo;
import io.gomint.world.block.data.Direction;
import io.gomint.world.block.data.Facing;
import io.gomint.world.block.data.LogType;

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
public class FenceGate extends Block implements io.gomint.world.block.BlockFenceGate {

    private final DirectionBlockState direction = new DirectionBlockState(this, () -> new String[]{"direction"});
    private final BooleanBlockState open = new BooleanBlockState(this, () -> new String[]{"open_bit"});
    private final BooleanBlockState inWall = new BooleanBlockState(this, () -> new String[]{"in_wall_bit"});

    @Override
    public String getBlockId() {
        return "minecraft:fence_gate";
    }

    @Override
    public long getBreakTime() {
        return 3000;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public float getBlastResistance() {
        return 15.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.FENCE_GATE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.AXE;
    }

    @Override
    public void toggle() {
        this.open.setState(!this.isOpen());
    }

    @Override
    public boolean isOpen() {
        return this.open.getState();
    }

    @Override
    public LogType getWoodType() {
        switch (this.getBlockId()) {
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
        }

        return LogType.OAK;
    }

    @Override
    public void setWoodType(LogType logType) {
        switch (logType) {
            case OAK:
                this.setBlockId("minecraft:fence_gate");
                break;
            case SPRUCE:
                this.setBlockId("minecraft:spruce_fence_gate");
                break;
            case BIRCH:
                this.setBlockId("minecraft:birch_fence_gate");
                break;
            case DARK_OAK:
                this.setBlockId("minecraft:dark_oak_fence_gate");
                break;
            case JUNGLE:
                this.setBlockId("minecraft:jungle_fence_gate");
                break;
            case ACACIA:
                this.setBlockId("minecraft:acacia_fence_gate");
                break;
        }
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction.setState(direction);
    }

    @Override
    public Direction getDirection() {
        return this.direction.getState();
    }

}
