package io.gomint.server.world.block;

import com.google.common.collect.Lists;
import io.gomint.inventory.item.ItemBed;
import io.gomint.inventory.item.ItemStack;
import io.gomint.math.AxisAlignedBB;
import io.gomint.math.Location;
import io.gomint.math.Vector;
import io.gomint.server.entity.EntityLiving;
import io.gomint.server.entity.tileentity.BedTileEntity;
import io.gomint.server.entity.tileentity.TileEntity;
import io.gomint.server.registry.RegisterInfo;
import io.gomint.server.util.Bearing;
import io.gomint.server.world.block.state.BooleanBlockState;
import io.gomint.server.world.block.state.DirectionBlockState;
import io.gomint.taglib.NBTTagCompound;
import io.gomint.world.block.BlockBed;
import io.gomint.world.block.BlockType;
import io.gomint.world.block.data.BlockColor;
import io.gomint.world.block.data.Direction;
import io.gomint.world.block.data.Facing;

import java.util.Collections;
import java.util.List;

/**
 * @author geNAZt
 * @version 1.0
 */
@RegisterInfo(sId = "minecraft:bed")
public class Bed extends Block implements BlockBed {

    private static final DirectionBlockState DIRECTION = new DirectionBlockState(() -> new String[]{"direction"});
    private static final BooleanBlockState OCCUPIED = new BooleanBlockState(() -> new String[]{"occupied_bit"});
    private static final BooleanBlockState HEAD = new BooleanBlockState(() -> new String[]{"head_piece_bit"});

    @Override
    public String blockId() {
        return "minecraft:bed";
    }

    @Override
    public long breakTime() {
        return 300;
    }

    @Override
    public boolean transparent() {
        return true;
    }

    @Override
    public boolean canBeBrokenWithHand() {
        return true;
    }

    @Override
    public boolean onBreak(boolean creative) {
        Bed otherHalf = (Bed) this.otherHalf();
        if (otherHalf != null) {
            otherHalf.blockType(Air.class);
        }

        return true;
    }

    @Override
    public float blastResistance() {
        return 1.0f;
    }

    @Override
    public BlockType blockType() {
        return BlockType.BED;
    }

    private io.gomint.world.block.Block getOtherBlock() {
        // Select which side we need to check
        Direction facingToOtherHalf = DIRECTION.state(this);
        if (this.head()) {
            facingToOtherHalf = facingToOtherHalf.opposite();
        }

        return this.side(facingToOtherHalf);
    }

    @Override
    public BlockColor color() {
        BedTileEntity tileEntity = this.tileEntity();
        return tileEntity.getColor();
    }

    @Override
    public BlockBed color(BlockColor color) {
        BedTileEntity tileEntity = this.tileEntity();
        tileEntity.setColor(color);

        this.updateBlock();
        return this;
    }

    @Override
    public BlockBed otherHalf() {
        io.gomint.world.block.Block otherHalf = getOtherBlock();

        // Check if other part is a bed
        if (otherHalf != null && otherHalf.blockType() == BlockType.BED) {
            Bed otherBedHalf = (Bed) otherHalf;
            if (otherBedHalf.head() != this.head()) {
                return otherBedHalf;
            }
        }

        return null;
    }

    @Override
    public boolean head() {
        return HEAD.state(this);
    }

    @Override
    public BlockBed head(boolean value) {
        HEAD.state(this, value);
        return this;
    }

    @Override
    public Direction face() {
        return DIRECTION.state(this);
    }

    @Override
    public BlockBed face(Direction value) {
        DIRECTION.state(this,value);
        return this;
    }

    @Override
    public boolean beforePlacement(EntityLiving<?> entity, ItemStack<?> item, Facing face, Location location, Vector clickVector) {
        // We need to check if we are placed on a solid block
        Block block = (Block) location.world().blockAt(location.toBlockPosition()).side(Facing.DOWN);
        if (block.solid()) {
            Bearing bearing = Bearing.fromAngle(entity.yaw());

            // Check for other block
            Block other = block.side(bearing.toDirection());
            if (!other.solid()) {
                return false;
            }
            DIRECTION.state(this,bearing.toDirection());
            OCCUPIED.detectFromPlacement(this, entity, item, face, clickVector);
            Block replacingHead = other.side(Facing.UP);
            return replacingHead.canBeReplaced(item);
        }

        return false;
    }

    @Override
    public void afterPlacement() {
        Block otherBlock = (Block) this.getOtherBlock();
        if (otherBlock != null) {
            Bed bed = otherBlock.blockType(Bed.class);
            bed.color(this.color());
            bed.face(this.face());
            bed.head(true);
        }

        super.afterPlacement();
    }

    @Override
    public boolean needsTileEntity() {
        return true;
    }

    @Override
    TileEntity createTileEntity(NBTTagCompound compound) {
        super.createTileEntity(compound);
        return this.tileEntities.construct(BedTileEntity.class, compound, this, this.items);
    }

    @Override
    public List<ItemStack<?>> drops(ItemStack<?> itemInHand) {
        ItemBed bed = ItemBed.create(1);
        bed.color(((BedTileEntity) this.tileEntity()).getColor());
        return Lists.<ItemStack<?>>newArrayList(bed);
    }

    @Override
    public List<AxisAlignedBB> boundingBoxes() {
        return Collections.singletonList(new AxisAlignedBB(
            this.location.x(),
            this.location.y(),
            this.location.z(),
            this.location.x() + 1,
            this.location.y() + 0.5625f,
            this.location.z() + 1
        ));
    }

}
