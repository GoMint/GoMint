package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.server.world.block.state.BlockfaceFromPlayerBlockState;
import io.gomint.world.block.BlockType;

import io.gomint.server.entity.tileentity.SignTileEntity;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockWallSign;
import io.gomint.world.block.data.Facing;
import io.gomint.world.block.data.LogType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:wall_sign", def = true)
@RegisterInfo(sId = "minecraft:jungle_wall_sign")
@RegisterInfo(sId = "minecraft:acacia_wall_sign")
@RegisterInfo(sId = "minecraft:birch_wall_sign")
@RegisterInfo(sId = "minecraft:spruce_wall_sign")
@RegisterInfo(sId = "minecraft:darkoak_wall_sign")
public class WallSign extends Block implements BlockWallSign {

    @Getter
    private enum LogTypeMagic {
        OAK("minecraft:wall_sign"),
        SPRUCE("minecraft:spruce_wall_sign"),
        BIRCH("minecraft:birch_wall_sign"),
        JUNGLE("minecraft:jungle_wall_sign"),
        ACACIA("minecraft:acacia_wall_sign"),
        DARK_OAK("minecraft:darkoak_wall_sign");

        private final String blockId;

        LogTypeMagic(String blockId) {
            this.blockId = blockId;
        }
    }

    private final BlockfaceFromPlayerBlockState facing = new BlockfaceFromPlayerBlockState(this, () -> new String[]{"facing_direction"}, false);

    @Override
    public String getBlockId() {
        return "minecraft:wall_sign";
    }

    @Override
    public long getBreakTime() {
        return 1500;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean needsTileEntity() {
        return true;
    }

    @Override
    TileEntity createTileEntity(NBTTagCompound compound) {
        super.createTileEntity(compound);
        return new SignTileEntity(this);
    }

    @Override
    public List<String> getLines() {
        SignTileEntity sign = this.getTileEntity();
        if (sign == null) {
            return null;
        }

        return new ArrayList<>(sign.getLines());
    }

    @Override
    public void setLine(int line, String content) {
        // Silenty fail when line is incorrect
        if (line > 4 || line < 1) {
            return;
        }

        SignTileEntity sign = this.getTileEntity();
        if (sign == null) {
            return;
        }

        if (sign.getLines().size() < line) {
            for (int i = 0; i < line - sign.getLines().size(); i++) {
                sign.getLines().add("");
            }
        }

        sign.getLines().set(line - 1, content);
        this.updateBlock();
    }

    @Override
    public String getLine(int line) {
        // Silenty fail when line is incorrect
        if (line > 4 || line < 1) {
            return null;
        }

        SignTileEntity sign = this.getTileEntity();
        if (sign == null) {
            return null;
        }

        if (sign.getLines().size() < line) {
            return null;
        }

        return sign.getLines().get(line - 1);
    }

    @Override
    public LogType getWoodType() {
        for (LogTypeMagic value : LogTypeMagic.values()) {
            if (value.getBlockId().equals(this.getBlockId())) {
                return LogType.valueOf(value.name());
            }
        }

        return null;
    }

    @Override
    public void setWoodType(LogType logType) {
        LogTypeMagic newState = LogTypeMagic.valueOf(logType.name());
        this.setBlockId(newState.getBlockId());
    }

    @Override
    public float getBlastResistance() {
        return 5.0f;
    }

    @Override
    public BlockType getType() {
        return BlockType.WALL_SIGN;
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
    public void setFacing(Facing facing) {
        this.facing.setState(facing);
    }

    @Override
    public Facing getFacing() {
        return this.facing.getState();
    }

}
