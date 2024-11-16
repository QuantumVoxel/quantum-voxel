package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.BreakResult;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.ChunkAccess;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface ClientWorldAccess extends WorldAccess {
    void fillCrashInfo(CrashLog crashLog);

    long getDaytime();

    Entity removeEntity(int id);

    void onPlayerAttack(int playerId, int entityId);

    void setDaytime(long time);

    void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline);

    Array<Entity> getAllEntities();

    int getSunlight(int x, int y, int z);

    float getGlobalSunlight();

    boolean isLoaded(dev.ultreon.quantum.world.vec.ChunkVec chunkVec);

    void onBlockSet(BlockVec pos, BlockState block);

    @Override
    default Collection<? extends ClientChunkAccess> getLoadedChunks() {
        return List.of();
    }

    @Override
    @Nullable ClientChunkAccess getChunkAt(@NotNull BlockVec pos);

    @Override
    @Nullable ClientChunkAccess getChunkAt(int x, int y, int z);

    @Override
    @Nullable ClientChunkAccess getChunk(ChunkVec pos);

    @Override
    @Nullable ClientChunkAccess getChunk(int x, int y, int z);
}
