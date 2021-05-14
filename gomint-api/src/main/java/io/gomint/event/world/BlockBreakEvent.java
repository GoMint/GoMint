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

import java.util.List;

/**
 * This event gets called when a player has mined a block long enough for it to break
 *
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class BlockBreakEvent extends CancellablePlayerEvent<BlockBreakEvent> {

    private final Block breakBlock;
    private final List<ItemStack<?>> drops;

    /**
     * Create new block break event
     *
     * @param player     which breaks the block
     * @param breakBlock which should be broken
     * @param drops      which should be dropped
     */
    public BlockBreakEvent(EntityPlayer player, Block breakBlock, List<ItemStack<?>> drops) {
        super(player);
        this.breakBlock = breakBlock;
        this.drops = drops;
    }

    /**
     * Get the list of drops
     *
     * @return drops which can be manipulated
     */
    public List<ItemStack<?>> drops() {
        return this.drops;
    }

    /**
     * Get block which should be broken
     *
     * @return block which should be broken
     */
    public Block breakBlock() {
        return this.breakBlock;
    }

    @Override
    public String toString() {
        return "BlockBreakEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", breakBlock=" + this.breakBlock +
            ", drops=" + this.drops +
            '}';
    }

}
