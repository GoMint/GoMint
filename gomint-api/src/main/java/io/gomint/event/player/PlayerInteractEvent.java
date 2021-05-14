package io.gomint.event.player;

import io.gomint.entity.EntityPlayer;
import io.gomint.world.block.Block;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerInteractEvent extends CancellablePlayerEvent<PlayerInteractEvent> {

    private final ClickType clickType;
    private final Block block;

    /**
     * Create a new interaction event
     *
     * @param player    which interacted with something
     * @param clickType which has been used
     * @param block     on which the player interacted
     */
    public PlayerInteractEvent(EntityPlayer player, ClickType clickType, Block block) {
        super(player);
        this.clickType = clickType;
        this.block = block;
    }

    /**
     * Get the click type
     *
     * @return enum value of the click type
     */
    public ClickType clickType() {
        return this.clickType;
    }

    /**
     * Get the block with which the player interacted. This may be null
     *
     * @return block which the player interacted with
     */
    public Block block() {
        return this.block;
    }

    @Override
    public String toString() {
        return "PlayerInteractEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", clickType=" + this.clickType +
            ", block=" + this.block +
            '}';
    }

    public enum ClickType {
        /**
         * Right click
         */
        RIGHT,

        /**
         * Left click
         */
        LEFT
    }

}
