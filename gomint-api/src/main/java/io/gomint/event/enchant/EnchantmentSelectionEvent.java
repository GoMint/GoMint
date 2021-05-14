package io.gomint.event.enchant;

import io.gomint.enchant.Enchantment;
import io.gomint.entity.EntityPlayer;
import io.gomint.event.player.SimplePlayerEvent;

import java.util.Collections;
import java.util.List;

/**
 * This event gets fired when a enchantment table needs to select new enchants (due to a player
 * putting a item in it for example). You can modify the list of enchants, this also alters the
 * output of the enchantment being applied later when the {@link ItemEnchantEvent} gets fired.
 *
 * @author geNAZt
 * @version 1.0
 * @stability 1
 */
public class EnchantmentSelectionEvent extends SimplePlayerEvent {

    public static class Option {

        private final List<Enchantment> enchantments;
        private int minimumRequirement;

        public Option(List<Enchantment> enchantments, int minimumRequirement) {
            this.enchantments = enchantments;
            this.minimumRequirement = minimumRequirement;
        }

        /**
         * Get the minimum requirement of levels one needs to unlock this enchantment
         *
         * @return minimum requirement of levels
         */
        public int minimumRequirement() {
            return this.minimumRequirement;
        }

        /**
         * Set the minimum requirement of levels for this enchantment unlock
         *
         * @param minimumRequirement needed to unlock this enchantments
         */
        public Option minimumRequirement(int minimumRequirement) {
            this.minimumRequirement = minimumRequirement;
            return this;
        }

        /**
         * The preselected list of enchantments. You can modify the enchantments
         * in the list itself (add, remove, clear etc.) but not set the list
         *
         * @return list of enchantments in this option
         */
        public List<Enchantment> enchantments() {
            return this.enchantments;
        }

    }

    private final List<Option> enchantmentOptions;

    public EnchantmentSelectionEvent(EntityPlayer player,
                                     List<Option> enchantmentOptions) {
        super(player);
        this.enchantmentOptions = enchantmentOptions;
    }

    /**
     * The preselected list of enchantments. This list is unmodifiable in itself since it needs to be
     * exactly the same length as the client displays options (currently three). You can modify the enchantments
     * in the option itself (.get(0).getEnchantments())
     *
     * @return list of enchantment options
     */
    public List<Option> enchantmentOptions() {
        return Collections.unmodifiableList(this.enchantmentOptions);
    }

    @Override
    public String toString() {
        return "EnchantmentSelectionEvent{" +
            "player=" + this.player() +
            ", enchantmentOptions=" + this.enchantmentOptions +
            '}';
    }

}
