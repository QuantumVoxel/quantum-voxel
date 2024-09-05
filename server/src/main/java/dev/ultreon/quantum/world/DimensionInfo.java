package dev.ultreon.quantum.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;

import java.util.Optional;

public record DimensionInfo(NamespaceID id, Optional<Long> seed, RegistryKey<ChunkGenerator> generatorKey) {
    public static final RegistryKey<DimensionInfo> OVERWORLD = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("overworld"));
    public static final RegistryKey<DimensionInfo> TEST = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("test"));

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
