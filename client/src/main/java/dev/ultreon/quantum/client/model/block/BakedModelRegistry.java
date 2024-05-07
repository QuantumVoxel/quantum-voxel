package dev.ultreon.quantum.client.model.block;

import com.google.common.collect.ImmutableMap;
import com.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.atlas.TextureAtlas;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class BakedModelRegistry {
    private final TextureAtlas atlas;
    private final ImmutableMap<Block, List<Pair<Predicate<BlockProperties>, BakedCubeModel>>> bakedModels;

    public BakedModelRegistry(TextureAtlas atlas, ImmutableMap<Block, List<Pair<Predicate<BlockProperties>, BakedCubeModel>>> bakedModels) {
        this.atlas = atlas;
        this.bakedModels = bakedModels;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public ImmutableMap<Block, List<Pair<Predicate<BlockProperties>, BakedCubeModel>>> bakedModels() {
        return this.bakedModels;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BakedModelRegistry that = (BakedModelRegistry) obj;
        return Objects.equals(this.atlas, that.atlas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.atlas);
    }

    @Override
    public String toString() {
        return "BakedModelRegistry[" +
                "atlas=" + this.atlas + ']';
    }
}