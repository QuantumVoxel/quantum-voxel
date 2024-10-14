package dev.ultreon.quantum.recipe;

import dev.ultreon.quantum.events.LoadingEvent;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.item.Items;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.List;

public class Recipes {
    public static void init() {
        RecipeManager recipes = RecipeManager.get();
        Recipes.registerCraftingRecipes(recipes);

        LoadingEvent.LOAD_RECIPES.factory().onRecipeState(recipes);

        recipes.fireRecipeModifications();
    }

    private static void registerCraftingRecipes(RecipeManager recipes) {
        recipes.register(new NamespaceID("log_to_planks"), new CraftingRecipe(
                List.of(new ItemStack(Items.LOG)),
                new ItemStack(Items.PLANK, 3)));

        recipes.register(new NamespaceID("planks_to_stick"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.STICK, 4)));

        recipes.register(new NamespaceID("planks_to_block"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 4)),
                new ItemStack(Items.PLANKS)));

        recipes.register(new NamespaceID("wooden_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 3)),
                new ItemStack(Items.WOODEN_PICKAXE)));

        recipes.register(new NamespaceID("wooden_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.WOODEN_SHOVEL)));

        recipes.register(new NamespaceID("wooden_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 2)),
                new ItemStack(Items.WOODEN_AXE)));

        recipes.register(new NamespaceID("crafting_bench"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8)),
                new ItemStack(Items.CRAFTING_BENCH)));

        recipes.register(new NamespaceID("rock_to_cobblestone"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 4)),
                new ItemStack(Items.COBBLESTONE)));

        recipes.register(new NamespaceID("stone_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 3), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_PICKAXE)));

        recipes.register(new NamespaceID("crate"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8), new ItemStack(Items.STICK, 2)),
                new ItemStack(Items.CRATE)));

        recipes.register(new NamespaceID("stone_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 1), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_SHOVEL)));

        recipes.register(new NamespaceID("stone_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 2), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_AXE)));

        recipes.register(new NamespaceID("smelt_iron"), new BlastingRecipe(
                new ItemStack(Items.RAW_IRON, 1),
                1000,
                200, new ItemStack(Items.IRON_INGOT)));

        recipes.register(new NamespaceID("smelt_iron"), new BlastingRecipe(
                new ItemStack(Items.IRON_ORE, 1),
                1000,
                200, new ItemStack(Items.IRON_INGOT)));
    }
}
