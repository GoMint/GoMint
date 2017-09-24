package io.gomint.server.inventory;

import com.koloboke.collect.map.ObjIntMap;
import com.koloboke.collect.map.hash.HashObjIntMaps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author geNAZt
 * @version 1.0
 */
public class MaterialMagicNumbers {

    private static final ObjIntMap<String> NEW_ID_MAPPING = HashObjIntMaps.newMutableMap();

    static {
        // CHECKSTYLE:OFF
        register( 0, "minecraft:air" );
        register( 1, "minecraft:stone" );
        register( 2, "minecraft:grass_block" );
        register( 3, "minecraft:dirt" );
        register( 4, "minecraft:cobblestone" );
        register( 5, "minecraft:wood_planks" );
        register( 6, "minecraft:sapling" );
        register( 7, "minecraft:bedrock" );
        register( 8, "minecraft:flowing_water" );
        register( 9, "minecraft:water" );
        register( 10, "minecraft:lava" );
        register( 11, "minecraft:flowing_lava" );
        register( 12, "minecraft:sand" );
        register( 13, "minecraft:gravel" );
        register( 14, "minecraft:gold_ore" );
        register( 15, "minecraft:iron_ore" );
        register( 16, "minecraft:coal_ore" );
        register( 17, "minecraft:wood" );
        register( 18, "minecraft:leaves" );
        register( 19, "minecraft:sponge" );
        register( 20, "minecraft:glass" );
        register( 21, "minecraft:lapis_ore" );
        register( 22, "minecraft:lapis_block" );
        register( 23, "minecraft:dispenser" );
        register( 24, "minecraft:sandstone" );
        register( 25, "minecraft:noteblock" );
        register( 27, "minecraft:golden_rail" );
        register( 28, "minecraft:detector_rail" );
        register( 29, "minecraft:sticky_piston" );
        register( 30, "minecraft:web" );
        register( 31, "minecraft:tallgrass" );
        register( 32, "minecraft:deadbush" );
        register( 33, "minecraft:piston" );
        register( 34, "minecraft:piston_head" );
        register( 35, "minecraft:wool" );
        register( 37, "minecraft:yellow_flower" );
        register( 38, "minecraft:red_flower" );
        register( 39, "minecraft:brown_mushroom" );
        register( 40, "minecraft:red_mushroom" );
        register( 41, "minecraft:gold_block" );
        register( 42, "minecraft:iron_block" );
        register( 43, "minecraft:double_stone_slab" );
        register( 44, "minecraft:stone_slab" );
        register( 45, "minecraft:brick_block" );
        register( 46, "minecraft:tnt" );
        register( 47, "minecraft:bookshelf" );
        register( 48, "minecraft:mossy_cobblestone" );
        register( 49, "minecraft:obsidian" );
        register( 50, "minecraft:torch" );
        register( 51, "minecraft:fire" );
        register( 52, "minecraft:mob_spawner" );
        register( 53, "minecraft:oak_stairs" );
        register( 54, "minecraft:chest" );
        register( 55, "minecraft:redstone_wire" );
        register( 56, "minecraft:diamond_ore" );
        register( 57, "minecraft:diamond_block" );
        register( 58, "minecraft:crafting_table" );
        register( 59, "minecraft:wheat" );
        register( 60, "minecraft:farmland" );
        register( 61, "minecraft:furnace" );
        register( 62, "minecraft:lit_furnace" );
        register( 64, "minecraft:wooden_door" );
        register( 65, "minecraft:ladder" );
        register( 66, "minecraft:rail" );
        register( 67, "minecraft:cobblestone_stairs" );
        register( 69, "minecraft:lever" );
        register( 70, "minecraft:stone_pressure_plate" );
        register( 72, "minecraft:wooden_pressure_plate" );
        register( 73, "minecraft:redstone_ore" );
        register( 74, "minecraft:lit_redstone_ore" );
        register( 75, "minecraft:unlit_redstone_torch" );
        register( 76, "minecraft:redstone_torch" );
        register( 77, "minecraft:stone_button" );
        register( 78, "minecraft:snow_layer" );
        register( 79, "minecraft:ice" );
        register( 80, "minecraft:snow" );
        register( 81, "minecraft:cactus" );
        register( 82, "minecraft:clay" );
        register( 85, "minecraft:fence" );
        register( 86, "minecraft:pumpkin" );
        register( 87, "minecraft:netherrack" );
        register( 88, "minecraft:soul_sand" );
        register( 89, "minecraft:glowstone" );
        register( 90, "minecraft:portal" );
        register( 91, "minecraft:lit_pumpkin" );
        register( 93, "minecraft:unpowered_repeater" );
        register( 94, "minecraft:powered_repeater" );
        register( 95, "minecraft:stained_glass" );
        register( 96, "minecraft:trapdoor" );
        register( 97, "minecraft:monster_egg" );
        register( 98, "minecraft:stone_brick" );
        register( 99, "minecraft:brown_mushroom_block" );
        register( 100, "minecraft:red_mushroom_block" );
        register( 101, "minecraft:iron_bars" );
        register( 102, "minecraft:glass_pane" );
        register( 103, "minecraft:melon_block" );
        register( 104, "minecraft:pumpkin_stem" );
        register( 105, "minecraft:melon_stem" );
        register( 106, "minecraft:vines" );
        register( 107, "minecraft:fence_gate" );
        register( 108, "minecraft:brick_stairs" );
        register( 109, "minecraft:stone_brick_stairs" );
        register( 110, "minecraft:mycelium" );
        register( 111, "minecraft:lily_pad" );
        register( 112, "minecraft:nether_brick" );
        register( 113, "minecraft:nether_brick_fence" );
        register( 114, "minecraft:nether_brick_stairs" );
        register( 116, "minecraft:enchantment_table" );
        register( 119, "minecraft:end_portal" );
        register( 120, "minecraft:end_portal_frame" );
        register( 121, "minecraft:end_stone" );
        register( 122, "minecraft:dragon_egg" );
        register( 123, "minecraft:redstone_lamp" );
        register( 124, "minecraft:lit_redstone_lamp" );
        register( 125, "minecraft:dropper" );
        register( 126, "minecraft:activator_rail" );
        register( 127, "minecraft:cocoa" );
        register( 128, "minecraft:sandstone_stairs" );
        register( 129, "minecraft:emerald_ore" );
        register( 130, "minecraft:ender_chest" );
        register( 131, "minecraft:tripwire_hook" );
        register( 132, "minecraft:tripwire" );
        register( 133, "minecraft:block_of_emerald" );
        register( 134, "minecraft:spruce_wood_stairs" );
        register( 135, "minecraft:birch_wood_stairs" );
        register( 136, "minecraft:jungle_wood_stairs" );
        register( 138, "minecraft:beacon" );
        register( 139, "minecraft:cobblestone_wall" );
        register( 141, "minecraft:carrots" );
        register( 143, "minecraft:wooden_button" );
        register( 144, "minecraft:skull" );
        register( 145, "minecraft:anvil" );
        register( 146, "minecraft:trapped_chest" );
        register( 147, "minecraft:light_weighted_pressure_plate" );
        register( 148, "minecraft:heavy_weighted_pressure_plate" );
        register( 149, "minecraft:unpowered_comparator" );
        register( 150, "minecraft:powered_comparator" );
        register( 151, "minecraft:daylight_detector" );
        register( 152, "minecraft:redstone_block" );
        register( 153, "minecraft:quartz_ore" );
        register( 155, "minecraft:quartz_block" );
        register( 156, "minecraft:quartz_stairs" );
        register( 157, "minecraft:wooden_double_slab" );
        register( 158, "minecraft:wooden_slab" );
        register( 159, "minecraft:stained_hardened_clay" );
        register( 160, "minecraft:stained_glass_pane" );
        register( 161, "minecraft:acacia_leaves" );
        register( 162, "minecraft:acacia_wood" );
        register( 163, "minecraft:acacia_wood_stairs" );
        register( 164, "minecraft:dark_oak_wood_stairs" );
        register( 165, "minecraft:slime_block" );
        register( 167, "minecraft:iron_trapdoor" );
        register( 168, "minecraft:prismarine" );
        register( 169, "minecraft:sea_lantern" );
        register( 170, "minecraft:hay_bale" );
        register( 171, "minecraft:carpet" );
        register( 172, "minecraft:hardened_clay" );
        register( 173, "minecraft:block_of_coal" );
        register( 174, "minecraft:packed_ice" );
        register( 175, "minecraft:sunflower" );
        register( 178, "minecraft:inverted_daylight_sensor" );
        register( 179, "minecraft:red_sandstone" );
        register( 180, "minecraft:red_sandstone_stairs" );
        register( 181, "minecraft:double_red_sandstone_slab" );
        register( 182, "minecraft:red_sandstone_slab" );
        register( 183, "minecraft:spruce_fence_gate" );
        register( 184, "minecraft:birch_fence_gate" );
        register( 185, "minecraft:jungle_fence_gate" );
        register( 186, "minecraft:dark_oak_fence_gate" );
        register( 187, "minecraft:acacia_fence_gate" );
        register( 198, "minecraft:grass_path" );
        register( 200, "minecraft:chorus_flower" );
        register( 201, "minecraft:purpur_block" );
        register( 203, "minecraft:purpur_stairs" );
        register( 206, "minecraft:end_bricks" );
        register( 208, "minecraft:end_rod" );
        register( 209, "minecraft:end_gateway" );
        register( 240, "minecraft:chorus_plant" );
        register( 241, "minecraft:stained_glass" );
        register( 243, "minecraft:podzol" );
        register( 244, "minecraft:beetroots" );
        register( 245, "minecraft:stonecutter" );
        register( 246, "minecraft:glowing_obsidian" );
        register( 247, "minecraft:nether_reactor_core" );
        register( 250, "minecraft:block_moved_by_piston" );
        register( 251, "minecraft:observer" );
        register( 255, "minecraft:reserved6" );
        register( 256, "minecraft:iron_shovel" );
        register( 257, "minecraft:iron_pickaxe" );
        register( 258, "minecraft:iron_axe" );
        register( 259, "minecraft:flint_and_steel" ); // <-- Done till here
        register( 260, "minecraft:apple" );
        register( 261, "minecraft:bow" );
        register( 262, "minecraft:arrow" );
        register( 263, "minecraft:coal" );
        register( 264, "minecraft:diamond" );
        register( 265, "minecraft:iron_ingot" );
        register( 266, "minecraft:gold_ingot" );
        register( 267, "minecraft:iron_sword" );
        register( 268, "minecraft:wooden_sword" );
        register( 269, "minecraft:wooden_shovel" );
        register( 270, "minecraft:wooden_pickaxe" );
        register( 271, "minecraft:wooden_axe" );
        register( 272, "minecraft:stone_sword" );
        register( 273, "minecraft:stone_shovel" );
        register( 274, "minecraft:stone_pickaxe" );
        register( 275, "minecraft:stone_axe" );
        register( 276, "minecraft:diamond_sword" );
        register( 277, "minecraft:diamond_shovel" );
        register( 278, "minecraft:diamond_pickaxe" );
        register( 279, "minecraft:diamond_axe" );
        register( 280, "minecraft:stick" );
        register( 281, "minecraft:bowl" );
        register( 282, "minecraft:mushroom_stew" );
        register( 283, "minecraft:golden_sword" );
        register( 284, "minecraft:golden_shovel" );
        register( 285, "minecraft:golden_pickaxe" );
        register( 286, "minecraft:golden_axe" );
        register( 287, "minecraft:string" );
        register( 288, "minecraft:feather" );
        register( 289, "minecraft:gunpowder" );
        register( 290, "minecraft:wooden_hoe" );
        register( 291, "minecraft:stone_hoe" );
        register( 292, "minecraft:iron_hoe" );
        register( 293, "minecraft:diamond_hoe" );
        register( 294, "minecraft:golden_hoe" );
        register( 295, "minecraft:seeds" );
        register( 296, "minecraft:wheat" );
        register( 297, "minecraft:bread" );
        register( 298, "minecraft:leather_cap" );
        register( 299, "minecraft:leather_tunic" );
        register( 300, "minecraft:leather_pants" );
        register( 301, "minecraft:leather_boots" );
        register( 302, "minecraft:chain_helmet" );
        register( 303, "minecraft:chain_chestplate" );
        register( 304, "minecraft:chain_leggings" );
        register( 305, "minecraft:chain_boots" );
        register( 306, "minecraft:iron_helmet" );
        register( 307, "minecraft:iron_chestplate" );
        register( 308, "minecraft:iron_leggings" );
        register( 309, "minecraft:iron_boots" );
        register( 310, "minecraft:diamond_helmet" );
        register( 311, "minecraft:diamond_chestplate" );
        register( 312, "minecraft:diamond_leggings" );
        register( 313, "minecraft:diamond_boots" );
        register( 314, "minecraft:golden_helmet" );
        register( 315, "minecraft:golden_chestplate" );
        register( 316, "minecraft:golden_leggings" );
        register( 317, "minecraft:golden_boots" );
        register( 318, "minecraft:flint" );
        register( 319, "minecraft:raw_porkchop" );
        register( 320, "minecraft:cooked_porkchop" );
        register( 321, "minecraft:painting" );
        register( 322, "minecraft:golden_apple" );
        register( 323, "minecraft:sign" );
        register( 324, "minecraft:wooden_door" );
        register( 325, "minecraft:bucket" );
        register( 328, "minecraft:minecart" );
        register( 329, "minecraft:saddle" );
        register( 330, "minecraft:iron_door" );
        register( 331, "minecraft:redstone" );
        register( 332, "minecraft:snowball" );
        register( 333, "minecraft:boat" );
        register( 334, "minecraft:leather" );
        register( 336, "minecraft:brick" );
        register( 337, "minecraft:clay_ball" );
        register( 338, "minecraft:reeds" );
        register( 339, "minecraft:paper" );
        register( 340, "minecraft:book" );
        register( 341, "minecraft:slimeball" );
        register( 342, "minecraft:minecart_with_chest" );
        register( 344, "minecraft:egg" );
        register( 345, "minecraft:compass" );
        register( 346, "minecraft:fishing_rod" );
        register( 347, "minecraft:clock" );
        register( 348, "minecraft:glowstone_dust" );
        register( 349, "minecraft:raw_fish" );
        register( 350, "minecraft:cooked_fish" );
        register( 351, "minecraft:dye" );
        register( 352, "minecraft:bone" );
        register( 353, "minecraft:sugar" );
        register( 354, "minecraft:cake" );
        register( 355, "minecraft:bed" );
        register( 356, "minecraft:redstone_repeater" );
        register( 357, "minecraft:cookie" );
        register( 358, "minecraft:filled_map" );
        register( 359, "minecraft:shears" );
        register( 360, "minecraft:melon" );
        register( 361, "minecraft:pumpkin_seeds" );
        register( 362, "minecraft:melon_seeds" );
        register( 363, "minecraft:raw_beef" );
        register( 364, "minecraft:steak" );
        register( 365, "minecraft:raw_chicken" );
        register( 366, "minecraft:cooked_chicken" );
        register( 367, "minecraft:rotten_flesh" );
        register( 368, "minecraft:ender_pearl" );
        register( 369, "minecraft:blaze_rod" );
        register( 370, "minecraft:ghast_tear" );
        register( 371, "minecraft:gold_nugget" );
        register( 372, "minecraft:nether_wart" );
        register( 373, "minecraft:potion" );
        register( 374, "minecraft:glass_bottle" );
        register( 375, "minecraft:spider_eye" );
        register( 376, "minecraft:fermented_spider_eye" );
        register( 377, "minecraft:blaze_powder" );
        register( 378, "minecraft:magma_cream" );
        register( 379, "minecraft:brewing_stand" );
        register( 380, "minecraft:cauldron" );
        register( 381, "minecraft:eye_of_ender" );
        register( 382, "minecraft:glistering_melon" );
        register( 383, "minecraft:spawn_egg" );
        register( 384, "minecraft:experience_bottle" );
        register( 385, "minecraft:fire_charge" );
        register( 388, "minecraft:emerald" );
        register( 389, "minecraft:item_frame" );
        register( 390, "minecraft:flower_pot" );
        register( 391, "minecraft:carrot" );
        register( 392, "minecraft:potato" );
        register( 393, "minecraft:baked_potato" );
        register( 394, "minecraft:poisonous_potato" );
        register( 395, "minecraft:map" );
        register( 396, "minecraft:golden_carrot" );
        register( 397, "minecraft:mob_head" );
        register( 398, "minecraft:carrot_on_a_stick" );
        register( 399, "minecraft:nether_star" );
        register( 400, "minecraft:pumpkin_pie" );
        register( 403, "minecraft:enchanted_book" );
        register( 404, "minecraft:comparator" );
        register( 405, "minecraft:netherbrick" );
        register( 406, "minecraft:nether_quartz" );
        register( 407, "minecraft:minecart_with_tnt" );
        register( 408, "minecraft:minecart_with_hopper" );
        register( 409, "minecraft:prismarine_shard" );
        register( 410, "minecraft:hopper" );
        register( 411, "minecraft:raw_rabbit" );
        register( 412, "minecraft:cooked_rabbit" );
        register( 413, "minecraft:rabbit_stew" );
        register( 414, "minecraft:rabbit_foot" );
        register( 415, "minecraft:rabbit_hide" );
        register( 416, "minecraft:leather_horse_armor" );
        register( 417, "minecraft:iron_horse_armor" );
        register( 418, "minecraft:golden_horse_armor" );
        register( 419, "minecraft:diamond_horse_armor" );
        register( 420, "minecraft:lead" );
        register( 421, "minecraft:name_tag" );
        register( 422, "minecraft:prismarine_crystals" );
        register( 423, "minecraft:mutton" );
        register( 424, "minecraft:cooked_mutton" );
        register( 426, "minecraft:end_crystal" );
        register( 427, "minecraft:spruce_door" );
        register( 428, "minecraft:birch_door" );
        register( 429, "minecraft:jungle_door" );
        register( 430, "minecraft:acacia_door" );
        register( 431, "minecraft:dark_oak_door" );
        register( 432, "minecraft:chorus_fruit" );
        register( 433, "minecraft:popped_chorus_fruit" );
        register( 437, "minecraft:dragon_breath" );
        register( 438, "minecraft:splash_potion" );
        register( 441, "minecraft:lingering_potion" );
        register( 444, "minecraft:elytra" );
        register( 445, "minecraft:shulker_shell" );
        register( 457, "minecraft:beetroot" );
        register( 458, "minecraft:beetroot_seeds" );
        register( 459, "minecraft:beetroot_soup" );
        register( 460, "minecraft:raw_salmon" );
        register( 461, "minecraft:clownfish" );
        register( 462, "minecraft:pufferfish" );
        register( 463, "minecraft:cooked_salmon" );
        register( 466, "minecraft:enchanted_golden_apple" );
        // CHECKSTYLE:ON
    }

