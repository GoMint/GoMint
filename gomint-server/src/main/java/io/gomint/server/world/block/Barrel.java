/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.world.block;

import io.gomint.inventory.item.ItemStack;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.tileentity.BarrelTileEntity;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.world.block.state.BlockfaceBlockState;
import io.gomint.server.world.block.state.BooleanBlockState;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockBarrel;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.Facing;

/**
 * @author geNAZt
 * @version 1.0
 */
// TODO: Proper impl
@RegisterInfo( sId = "minecraft:barrel" )
public class Barrel extends Block implements BlockBarrel {
    private static final BlockfaceBlockState DIRECTION = new BlockfaceBlockState(() -> new String[]{"direction"});
    private static final BooleanBlockState OPEN = new BooleanBlockState(() -> new String[]{"open_bit"});

    @Override
    public long breakTime() {
        return -1;
    }

    @Override
    public boolean onBreak( boolean creative ) {
        return creative;
    }

    @Override
    public float blastResistance() {
        return 1.8E7f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.BARREL;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return false;
    }

    @Override
    public boolean needsTileEntity() {
        return true;
    }

    @Override
    TileEntity createTileEntity(NBTTagCompound compound) {
        super.createTileEntity(compound);
        return this.tileEntities.construct(BarrelTileEntity.class, compound, this, this.items);
    }

    @Override
    public boolean beforePlacement(EntityLiving<?> entity, ItemStack<?> item, Facing face, Location location, Vector clickVector) {
        DIRECTION.detectFromPlacement(this, entity, item, face, clickVector);
        OPEN.detectFromPlacement(this, entity, item, face, clickVector);
        return super.beforePlacement(entity, item, face, location, clickVector);
    }

    @Override
    public Facing face() {
        return DIRECTION.state(this);
    }

    @Override
    public BlockBarrel face(Facing value) {
        DIRECTION.state(this,value);
        return this;
    }

    @Override
    public Boolean open() {
        return OPEN.state(this);
    }

    @Override
    public BlockBarrel open(Boolean value) {
        OPEN.state(this,value);
        return this;
    }
}
