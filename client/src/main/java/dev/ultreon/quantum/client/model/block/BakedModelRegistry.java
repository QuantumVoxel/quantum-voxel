package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.atlas.TextureAtlas;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public final class BakedModelRegistry implements Disposable {
    private final TextureAtlas atlas;
    private final Map<Block, List<Pair<Predicate<BlockState>, BakedCubeModel>>> bakedModels;

    public BakedModelRegistry(TextureAtlas atlas, Map<Block, List<Pair<Predicate<BlockState>, BakedCubeModel>>> bakedModels) {
        this.atlas = atlas;
        this.bakedModels = bakedModels;
    }

    public TextureAtlas atlas() {
        return this.atlas;
    }

    public Map<Block, List<Pair<Predicate<BlockState>, BakedCubeModel>>> bakedModels() {
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

    @Override
    public void dispose() {
        this.atlas.dispose();
    }
}