    public static void register( int id, String newId ) {
        NEW_ID_MAPPING.put( newId, id );

        // TODO: Remove this
        String[] split = newId.split( ":" );
        String mcName = split[1];
        StringBuilder className = new StringBuilder();
        boolean upperCase = true;

        for ( int i = 0; i < mcName.length(); i++ ) {
            char current = mcName.charAt( i );
            if ( upperCase ) {
                className.append( Character.toUpperCase( current ) );
                upperCase = false;
            } else if ( current == '_' ) {
                upperCase = true;
            } else {
                className.append( current );
            }
        }

        String simpleName = className.toString();
        String finalClassName = "Item" + className.toString();

        File implFile = new File( "gomint-server/src/main/java/io/gomint/server/inventory/item/" + finalClassName + ".java" );
        if ( !implFile.exists() ) {
            // API first
            String apiInterface = "package io.gomint.inventory.item;\n\n" +
                    "import io.gomint.GoMint;\n\n" +
                    "/**\n" +
                    " * @author geNAZt\n" +
                    " * @version 1.0\n" +
                    " */\n" +
                    "public interface Item" + simpleName + " extends ItemStack {\n\n" +
                    "    /**\n" +
                    "     * Create a new item stack with given class and amount\n" +
                    "     *\n" +
                    "     * @param amount which is used for the creation\n" +
                    "     */\n" +
                    "    static Item" + simpleName + " create( int amount ) {\n" +
                    "        return GoMint.instance().createItemStack( Item" + simpleName + ".class, amount );\n" +
                    "    }\n\n" +
                    "}";

            try {
                Files.write( Paths.get( "generated/api/Item" + simpleName + ".java" ), apiInterface.getBytes(), StandardOpenOption.CREATE );
            } catch ( IOException e ) {
                e.printStackTrace();
            }

            // Implementation
            String implementation = "package io.gomint.server.inventory.item;\n" +
                    "\n" +
                    "import io.gomint.server.registry.RegisterInfo;\n" +
                    "import io.gomint.taglib.NBTTagCompound;\n" +
                    "\n" +
                    "/**\n" +
                    " * @author geNAZt\n" +
                    " * @version 1.0\n" +
                    " */\n" +
                    "@RegisterInfo( id = " + id + " )\n " +
                    "public class Item" + simpleName + " extends ItemStack implements io.gomint.inventory.item.Item" + simpleName + " {\n" +
                    "\n" +
                    "    // CHECKSTYLE:OFF\n" +
                    "    public Item" + simpleName + "( short data, int amount ) {\n" +
                    "        super( " + id + ", data, amount );\n" +
                    "    }\n" +
                    "\n" +
                    "    public Item" + simpleName + "( short data, int amount, NBTTagCompound nbt ) {\n" +
                    "        super( " + id + ", data, amount, nbt );\n" +
                    "    }\n" +
                    "    // CHECKSTYLE:ON\n" +
                    "\n" +
                    "}\n";

            try {
                Files.write( Paths.get( "generated/impl/Item" + simpleName + ".java" ), implementation.getBytes(), StandardOpenOption.CREATE );
            } catch ( IOException e ) {
                e.printStackTrace();
            }

            System.out.println( finalClassName );
        }
    }

    public static int valueOfWithId( String newId ) {
        return NEW_ID_MAPPING.getOrDefault( newId, 0 );
    }

}
