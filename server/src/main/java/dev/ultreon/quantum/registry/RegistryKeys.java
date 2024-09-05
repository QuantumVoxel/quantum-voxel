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
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.weather.Weather;
import dev.ultreon.quantum.world.Biome;
import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.SoundEvent;
import dev.ultreon.quantum.world.capability.CapabilityType;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.particles.ParticleType;

public class RegistryKeys {
    public static final RegistryKey<Registry<Block>> BLOCK = RegistryKey.registry(new NamespaceID("block"));
    public static final RegistryKey<Registry<Item>> ITEM = RegistryKey.registry(new NamespaceID("item"));
    public static final RegistryKey<Registry<NoiseConfig>> NOISE_CONFIG = RegistryKey.registry(new NamespaceID("noise_settings"));
    public static final RegistryKey<Registry<EntityType<?>>> ENTITY_TYPE = RegistryKey.registry(new NamespaceID("entity_type"));
    public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENT = RegistryKey.registry(new NamespaceID("sound"));
    public static final RegistryKey<Registry<MenuType<?>>> MENU_TYPE = RegistryKey.registry(new NamespaceID("menu_type"));
    public static final RegistryKey<Registry<Biome>> BIOME = RegistryKey.registry(new NamespaceID("biome"));
    public static final RegistryKey<Registry<Weather>> WEATHER = RegistryKey.registry(new NamespaceID("weather"));
    public static final RegistryKey<Registry<Attribute>> ATTRIBUTE = RegistryKey.registry(new NamespaceID("attribute"));
    public static final RegistryKey<Registry<DamageSource>> DAMAGE_SOURCE = RegistryKey.registry(new NamespaceID("damage_source"));
    public static final RegistryKey<Registry<RecipeType<?>>> RECIPE_TYPE = RegistryKey.registry(new NamespaceID("recipe_type"));
    public static final RegistryKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE = RegistryKey.registry(new NamespaceID("block_entity_type"));
    public static final RegistryKey<Registry<ParticleType>> PARTICLE_TYPE = RegistryKey.registry(new NamespaceID("particle_type"));
    public static final RegistryKey<Registry<FontIconMap>> FONT_ICON_MAP = RegistryKey.registry(new NamespaceID("font_icon_map"));
    public static final RegistryKey<Registry<CapabilityType<?, ?>>> CAPABILITY_TYPE = RegistryKey.registry(new NamespaceID("capability_type"));
    public static final RegistryKey<Registry<ChunkGenerator>> CHUNK_GENERATOR = RegistryKey.registry(new NamespaceID("chunk_generator"));
    public static final RegistryKey<Registry<DimensionInfo>> DIMENSION = RegistryKey.registry(new NamespaceID("dimension"));
}
