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
    ClientChunkAccess getChunkAt(@NotNull BlockPos pos);

    @Override
    @Nullable ClientChunkAccess getChunk(ChunkPos pos);

    @Override
    @Nullable ClientChunkAccess getChunk(int x, int z);

    ClientChunkAccess getChunkAt(int x, int y, int z);

    Collection<? extends ClientChunkAccess> getLoadedChunks();

    boolean isChunkInvalidated(@NotNull Chunk chunk);

    void fillCrashInfo(CrashLog crashLog);

    BreakResult continueBreaking(BlockPos breaking, float v, Player player);

    void stopBreaking(BlockPos blockPos, Player player);

    void startBreaking(BlockPos blockPos, Player player);

    float getBreakProgress(BlockPos blockPos);

    int getDaytime();

    Entity removeEntity(int id);

    void onPlayerAttack(int playerId, int entityId);

    void setDaytime(int time);

    void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline);

    Array<Entity> getAllEntities();

    int getSunlight(int x, int y, int z);

    float getGlobalSunlight();
}
