package dev.ultreon.quantum.server.dimension;

import dev.ultreon.quantum.world.DimensionInfo;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;

import java.util.Objects;

public final class Dimension {
    private final DimensionInfo info;
    private final ChunkGenerator generator;

    public Dimension(DimensionInfo info, ChunkGenerator generator) {
        this.info = info;
        this.generator = generator;
    }

    public DimensionInfo info() {
        return info;
    }

    public ChunkGenerator generator() {
        return generator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Dimension) obj;
        return Objects.equals(this.info, that.info) &&
               Objects.equals(this.generator, that.generator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, generator);
    }

    @Override
    public String toString() {
        return "Dimension[" +
               "info=" + info + ", " +
               "generator=" + generator + ']';
    }

}
