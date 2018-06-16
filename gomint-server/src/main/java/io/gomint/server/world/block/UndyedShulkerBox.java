/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.block;

import io.gomint.inventory.item.*;
import io.gomint.math.Vector;
import io.gomint.server.entity.Entity;
import io.gomint.server.entity.tileentity.ShulkerBoxTileEntity;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.helper.ToolPresets;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockFace;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.BlockUndyedShulkerBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo( id = 205 )
public class UndyedShulkerBox extends Block implements BlockUndyedShulkerBox {

    private static final Logger LOGGER = LoggerFactory.getLogger( ShulkerBox.class );

    @Override
    public int getBlockId() {
        return 205;
    }

    @Override
    public float getBlastResistance() {
        return 30.0f;
    }

    @Override
    public Class<? extends ItemStack>[] getToolInterfaces() {
        return ToolPresets.PICKAXE;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public long getBreakTime() {
        return 9000;
    }

    @Override
    public boolean needsTileEntity() {
        return true;
    }

    @Override
    public boolean interact( Entity entity, BlockFace face, Vector facePos, ItemStack item ) {
        ShulkerBoxTileEntity tileEntity = this.getTileEntity();
        if ( tileEntity != null ) {
            tileEntity.interact( entity, face, facePos, item );
        } else {
            LOGGER.warn( "ShulkerBox @ {} has no tile entity. Generating new tile entity", this.location );
            tileEntity = (ShulkerBoxTileEntity) this.createTileEntity( new NBTTagCompound( "" ) );
            this.setTileEntity( tileEntity );
            this.world.storeTileEntity( this.location.toBlockPosition(), tileEntity );
            tileEntity.interact( entity, face, facePos, item );
        }

        return true;
    }

    @Override
    TileEntity createTileEntity( NBTTagCompound compound ) {
        super.createTileEntity( compound );

        // Add flags
        compound.addValue( "isUndyed", (byte) 1 );
        compound.addValue( "facing", (byte) 1 );

        return new ShulkerBoxTileEntity( compound, this.world );
    }

    @Override
    public BlockType getType() {
        return BlockType.UNDYED_SHULKER_BOX;
    }

}
