package io.gomint.event.player;

import io.gomint.crafting.Recipe;
import io.gomint.entity.EntityPlayer;

/**
 * @author geNAZt
 * @version 1.0
 * @stability 3
 */
public class PlayerCraftingEvent extends CancellablePlayerEvent<PlayerCraftingEvent> {

    private final Recipe recipe;

    public PlayerCraftingEvent(EntityPlayer player, Recipe recipe) {
        super(player);
        this.recipe = recipe;
    }

    /**
     * The recipe the player crafted
     *
     * @return recipe which has been crafted
     */
    public Recipe recipe() {
        return this.recipe;
    }

    @Override
    public String toString() {
        return "PlayerCraftingEvent{" +
            "cancelled=" + this.cancelled() +
            ", player=" + this.player() +
            ", recipe=" + this.recipe +
            '}';
    }

}
