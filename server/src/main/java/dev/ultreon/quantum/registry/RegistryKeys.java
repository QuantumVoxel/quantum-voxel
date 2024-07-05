package dev.ultreon.quantum.registry;

import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.entity.BlockEntityType;
import dev.ultreon.quantum.entity.Attribute;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.item.Item;
import dev.ultreon.quantum.menu.MenuType;
import dev.ultreon.quantum.recipe.RecipeType;
import dev.ultreon.quantum.text.icon.FontIconMap;
import dev.ultreon.quantum.util.Identifier;
import dev.ultreon.quantum.weather.Weather;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.particles.ParticleType;

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
    public static final RegistryKey<Registry<ParticleType>> PARTICLE_TYPE = RegistryKey.registry(new Identifier("particle_type"));
    public static final RegistryKey<Registry<FontIconMap>> FONT_ICON_MAP = RegistryKey.registry(new Identifier("font_icon_map"));
}
