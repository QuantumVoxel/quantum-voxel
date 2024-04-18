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

public class RegistryKeys {
    public static final RegistryKey<Registry<Block>> BLOCK = RegistryKey.registry(new Identifier("block"));
    public static final RegistryKey<Registry<Item>> ITEM = RegistryKey.registry(new Identifier("item"));
    public static final RegistryKey<Registry<NoiseConfig>> NOISE_SETTINGS = RegistryKey.registry(new Identifier("noise_settings"));
    public static final RegistryKey<Registry<EntityType<?>>> ENTITY_TYPE = RegistryKey.registry(new Identifier("entity_type"));
    public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENT = RegistryKey.registry(new Identifier("sound"));
    public static final RegistryKey<Registry<MenuType<?>>> MENU_TYPE = RegistryKey.registry(new Identifier("menu_type"));
    public static final RegistryKey<Registry<Biome>> BIOME = RegistryKey.registry(new Identifier("biome"));
    public static final RegistryKey<Registry<Weather>> WEATHER = RegistryKey.registry(new Identifier("weather"));
    public static final RegistryKey<Registry<Attribute>> ATTRIBUTE = RegistryKey.registry(new Identifier("attribute"));
    public static final RegistryKey<Registry<DamageSource>> DAMAGE_SOURCE = RegistryKey.registry(new Identifier("damage_source"));
    public static final RegistryKey<Registry<RecipeType<?>>> RECIPE_TYPE = RegistryKey.registry(new Identifier("recipe_type"));
    public static final RegistryKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE = RegistryKey.registry(new Identifier("block_entity_type"));
}
