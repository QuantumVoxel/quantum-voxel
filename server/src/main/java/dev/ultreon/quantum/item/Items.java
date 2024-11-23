package dev.ultreon.quantum.item;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.item.Item.Properties;
import dev.ultreon.quantum.item.food.Foods;
import dev.ultreon.quantum.item.material.ItemMaterials;
import dev.ultreon.quantum.item.tool.AxeItem;
import dev.ultreon.quantum.item.tool.PickaxeItem;
import dev.ultreon.quantum.item.tool.ShovelItem;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.util.NamespaceID;

public class Items {
    public static final Item AIR = Items.register("air", new Item(new Properties()));
    public static final BlockItem GRASS_BLOCK = Items.register("grass_block", new BlockItem(new Properties(), () -> Blocks.GRASS_BLOCK));
    public static final BlockItem DIRT = Items.register("dirt", new BlockItem(new Properties(), () -> Blocks.DIRT));
    public static final BlockItem SAND = Items.register("sand", new BlockItem(new Properties(), () -> Blocks.SAND));
    public static final BlockItem GRAVEL = Items.register("gravel", new BlockItem(new Properties(), () -> Blocks.GRAVEL));
    public static final BlockItem STONE = Items.register("stone", new BlockItem(new Properties(), () -> Blocks.STONE));
    public static final BlockItem COBBLESTONE = Items.register("cobblestone", new BlockItem(new Properties(), () -> Blocks.COBBLESTONE));
    public static final BlockItem SANDSTONE = Items.register("sandstone", new BlockItem(new Properties(), () -> Blocks.SANDSTONE));
    public static final BlockItem WATER = Items.register("water", new BlockItem(new Properties(), () -> Blocks.WATER));
    public static final BlockItem LOG = Items.register("log", new BlockItem(new Properties(), () -> Blocks.LOG));
    public static final Item PLANK = Items.register("plank", new Item(new Properties()));
    public static final BlockItem PLANKS = Items.register("planks", new BlockItem(new Properties(), () -> Blocks.PLANKS));
    public static final BlockItem PLANKS_SLAB = Items.register("planks_slab", new BlockItem(new Properties(), () -> Blocks.PLANKS_SLAB));
    public static final BlockItem CRATE = Items.register("crate", new BlockItem(new Properties(), () -> Blocks.CRATE));
    public static final PickaxeItem WOODEN_PICKAXE = Items.register("wooden_pickaxe", new PickaxeItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final ShovelItem WOODEN_SHOVEL = Items.register("wooden_shovel", new ShovelItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final AxeItem WOODEN_AXE = Items.register("wooden_axe", new AxeItem(new Properties().stackSize(1), ItemMaterials.WOOD));
    public static final PickaxeItem STONE_PICKAXE = Items.register("stone_pickaxe", new PickaxeItem(new Properties().stackSize(1), ItemMaterials.STONE));
    public static final ShovelItem STONE_SHOVEL = Items.register("stone_shovel", new ShovelItem(new Properties().stackSize(1), ItemMaterials.STONE));
    public static final AxeItem STONE_AXE = Items.register("stone_axe", new AxeItem(new Properties().stackSize(1), ItemMaterials.STONE));
    public static final PickaxeItem IRON_PICKAXE = Items.register("iron_pickaxe", new PickaxeItem(new Properties().stackSize(1), ItemMaterials.IRON));
    public static final ShovelItem IRON_SHOVEL = Items.register("iron_shovel", new ShovelItem(new Properties().stackSize(1), ItemMaterials.IRON));
    public static final AxeItem IRON_AXE = Items.register("iron_axe", new AxeItem(new Properties().stackSize(1), ItemMaterials.IRON));
    public static final BlockItem CRAFTING_BENCH = Items.register("crafting_bench", new BlockItem(new Properties(), () -> Blocks.CRAFTING_BENCH));
    public static final Item STICK = Items.register("stick", new Item(new Properties()));
    public static final Item ROCK = Items.register("rock", new Item(new Properties()));
    public static final Item GRASS_FIBRE = Items.register("grass_fibre", new Item(new Properties()));
    public static final BlockItem CACTUS = Items.register("cactus", new BlockItem(new Properties(), () -> Blocks.CACTUS));
    public static final BlockItem BLAST_FURNACE = Items.register("blast_furnace", new BlockItem(new Properties(), () -> Blocks.BLAST_FURNACE));
    public static final BlockItem IRON_ORE = Items.register("iron_ore", new BlockItem(new Properties(), () -> Blocks.IRON_ORE));
    public static final Item IRON_INGOT = Items.register("iron_ingot", new Item(new Properties()));
    public static final Item RAW_IRON = Items.register("raw_iron", new Item(new Properties()));
    public static final Item RAW_BACON = Items.register("raw_bacon", new Item(new Properties().food(Foods.RAW_BACON)));
    public static final Item BACON = Items.register("bacon", new Item(new Properties().food(Foods.BACON)));
    public static final Item SNOW_BALL = Items.register("snow_ball", new Item(new Properties().stackSize(32)));
    public static final BlockItem LIGHT = Items.register("light", new BlockItem(new Properties(), () -> Blocks.LIGHT));

    private static <T extends Item> T register(String name, T block) {
        Registries.ITEM.register(new NamespaceID(name), block);
        return block;
    }

    public static void init() {

    }
}
