package dev.ultreon.quantum.world;

import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.registry.RegistryKeys;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.gen.chunk.ChunkGenerator;

import java.util.Objects;
import java.util.Optional;

/**
 * This class represents information about a dimension in a game world.
 * It includes the dimension's unique identifier, an optional seed value,
 * and a key for the chunk generator.
 *
 */
public final class DimensionInfo {
    public static final RegistryKey<DimensionInfo> OVERWORLD = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("overworld"));
    public static final RegistryKey<DimensionInfo> TEST = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("test"));
    public static final RegistryKey<DimensionInfo> SPACE = RegistryKey.of(RegistryKeys.DIMENSION, new NamespaceID("space"));

    public TextObject getName() {
        return TextObject.translation(this.id.getDomain() + ".dimension." + this.id.getPath().replace('/', '.'));
    }

    public NamespaceID id() {
        return id;
    }

    public Optional<Long> seed() {
        return seed;
    }

    public RegistryKey<ChunkGenerator> generatorKey() {
        return generatorKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DimensionInfo) obj;
        return Objects.equals(this.id, that.id) &&
               Objects.equals(this.seed, that.seed) &&
               Objects.equals(this.generatorKey, that.generatorKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seed, generatorKey);
    }

    @Override
    public String toString() {
        return "DimensionInfo[" +
               "id=" + id + ", " +
               "seed=" + seed + ", " +
               "generatorKey=" + generatorKey + ']';
    }


    public static DimensionInfo fromJson(JsonValue json) {
        NamespaceID id = new NamespaceID(json.getString("id"));
        Optional<Long> seed = json.has("seed") ? Optional.of(json.getLong("seed")) : Optional.empty();
        RegistryKey<ChunkGenerator> generatorKey = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, new NamespaceID(json.getString("generator")));
        return new DimensionInfo(id, seed, generatorKey);
    }

    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("id", new JsonValue(id.toString()));
        seed.ifPresent(s -> json.addChild("seed", new JsonValue(s)));
        json.addChild("generator", new JsonValue(generatorKey.id().toString()));
        return json;
    }

    private final NamespaceID id;
    private final Optional<Long> seed;
    private final RegistryKey<ChunkGenerator> generatorKey;

    /**
     * @param id           The unique namespace ID of the dimension.
     * @param seed         An optional seed value for the dimension.
     * @param generatorKey The key for the chunk generator associated with the dimension.
     */
    public DimensionInfo(NamespaceID id, Optional<Long> seed, RegistryKey<ChunkGenerator> generatorKey) {
        this.id = id;
        this.seed = seed;
        this.generatorKey = generatorKey;
    }
}
