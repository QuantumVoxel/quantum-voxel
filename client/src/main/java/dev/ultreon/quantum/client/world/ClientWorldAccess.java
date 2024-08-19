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
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ClientWorldAccess extends WorldAccess {
    ClientChunkAccess getChunkAt(@NotNull dev.ultreon.quantum.world.vec.BlockVec pos);

    @Override
    @Nullable ClientChunkAccess getChunk(dev.ultreon.quantum.world.vec.ChunkVec pos);

    @Override
    @Nullable ClientChunkAccess getChunk(int x, int z);

    ClientChunkAccess getChunkAt(int x, int y, int z);

    Collection<? extends ClientChunkAccess> getLoadedChunks();

    boolean isChunkInvalidated(@NotNull Chunk chunk);

    void fillCrashInfo(CrashLog crashLog);

    BreakResult continueBreaking(dev.ultreon.quantum.world.vec.BlockVec breaking, float v, Player player);

    void stopBreaking(dev.ultreon.quantum.world.vec.BlockVec blockVec, Player player);

    void startBreaking(dev.ultreon.quantum.world.vec.BlockVec blockVec, Player player);

    float getBreakProgress(BlockVec blockVec);

    int getDaytime();

    Entity removeEntity(int id);

    void onPlayerAttack(int playerId, int entityId);

    void setDaytime(int time);

    void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline);

    Array<Entity> getAllEntities();

    int getSunlight(int x, int y, int z);

    float getGlobalSunlight();

    boolean isLoaded(dev.ultreon.quantum.world.vec.ChunkVec chunkVec);

    void onBlockSet(BlockVec pos, BlockState block);
}
