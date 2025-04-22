package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeManager;
import dev.ultreon.quantum.recipe.RecipeRegistry;
import dev.ultreon.quantum.recipe.RecipeType;

public class LoadingEvent {
    public static final Event<RegisterCommands> REGISTER_COMMANDS = Event.create(listeners -> () -> {
        for (RegisterCommands listener : listeners) {
            listener.onRegisterCommands();
        }
    });
    public static final Event<RecipeState> LOAD_RECIPES = Event.create(listeners -> recipes -> {
        for (RecipeState listener : listeners) {
            listener.onRecipeState(recipes);
        }
    });
    public static final Event<RecipeState> UNLOAD_RECIPES = Event.create(listeners -> recipes -> {
        for (RecipeState listener : listeners) {
            listener.onRecipeState(recipes);
        }
    });
    public static final Event<ModifyRecipes> MODIFY_RECIPES = Event.create(listeners -> (recipes, type, recipeRecipeRegistry) -> {
        for (ModifyRecipes listener : listeners) {
            listener.onModifyRecipes(recipes, type, recipeRecipeRegistry);
        }
    });

    @FunctionalInterface
    public interface RegisterCommands {
        void onRegisterCommands();
    }

    @FunctionalInterface
    public interface RecipeState {
        void onRecipeState(RecipeManager recipes);
    }

    @FunctionalInterface
    public interface ModifyRecipes {
        void onModifyRecipes(RecipeManager recipes, RecipeType type, RecipeRegistry<Recipe> recipeRecipeRegistry);
    }
}
