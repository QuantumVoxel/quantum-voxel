package com.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.quantum.CommonConstants;
import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.config.Config;
import com.ultreon.quantum.client.particle.ClientParticle;
import com.ultreon.quantum.client.particle.ClientParticleRegistry;
import com.ultreon.quantum.client.particle.PFXPool;
import com.ultreon.quantum.client.player.LocalPlayer;
import com.ultreon.quantum.client.player.RemotePlayer;
import com.ultreon.quantum.client.util.Rot;
import com.ultreon.quantum.entity.Entity;
import com.ultreon.quantum.entity.EntityType;
import com.ultreon.quantum.entity.Player;
import com.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import com.ultreon.quantum.network.packets.c2s.C2SBlockBreakingPacket;
import com.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import com.ultreon.quantum.network.packets.c2s.C2SPlaceBlockPacket;
import com.ultreon.quantum.util.Color;
import com.ultreon.quantum.util.InvalidThreadException;
import com.ultreon.quantum.world.*;
import com.ultreon.data.types.MapType;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2d;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.quantum.world.particles.ParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.badlogic.gdx.math.MathUtils.lerp;
import static com.ultreon.quantum.client.util.ExtKt.deg;

public final class ClientWorld extends World implements Disposable {
    public static final int DAY_CYCLE = 24000;
    public static final AtomicReference<Color> FOG_COLOR = new AtomicReference<>(Color.rgb(0x7fb0fe));
    public static final AtomicDouble FOG_DENSITY = new AtomicDouble(0.001);
    public static final AtomicDouble FOG_START = new AtomicDouble(0.0);
    public static final AtomicDouble FOG_END = new AtomicDouble(1.0);
    public static final AtomicReference<Vec2f> ATLAS_SIZE = new AtomicReference<>(new Vec2f(512, 512));
    public static final AtomicReference<Vec2f> ATLAS_OFFSET = new AtomicReference<>(new Vec2f(0.99908f, 1.03125f));
    public static Rot SKYBOX_ROTATION = deg(-60);
    public static Color DAY_TOP_COLOR = Color.rgb(0x7fb0fe);
    public static Color DAY_BOTTOM_COLOR = Color.rgb(0xc1d3f1);
    public static Color NIGHT_TOP_COLOR = Color.rgb(0x01010b);
    public static Color NIGHT_BOTTOM_COLOR = Color.rgb(0x0a0c16);
    public static Color SUN_RISE_COLOR = Color.rgb(0xff3000);
    @NotNull
    private final QuantumClient client;
    private final Map<ChunkPos, ClientChunk> chunks = new HashMap<>();
    private int chunkRefresh;
    private ChunkPos oldChunkPos = new ChunkPos(0, 0);
    private int time = 0;
    private int totalChunks;

    public ClientWorld(@NotNull QuantumClient client) {
        super();
        this.client = client;
    }

    @Override
    public int getRenderDistance() {
        return Config.renderDistance;
    }

