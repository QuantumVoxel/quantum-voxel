package dev.ultreon.quantum.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;

import java.util.Optional;

/**
 * This class represents information about a dimension in a game world.
 * It includes the dimension's unique identifier, an optional seed value,
 * and a key for the chunk generator.
 *
 * @param id           The unique namespace ID of the dimension.
 * @param seed         An optional seed value for the dimension.
 * @param generatorKey The key for the chunk generator associated with the dimension.
 */
public record DimensionInfo(NamespaceID id, Optional<Long> seed, RegistryKey<ChunkGenerator> generatorKey) {
    public static final RegistryKey<DimensionInfo> OVERWORLD = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("overworld"));
    public static final RegistryKey<DimensionInfo> TEST = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("test"));
    public static final RegistryKey<DimensionInfo> SPACE = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("space"));

    public TextObject getName() {
        return TextObject.translation(this.id.getDomain() + ".dimension." + this.id.getPath().replace('/', '.'));
    }

    public static final Codec<DimensionInfo> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(NamespaceID.CODEC.fieldOf("id").forGetter(DimensionInfo::id),
                    Codec.LONG.optionalFieldOf("seed").forGetter(DimensionInfo::seed),
                    NamespaceID.CODEC.xmap(id -> RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, id), RegistryKey::id)
                            .fieldOf("generator").forGetter(DimensionInfo::generatorKey))
            .apply(instance, DimensionInfo::new)
    );
}
