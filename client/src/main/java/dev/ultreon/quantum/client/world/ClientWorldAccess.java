package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.utils.Array;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.world.*;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ClientWorldAccess extends WorldAccess {
    ClientChunkAccess getChunkAt(@NotNull BlockVec pos);

    @Override
    @Nullable ClientChunkAccess getChunk(ChunkVec pos);

    @Override
    @Nullable ClientChunkAccess getChunk(int x, int z);

    ClientChunkAccess getChunkAt(int x, int y, int z);

    Collection<? extends ClientChunkAccess> getLoadedChunks();

    boolean isChunkInvalidated(@NotNull Chunk chunk);

    void fillCrashInfo(CrashLog crashLog);

    BreakResult continueBreaking(BlockVec breaking, float v, Player player);

    void stopBreaking(BlockVec blockVec, Player player);

    void startBreaking(BlockVec blockVec, Player player);

    float getBreakProgress(BlockVec blockVec);

    int getDaytime();

    Entity removeEntity(int id);

    void onPlayerAttack(int playerId, int entityId);

    void setDaytime(int time);

    void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline);

    Array<Entity> getAllEntities();

    int getSunlight(int x, int y, int z);

    float getGlobalSunlight();

    boolean isLoaded(ChunkVec chunkVec);
}
