/*
 * Copyright (c) 2020, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.event.world;

import io.gomint.entity.EntityPlayer;
import io.gomint.event.player.CancellablePlayerEvent;
import io.gomint.inventory.item.ItemStack;
import io.gomint.world.block.Block;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class BlockPlaceEvent extends CancellablePlayerEvent<BlockPlaceEvent> {

    private final Block placedAgainst;
    private final Block shouldReplace;
    private final ItemStack<?> item;
    private final Block replacingBlock;

    /**
     * Created when a player wants to place a block
     *
     * @param player         which wants to place the block
     * @param placedAgainst  against which block did the player place
     * @param shouldReplace  block which should be replaced with the item
     * @param itemStack      which should replace above block
     * @param replacingBlock get the block which will replace the shouldReplace block
     */
    public BlockPlaceEvent(EntityPlayer player, Block placedAgainst, Block shouldReplace, ItemStack<?> itemStack, Block replacingBlock) {
        super(player);
        this.placedAgainst = placedAgainst;
        this.shouldReplace = shouldReplace;
        this.item = itemStack;
        this.replacingBlock = replacingBlock;
    }

    /**
     * Get the block which replaced the {@link #shouldReplace()} block after this event. This block
     * can NOT be modified (well you can but the server won't place the block with the modifications made in
     * this event)
     *
     * @return block which replaces the should replace block
     */
    public Block replacingBlock() {
        return this.replacingBlock;
    }

    /**
     * Get the block which the player placed against
     *
     * @return block which got placed against
     */
    public Block placedAgainst() {
        return this.placedAgainst;
    }

    /**
     * Get the block which should be replaced
     *
     * @return block which should be replaced
     */
    public Block shouldReplace() {
        return this.shouldReplace;
    }

    /**
     * Get the item which is going to be used to replace the block
     *
     * @return item which should replace the block
     */
    public ItemStack<?> item() {
        return this.item;
    }

    @Override
    public String toString() {
        return "BlockPlaceEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", placedAgainst=" + this.placedAgainst +
            ", shouldReplace=" + this.shouldReplace +
            ", item=" + this.item +
            ", replacingBlock=" + this.replacingBlock +
            '}';
    }

}
