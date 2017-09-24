/*
 * Copyright (c) 2017, GoMint, BlackyPaw and geNAZt
 *
 * This code is licensed under the BSD license found in the
 * LICENSE file in the root directory of this source tree.
 */

package io.gomint.server.crafting;

import io.gomint.inventory.item.ItemStack;
import io.gomint.jraknet.PacketBuffer;

import java.util.Collection;
import java.util.UUID;

/**
 * A recipe of some type that may be used to create a new item given some other
 * ingredients.
 *
 * @author BlackyPaw
 * @version 1.0
 */
public abstract class Recipe {

    private final UUID uuid;

    /**
     * Construct new recipe
     *
     * @param uuid of the recipe
     */
    protected Recipe( UUID uuid ) {
        this.uuid = ( uuid != null ? uuid : UUID.randomUUID() );
    }

    /**
     * Gets the UUID of this recipe.
     *
     * @return The UUID of this recipe
     */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Returns a list of ingredients required by this recipe.
     *
     * @return The list of ingredients required by this recipe
     */
    public abstract Collection<ItemStack> getIngredients();

    /**
     * Creates a collections of items stacks which represent the
     * result of the recipe. Usually a recipe has only got one
     * result item stack yet there are recipes such as the one
     * for cake which do have more than one result. Such recipes
     * might return multiple item stacks. Item stacks returned by
     * this method are entirely new instances and mays be modified
     * without prior cloning.
     *
     * @return The newly created resulting item stacks
     */
    public abstract Collection<ItemStack> createResult();

    /**
     * Serializes the recipe into the given packet buffer.
     *
     * @param buffer       The buffer to serialize the recipe into
     */
    public abstract void serialize( PacketBuffer buffer );

}
