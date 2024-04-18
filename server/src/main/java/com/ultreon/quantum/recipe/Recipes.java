package com.ultreon.quantum.recipe;

import com.ultreon.quantum.events.LoadingEvent;
import com.ultreon.quantum.item.ItemStack;
import com.ultreon.quantum.item.Items;
import com.ultreon.quantum.util.Identifier;

import java.util.List;

public class Recipes {
    public static void init() {
        RecipeManager recipes = RecipeManager.get();
        Recipes.registerCraftingRecipes(recipes);

        LoadingEvent.LOAD_RECIPES.factory().onRecipeState(recipes);

        recipes.fireRecipeModifications();
    }

    private static void registerCraftingRecipes(RecipeManager recipes) {
        recipes.register(new Identifier("log_to_planks"), new CraftingRecipe(
                List.of(new ItemStack(Items.LOG)),
                new ItemStack(Items.PLANK, 3)));

        recipes.register(new Identifier("planks_to_stick"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.STICK, 4)));

        recipes.register(new Identifier("planks_to_block"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 4)),
                new ItemStack(Items.PLANKS)));

        recipes.register(new Identifier("wooden_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 3)),
                new ItemStack(Items.WOODEN_PICKAXE)));

        recipes.register(new Identifier("wooden_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 1)),
                new ItemStack(Items.WOODEN_SHOVEL)));

        recipes.register(new Identifier("wooden_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.STICK, 1), new ItemStack(Items.PLANK, 2)),
                new ItemStack(Items.WOODEN_AXE)));

        recipes.register(new Identifier("crafting_bench"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8)),
                new ItemStack(Items.CRAFTING_BENCH)));

        recipes.register(new Identifier("rock_to_cobblestone"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 4)),
                new ItemStack(Items.COBBLESTONE)));

        recipes.register(new Identifier("stone_pickaxe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 3), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_PICKAXE)));

        recipes.register(new Identifier("crate"), new CraftingRecipe(
                List.of(new ItemStack(Items.PLANK, 8), new ItemStack(Items.STICK, 2)),
                new ItemStack(Items.CRATE)));

        recipes.register(new Identifier("stone_shovel"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 1), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_SHOVEL)));

        recipes.register(new Identifier("stone_axe"), new CraftingRecipe(
                List.of(new ItemStack(Items.ROCK, 2), new ItemStack(Items.STICK, 1)),
                new ItemStack(Items.STONE_AXE)));
    }
}
