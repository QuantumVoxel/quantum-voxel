package com.ultreon.quantum.registry;

import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.block.entity.BlockEntityType;
import com.ultreon.quantum.entity.Attribute;
import com.ultreon.quantum.entity.EntityType;
import com.ultreon.quantum.entity.damagesource.DamageSource;
import com.ultreon.quantum.item.Item;
import com.ultreon.quantum.menu.MenuType;
import com.ultreon.quantum.recipe.RecipeType;
import com.ultreon.quantum.util.Identifier;
import com.ultreon.quantum.weather.Weather;
import com.ultreon.quantum.world.Biome;
import com.ultreon.quantum.world.SoundEvent;
import com.ultreon.quantum.world.gen.noise.NoiseConfig;

public final class Registries {
    public static final Registry<Registry<?>> REGISTRY = Registry.REGISTRY;

    public static final Registry<Block> BLOCK = Registries.create(RegistryKeys.BLOCK);
    public static final Registry<Item> ITEM = Registries.create(RegistryKeys.ITEM);
    public static final Registry<NoiseConfig> NOISE_SETTINGS = Registries.create(RegistryKeys.NOISE_SETTINGS);
    public static final Registry<EntityType<?>> ENTITY_TYPE = Registries.create(RegistryKeys.ENTITY_TYPE);
    public static final Registry<SoundEvent> SOUND_EVENT = Registries.create(RegistryKeys.SOUND_EVENT);
    public static final Registry<MenuType<?>> MENU_TYPE = Registries.create(RegistryKeys.MENU_TYPE);
    public static final Registry<Biome> BIOME = Registries.create(RegistryKeys.BIOME);
    public static final Registry<Weather> WEATHER = Registries.create(RegistryKeys.WEATHER);
    public static final Registry<Attribute> ATTRIBUTE = Registries.create(RegistryKeys.ATTRIBUTE);
    public static final Registry<DamageSource> DAMAGE_SOURCE = Registries.create(RegistryKeys.DAMAGE_SOURCE);
    public static final Registry<RecipeType<?>> RECIPE_TYPE = Registries.create(RegistryKeys.RECIPE_TYPE);
    public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = Registries.create(RegistryKeys.BLOCK_ENTITY_TYPE);

    public static void nopInit() {
        // Load class
    }

    @SafeVarargs
    public static <T> Registry<T> create(Identifier id, T... typeGetter) {
        Registry<T> registry = Registry.builder(id, typeGetter).build();
        Registries.REGISTRY.register(id, registry);
        return registry;
    }

    @SafeVarargs
    public static <T> Registry<T> create(RegistryKey<Registry<T>> id, T... typeGetter) {
        Registry<T> registry = Registry.builder(id.element(), typeGetter).build();
        Registries.REGISTRY.register(id.element(), registry);
        return registry;
    }
}
