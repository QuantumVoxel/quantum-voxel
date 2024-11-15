package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.world.ChunkBuildInfo;
import org.jetbrains.annotations.Nullable;

public class ClientChunkInfo {
    public long loadDuration;
    @Nullable public ChunkBuildInfo build;
}
