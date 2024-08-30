package dev.ultreon.quantum.world.gen;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ChunkGenerator extends Disposable {
    void create(ServerWorld world, long seed);

    void generate(ServerWorld world, BuilderChunk chunk, Collection<ServerWorld.@NotNull RecordedChange> neighbors);
}
