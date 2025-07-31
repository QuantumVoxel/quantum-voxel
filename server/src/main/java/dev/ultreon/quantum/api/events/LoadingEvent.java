package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.recipe.Recipe;
import dev.ultreon.quantum.recipe.RecipeManager;
import dev.ultreon.quantum.recipe.RecipeRegistry;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.resources.ResourcePackage;
import dev.ultreon.quantum.util.Env;

public interface LoadingEvent extends Event {

    class LoadRecipes implements LoadingEvent {
        private final RecipeManager recipes;

        public LoadRecipes(RecipeManager recipes) {
            this.recipes = recipes;
        }

        public RecipeManager getRecipes() {
            return recipes;
        }
    }

    class UnloadRecipes implements LoadingEvent {
        private final RecipeManager recipes;

        public UnloadRecipes(RecipeManager recipes) {
            this.recipes = recipes;
        }

        public RecipeManager getRecipes() {
            return recipes;
        }
    }

    class ModifyRecipes implements LoadingEvent {
        private final RecipeManager recipes;
        private final RecipeType<?> type;
        private final RecipeRegistry<Recipe> registry;

        public ModifyRecipes(RecipeManager recipes, RecipeType<?> type, RecipeRegistry<Recipe> registry) {
            this.recipes = recipes;
            this.type = type;
            this.registry = registry;
        }

        public RecipeManager getRecipes() {
            return recipes;
        }

        public RecipeRegistry<Recipe> getRegistry() {
            return registry;
        }

        public RecipeType<?> getType() {
            return type;
        }
    }

    public class Configs implements LoadingEvent {
        private final Env env;

        public Configs(Env env) {
            this.env = env;
        }

        public Env getEnv() {
            return env;
        }
    }

    class ImportResourcePackage implements LoadingEvent {
        private final ResourceManager resourceManager;
        private final ResourcePackage resourcePackage;

        public ImportResourcePackage(ResourceManager resourceManager, ResourcePackage resourcePackage) {
            this.resourceManager = resourceManager;
            this.resourcePackage = resourcePackage;
        }

        public ResourcePackage getResourcePackage() {
            return resourcePackage;
        }

        public ResourceManager getResourceManager() {
            return resourceManager;
        }
    }
}