    @Override
    protected boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkPos pos) {
        if (!QuantumClient.isOnMainThread()) {
            return QuantumClient.invokeAndWait(() -> this.unloadChunk(chunk, pos));
        }

        if (shouldStayLoaded(pos)) return false;

        boolean removed = this.chunks.remove(pos) == chunk;
        if (removed) {
            this.totalChunks--;
        }

        return removed;
    }

    @Override
    protected void checkThread() {
        if (!QuantumClient.isOnMainThread())
            throw new InvalidThreadException(CommonConstants.EX_NOT_ON_RENDER_THREAD);
    }

    @Override
    public @Nullable ClientChunk getChunk(@NotNull ChunkPos pos) {
        synchronized (this) {
            return this.chunks.get(pos);
        }
    }

    @Override
    public ClientChunk getChunk(int x, int z) {
        return (ClientChunk) super.getChunk(x, z);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(@NotNull BlockPos pos) {
        return (ClientChunk) super.getChunkAt(pos);
    }

    @Override
    public @Nullable ClientChunk getChunkAt(int x, int y, int z) {
        return (ClientChunk) super.getChunkAt(x, y, z);
    }

    @Override
    public Collection<ClientChunk> getLoadedChunks() {
        this.checkThread();

        return this.chunks.values();
    }

    @Override
    public boolean isChunkInvalidated(@NotNull Chunk chunk) {
        this.checkThread();
        return super.isChunkInvalidated(chunk);
    }

    @Override
    public void updateChunk(@Nullable Chunk chunk) {
        if (!QuantumClient.isOnMainThread()) {
            QuantumClient.invokeAndWait(() -> this.updateChunk(chunk));
            return;
        }
        super.updateChunk(chunk);
    }

    @Override
    public void startBreaking(@NotNull BlockPos breaking, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.START));
        }
        super.startBreaking(breaking, breaker);
    }

    @Override
    public BreakResult continueBreaking(@NotNull BlockPos breaking, float amount, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.CONTINUE));
        }
        BreakResult breakResult = super.continueBreaking(breaking, amount, breaker);
        if (breakResult == BreakResult.BROKEN) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.STOP));
            this.client.connection.send(new C2SBlockBreakPacket(breaking));
            this.set(breaking, Blocks.AIR.createMeta(), BlockFlags.UPDATE);
        }
        return breakResult;
    }

    @Override
    public void stopBreaking(@NotNull BlockPos breaking, @NotNull Player breaker) {
        if (breaker == this.client.player) {
            this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.STOP));
        }
        super.stopBreaking(breaking, breaker);
    }

    @Override
    public void onChunkUpdated(@NotNull Chunk chunk) {
        this.checkThread();

        super.onChunkUpdated(chunk);
    }

    @Override
    public void playSound(@NotNull SoundEvent sound, double x, double y, double z) {
        float range = sound.getRange();
        Player player = this.client.player;
        if (player != null) {
            player.playSound(sound, (float) ((range - player.getPosition().dst(x, y, z)) / range));
        }
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public boolean set(int x, int y, int z, @NotNull BlockProperties block, int flags) {
        if (!QuantumClient.isOnMainThread()) {
            return QuantumClient.invokeAndWait(() -> this.set(x, y, z, block, flags));
        }
        boolean isBlockSet = super.set(x, y, z, block, flags);
        BlockPos blockPos = new BlockPos(x, y, z);
        if ((flags & BlockFlags.SYNC) != 0) this.sync(x, y, z, block);
        if ((flags & BlockFlags.UPDATE) != 0) {
            for (CubicDirection direction : CubicDirection.values()) {
                BlockPos offset = blockPos.offset(direction);
                BlockProperties blockProperties = this.get(offset);
                blockProperties.update(this, offset);
            }
        }

        return isBlockSet;
    }

    @Override
    public void spawnParticles(@NotNull ParticleType particleType, @NotNull Vec3d position, @NotNull Vec3d motion, int count) {
        if (!QuantumClient.isOnMainThread()) {
            QuantumClient.invokeAndWait(() -> this.spawnParticles(particleType, position, motion, count));
        }

        super.spawnParticles(particleType, position, motion, count);

        ClientParticle clientParticle = ClientParticleRegistry.getParticle(particleType);
        WorldRenderer worldRenderer = this.client.worldRenderer;
        if (worldRenderer != null && clientParticle != null) {
            PFXPool particleController = clientParticle.getPool();
            ParticleEffect obtained = particleController.obtain();
            worldRenderer.addParticles(obtained, position, motion, count);
        } else if (clientParticle == null) {
            World.LOGGER.warn("Unknown particle type: {}", particleType.getId());
        }
    }

    private void sync(int x, int y, int z, BlockProperties block) {
        this.client.connection.send(new C2SPlaceBlockPacket(x, y, z, block));
    }

    public void loadChunk(ChunkPos pos, ClientChunk data) {
        var chunk = QuantumClient.invokeAndWait(() -> this.chunks.get(pos));
        if (chunk == null) {
            chunk = data;
        } else {
            World.LOGGER.warn("Duplicate chunk packet detected! Chunk {}", pos);
            return;
        }
        LocalPlayer player = this.client.player;
        if (player == null) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.FAILED));
            return;
        }
        if (new Vec2d(pos.x(), pos.z()).dst(new Vec2d(player.getChunkPos().x(), player.getChunkPos().z())) > Config.renderDistance) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
            return;
        }

        ClientChunk finalChunk = chunk;
        QuantumClient.invoke(() -> {
            finalChunk.ready();
            synchronized (this) {
                this.chunks.put(pos, data);
                this.totalChunks++;
            }
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SUCCESS));
        });
    }

    public void tick() {
        this.time++;

        if (this.chunkRefresh-- <= 0) {
            this.chunkRefresh = 40;

            LocalPlayer player = this.client.player;
            if (player != null) {
                if (this.oldChunkPos.equals(player.getChunkPos())) return;
                this.oldChunkPos = player.getChunkPos();
                for (Iterator<Map.Entry<ChunkPos, ClientChunk>> iterator = this.chunks.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<ChunkPos, ClientChunk> entry = iterator.next();
                    ChunkPos chunkPos = entry.getKey();
                    ClientChunk clientChunk = entry.getValue();
                    if (new Vec2d(chunkPos.x(), chunkPos.z()).dst(player.getChunkPos().x(), player.getChunkPos().z()) > Config.renderDistance) {
                        iterator.remove();
                        clientChunk.dispose();
                        this.updateNeighbours(clientChunk);
                    }
                }
            }
        }
    }

    public Collection<Entity> getAllEntities() {
        return this.entitiesById.values();
    }

    public float getGlobalSunlight() {
        int daytime = this.getDaytime();
        final int riseSetDuration = ClientWorld.DAY_CYCLE / 24;
        if (daytime < riseSetDuration / 2) {
            return lerp(
                    0.25f, 1.0f,
                    0.5f + daytime / (float) riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 - riseSetDuration / 2) {
            return 1.0f;
        } else if (daytime <= ClientWorld.DAY_CYCLE / 2 + riseSetDuration / 2) {
            return lerp(
                    1.0f, 0.25f,
                    (daytime - ((float) ClientWorld.DAY_CYCLE / 2 - (float) riseSetDuration / 2)) / riseSetDuration);
        } else if (daytime <= ClientWorld.DAY_CYCLE - riseSetDuration / 2) {
            return 0.25f;
        } else {
            return lerp(
                    0.25f, 1.0f,
                    (daytime - (ClientWorld.DAY_CYCLE - (float) riseSetDuration / 2)) / riseSetDuration);
        }
    }

    @Deprecated
    public Color getSkyColor() {
        WorldRenderer worldRenderer = QuantumClient.get().worldRenderer;
        if (worldRenderer == null) return Color.BLACK;
        return Color.gdx(worldRenderer.getSkybox().topColor);
    }

    public int getDaytime() {
        return this.time % DAY_CYCLE;
    }

    static Color mixColors(Color color1, Color color2, double percent) {
        percent = Mth.clamp(percent, 0.0, 1.0);
        double inversePercent = 1.0 - percent;
        int redPart = (int) (color1.getRed() * percent + color2.getRed() * inversePercent);
        int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inversePercent);
        int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inversePercent);
        int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inversePercent);
        return Color.rgba(redPart, greenPart, bluePart, alphaPart);
    }
    
    @Override
    public void dispose() {
        this.checkThread();
        
        super.dispose();
        
        synchronized (this) {
            this.chunks.forEach((chunkPos, clientChunk) -> clientChunk.dispose());
            this.chunks.clear();
        }
    }

    @Override
    public int getTotalChunks() {
        return this.totalChunks;
    }

    public void setDaytime(int daytime) {
        this.time = daytime;
    }

    public void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        QuantumClient.LOGGER.debug("Adding entity with id {} of type {} at {}", new Object[]{id, type.getId(), position});

        Entity entity = type.create(this);
        entity.setId(id);
        entity.setPosition(position);
        entity.onPipeline(pipeline);
        this.entitiesById.put(id, entity);
    }

    @CanIgnoreReturnValue
    public Entity removeEntity(int id) {
        Entity remove = this.entitiesById.remove(id);
        WorldRenderer worldRenderer = client.worldRenderer;
        if (worldRenderer != null) {
            worldRenderer.removeEntity(id);
        }
        return remove;
    }

    public void onPlayerAttack(int playerId, int entityId) {
        Entity player = this.entitiesById.get(playerId);
        Entity entity = this.entitiesById.get(entityId);

        if (entity != null && player instanceof RemotePlayer remotePlayer) {
            remotePlayer.onAttack(entity);
        }

        if (player instanceof LocalPlayer) {
            //??? This should not happen...
            LOGGER.warn("SANITY CHECK: local player tried to attack entity {}!", entityId);
        }
    }
}
