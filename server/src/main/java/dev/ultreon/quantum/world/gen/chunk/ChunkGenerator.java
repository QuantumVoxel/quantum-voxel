package dev.ultreon.quantum.world.gen.chunk;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ChunkGenerator extends Disposable {
    RegistryKey<ChunkGenerator> OVERWORLD = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new NamespaceID("overworld"));
    RegistryKey<ChunkGenerator> TEST = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new NamespaceID("test"));

    void create(ServerWorld world, long seed);

    void generate(ServerWorld world, BuilderChunk chunk, Collection<ServerWorld.@NotNull RecordedChange> neighbors);

    DomainWarping getLayerDomain();
}
