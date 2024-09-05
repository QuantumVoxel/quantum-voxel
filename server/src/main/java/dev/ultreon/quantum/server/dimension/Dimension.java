package dev.ultreon.quantum.server.dimension;

import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;

public record Dimension(DimensionInfo info, ChunkGenerator generator) {
}
