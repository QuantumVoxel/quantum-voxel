package dev.ultreon.quantum.block;

import dev.ultreon.quantum.block.Block.Properties;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.item.tool.ToolType;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.sound.SoundType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.loot.RandomLoot;
import org.apache.commons.lang3.IntegerRange;


/**
 * The registry for all blocks in the game.
 *
 * @see Block
 * @see Registries
 */
public final class Blocks {
    public static final Block AIR = Blocks.register("air", new Block(new Properties().replaceable().noCollision().noRendering().transparent()));
    public static final Block CAVE_AIR = Blocks.register("cave_air", new Block(new Properties().replaceable().noCollision().noRendering().transparent()));
    public static final Block VOID_AIR = Blocks.register("void_air", new Block(new Properties().noRendering().transparent()));
    public static final Block BARRIER = Blocks.register("barrier", new Block(new Properties().soundType(SoundType.STONE).transparent().noRendering()));
    public static final Block ERROR = Blocks.register("error", new Block(new Properties().soundType(SoundType.STONE)));
    public static final GrassBlock GRASS_BLOCK = Blocks.register("grass_block", new GrassBlock(new Properties().hardness(3F).soundType(SoundType.GRASS).effectiveTool(ToolType.SHOVEL).dropsItems(Items.DIRT).doRandomTick()));
    public static final Block DIRT = Blocks.register("dirt", new Block(new Properties().hardness(3F).soundType(SoundType.GRASS).effectiveTool(ToolType.SHOVEL).dropsItems(Items.DIRT)));
    public static final Block VOIDGUARD = Blocks.register("voidguard", new Block(new Properties().unbreakable().soundType(SoundType.STONE).effectiveTool(ToolType.PICKAXE).dropsItems(new ItemStack[0])));
    public static final Block SAND = Blocks.register("sand", new Block(new Properties().hardness(2.5F).soundType(SoundType.SAND).effectiveTool(ToolType.SHOVEL).dropsItems(Items.SAND)));
    public static final Block GRAVEL = Blocks.register("gravel", new Block(new Properties().hardness(2.7F).soundType(SoundType.SAND).effectiveTool(ToolType.SHOVEL).dropsItems(Items.GRAVEL)));
    public static final Block STONE = Blocks.register("stone", new Block(new Properties().hardness(12.0F).soundType(SoundType.STONE).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(new RandomLoot(new RandomLoot.CountLootEntry(IntegerRange.of(2, 4), Items.ROCK)))));
    public static final Block COBBLESTONE = Blocks.register("cobblestone", new Block(new Properties().hardness(11.0F).soundType(SoundType.STONE).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(Items.COBBLESTONE)));
    public static final Block SANDSTONE = Blocks.register("sandstone", new Block(new Properties().hardness(8.0F).soundType(SoundType.STONE).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(Items.SANDSTONE)));
    public static final Block WATER = Blocks.register("water", new Block(new Properties().noCollision().noOcclude().transparent().fluid().replaceable().lightReduction(3).hardness(120.0F)));
    public static final Block LOG = Blocks.register("log", new Block(new Properties().hardness(2.0F).soundType(SoundType.WOOD).effectiveTool(ToolType.AXE).dropsItems(Items.LOG)));
    public static final Block PLANKS = Blocks.register("planks", new Block(new Properties().hardness(2.0F).soundType(SoundType.WOOD).effectiveTool(ToolType.AXE).dropsItems(Items.PLANKS)));
    public static final Block PLANKS_SLAB = Blocks.register("planks_slab", new SlabBlock(new Properties().hardness(2.0F).soundType(SoundType.WOOD).effectiveTool(ToolType.AXE).dropsItems(Items.PLANKS)));
    public static final LeavesBlock LEAVES = Blocks.register("leaves", new LeavesBlock(new Properties().transparent().hardness(0.2F).soundType(SoundType.GRASS).noCollision().lightReduction(2)));
    public static final Block CRATE = Blocks.register("crate", new CrateBlock(new Properties().hardness(2.0F).soundType(SoundType.WOOD).effectiveTool(ToolType.AXE).usesCustomRender().dropsItems(Items.CRATE)));
    public static final WorkbenchBlock CRAFTING_BENCH = Blocks.register("crafting_bench", new WorkbenchBlock(new Properties().hardness(3.0F).soundType(SoundType.WOOD).effectiveTool(ToolType.AXE).dropsItems(Items.CRAFTING_BENCH)));
    public static final Block SHORT_GRASS = Blocks.register("short_grass", new Block(new Properties().noOcclude().replaceable().transparent().noCollision().usesCustomRender().dropsItems(new RandomLoot(new RandomLoot.ChanceLootEntry(0.4f, Items.GRASS_FIBRE)))));
    public static final Block SNOWY_SHORT_GRASS = Blocks.register("snowy_short_grass", new Block(new Properties().noOcclude().replaceable().transparent().noCollision().usesCustomRender().dropsItems(new RandomLoot(new RandomLoot.ChanceLootEntry(0.4f, Items.GRASS_FIBRE)))));
    public static final CactusBlock CACTUS = Blocks.register("cactus", new CactusBlock(new Properties().noOcclude().usesCustomRender().dropsItems(Items.CACTUS)));
    public static final Block BLAST_FURNACE = Blocks.register("blast_furnace", new BlastFurnaceBlock(new Properties().hardness(12.0F).effectiveTool(ToolType.PICKAXE).requiresTool().dropsItems(Items.BLAST_FURNACE)));
    public static final Block IRON_ORE = Blocks.register("iron_ore", new Block(new Properties().hardness(3.0F).soundType(SoundType.STONE).effectiveTool(ToolType.PICKAXE).toolRequirement(ToolLevel.STONE).requiresTool().dropsItems(Items.IRON_ORE)));
    public static final Block SNOWY_GRASS_BLOCK = Blocks.register("snowy_grass_block", new Block(new Properties().hardness(3F).soundType(SoundType.SNOW).effectiveTool(ToolType.SHOVEL).dropsItems(Items.DIRT, Items.SNOW_BALL)));
    public static final Block LIGHT = Blocks.register("light", new LightBlock(new Properties().hardness(0.5F).noCollision().usesCustomRender().effectiveTool(ToolType.PICKAXE)));
    public static final Block ICE = Blocks.register("ice", new Block(new Properties().hardness(0.5F).lightReduction(4).soundType(SoundType.STONE).effectiveTool(ToolType.PICKAXE)));
    public static final Block SNOW_BLOCK = Blocks.register("snow_block", new Block(new Properties().hardness(0.5F).soundType(SoundType.SNOW).effectiveTool(ToolType.PICKAXE)));

    private static <T extends Block> T register(String name, T block) {
        Registries.BLOCK.register(new NamespaceID(name), block);
        return block;
    }

    public static void init() {
        // Load class
    }
}
