package dev.ultreon.quantum.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldStorage;
import dev.ultreon.quantum.world.gen.TerrainFeature;
import dev.ultreon.quantum.world.gen.layer.TerrainLayer;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.List;

public class WorldEvents {
    public static final Event<PreTick> PRE_TICK = Event.create(listeners -> world -> {
        for (PreTick listener : listeners) {
            listener.onPreTick(world);
        }
    });
    public static final Event<PostTick> POST_TICK = Event.create(listeners -> world -> {
        for (PostTick listener : listeners) {
            listener.onPostTick(world);
        }
    });
    public static final Event<ChunkBuilt> CHUNK_BUILT = Event.create(listeners -> (world, pos, chunk) -> {
        for (ChunkBuilt listener : listeners) {
            listener.onChunkGenerated(world, pos, chunk);
        }
    });
    public static final Event<ChunkLoaded> CHUNK_LOADED = Event.create(listeners -> (world, pos, chunk) -> {
        for (ChunkLoaded listener : listeners) {
            listener.onChunkLoaded(world, pos, chunk);
        }
    });
    public static final Event<ChunkUnloaded> CHUNK_UNLOADED = Event.create(listeners -> (world, pos, chunk) -> {
        for (ChunkUnloaded listener : listeners) {
            listener.onChunkUnloaded(world, pos, chunk);
        }
    });
    public static final Event<CreateBiome> CREATE_BIOME = Event.create(listeners -> (world, domainWarping, layers, features) -> {
        for (CreateBiome listener : listeners) {
            listener.onCreateBiome(world, domainWarping, layers, features);
        }
    });
    public static final Event<SaveWorld> SAVE_WORLD = Event.create(listeners -> (world, save) -> {
        for (SaveWorld listener : listeners) {
            listener.onSaveWorld(world, save);
        }
    });
    public static final Event<LoadWorld> LOAD_WORLD = Event.create(listeners -> (world, save) -> {
        for (LoadWorld listener : listeners) {
            listener.onLoadWorld(world, save);
        }
    });
    @Deprecated public static final Event<SaveRegion> SAVE_REGION = Event.create(listeners -> (world, region) -> {
        for (SaveRegion listener : listeners) {
            listener.onSaveRegion(world, region);
        }
    });
    @Deprecated public static final Event<LoadRegion> LOAD_REGION = Event.create(listeners -> (world, region) -> {
        for (LoadRegion listener : listeners) {
            listener.onLoadRegion(world, region);
        }
    });
    public static final Event<SaveChunk> SAVE_CHUNK = Event.create(listeners -> (region, extraData) -> {
        for (SaveChunk listener : listeners) {
            listener.onSaveChunk(region, extraData);
        }
    });
    public static final Event<LoadChunk> LOAD_CHUNK = Event.create(listeners -> (region, extraData) -> {
        for (LoadChunk listener : listeners) {
            listener.onLoadChunk(region, extraData);
        }
    });

    @FunctionalInterface
    public interface PreTick {
        void onPreTick(World world);
    }

    @FunctionalInterface
    public interface PostTick {
        void onPostTick(World world);
    }

    @FunctionalInterface
    public interface ChunkBuilt {
        void onChunkGenerated(World world, ServerWorld.Region pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface ChunkLoaded {
        void onChunkLoaded(World world, ChunkVec pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface ChunkUnloaded {
        void onChunkUnloaded(World world, ChunkVec pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface CreateBiome {
        void onCreateBiome(World world, DomainWarping domainWarping, List<TerrainLayer> layers, List<TerrainFeature> features);
    }

    @FunctionalInterface
    public interface SaveWorld {
        void onSaveWorld(World world, WorldStorage save);
    }

    @FunctionalInterface
    public interface LoadWorld {
        void onLoadWorld(World world, WorldStorage save);
    }

    @FunctionalInterface
    public interface SaveRegion {
        void onSaveRegion(World world, ServerWorld.Region region);
    }

    @FunctionalInterface
    public interface LoadRegion {
        void onLoadRegion(World world, ServerWorld.Region region);
    }

    @FunctionalInterface
    public interface SaveChunk {
        void onSaveChunk(Chunk region, MapType extraData);
    }

    @FunctionalInterface
    public interface LoadChunk {
        void onLoadChunk(Chunk region, MapType extraData);
    }
}
