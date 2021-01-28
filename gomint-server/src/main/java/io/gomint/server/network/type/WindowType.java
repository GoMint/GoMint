package io.gomint.server.network.type;

/**
 * @author geNAZt
 * @version 1.0
 */
public enum WindowType {

    // CHECKSTYLE:OFF
    INVENTORY(-1),
    CONTAINER(0),
    WORKBENCH(1),
    FURNACE(2),
    ENCHANTMENT(3),
    BREWING_STAND(4),
    ANVIL(5),
    DISPENSER(6),
    DROPPER(7),
    HOPPER(8),
    CAULDRON(9),
    MINECART_CHEST(10),
    MINECART_HOPPER(11),
    HORSE(12),
    BEACON(13),
    STRUCTURE_EDITOR(14),
    TRADING(15),
    COMMAND_BLOCK(16),
    JUKEBOX(17),
    ARMOR(18),
    HAND(19),
    COMPOUND_CREATOR(20),
    ELEMENT_CONSTRUCTOR(21),
    MATERIAL_REDUCER(22),
    LAB_TABLE(23),
    LOOM(24),
    LECTERN(25),
    GRINDSTONE(26),
    BLAST_FURNACE(27),
    SMOKER(28),
    STONECUTTER(29),
    CARTOGRAPHY(30),
    HUD(31),
    JIGSAW_EDITOR(32),
    SMITHING_TABLE(33);

    private final byte id;

    WindowType(int id) {
        this.id = (byte) id;
    }

    public byte id() {
        return this.id;
    }

}
