package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.Queue;
import com.google.common.util.concurrent.AtomicDouble;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.libs.commons.v0.vector.Vec2d;
import dev.ultreon.libs.commons.v0.vector.Vec2f;
import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.particle.ClientParticle;
import dev.ultreon.quantum.client.particle.ClientParticleRegistry;
import dev.ultreon.quantum.client.particle.PFXPool;
import dev.ultreon.quantum.client.player.LocalPlayer;
import dev.ultreon.quantum.client.player.RemotePlayer;
import dev.ultreon.quantum.client.util.Rot;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SBlockBreakingPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SChunkStatusPacket;
import dev.ultreon.quantum.network.packets.c2s.C2SPlaceBlockPacket;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.util.InvalidThreadException;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.particles.ParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.badlogic.gdx.math.MathUtils.lerp;
import static dev.ultreon.quantum.client.util.ExtKt.deg;
import static java.lang.Math.max;

public final class ClientWorld extends World implements Disposable {
    public static final int DAY_CYCLE = 24000;
    public static final AtomicReference<RgbColor> FOG_COLOR = new AtomicReference<>(RgbColor.rgb(0x7fb0fe));
    public static final AtomicDouble FOG_DENSITY = new AtomicDouble(0.001);
    public static final AtomicDouble FOG_START = new AtomicDouble(0.0);
    public static final AtomicDouble FOG_END = new AtomicDouble(1.0);
    public static final AtomicReference<Vec2f> ATLAS_SIZE = new AtomicReference<>(new Vec2f(512, 512));
    public static final AtomicReference<Vec2f> ATLAS_OFFSET = new AtomicReference<>(new Vec2f(0.99908f, 1.03125f));
    public static Rot SKYBOX_ROTATION = deg(-60);
    public static RgbColor DAY_TOP_COLOR = RgbColor.rgb(0x7fb0fe);
    public static RgbColor DAY_BOTTOM_COLOR = RgbColor.rgb(0xc1d3f1);
    public static RgbColor NIGHT_TOP_COLOR = RgbColor.rgb(0x01010b);
    public static RgbColor NIGHT_BOTTOM_COLOR = RgbColor.rgb(0x0a0c16);
    public static RgbColor SUN_RISE_COLOR = RgbColor.rgb(0xff3000);
    public static RgbColor VOID_COLOR = RgbColor.rgb(0x0a0a0a);
    public static int VOID_Y_START = 20;
    public static int VOID_Y_END = 0;
    @NotNull
    private final QuantumClient client;
    private final Map<ChunkPos, ClientChunk> chunks = new HashMap<>();
    private int chunkRefresh;
    private ChunkPos oldChunkPos = new ChunkPos(0, 0);
    private int time = 0;
    private int totalChunks;
    private final Vec3i tmp = new Vec3i();
    private final Queue<LightData> panelQueue = new Queue<>();
    private final ObjectIntMap<LightData> panelMap = new ObjectIntMap<>();

    public ClientWorld(@NotNull QuantumClient client) {
        super();
        this.client = client;
    }

    @Override
    public int getRenderDistance() {
        return ClientConfig.renderDistance;
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
    public void updateChunkAndNeighbours(@NotNull Chunk chunk) {
        super.updateChunkAndNeighbours(chunk);

        this.updateLightChunks((ClientChunk) chunk);
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
            if (this.destroyBlock(breaking, breaker)) {
                this.client.connection.send(new C2SBlockBreakingPacket(breaking, C2SBlockBreakingPacket.BlockStatus.BROKEN));
                this.client.connection.send(new C2SBlockBreakPacket(breaking));
            }
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
        ClientChunk chunk = getChunkAt(blockPos);
        if (chunk != null) {
            chunk.setLightSource(World.toLocalBlockPos(x, y, z, this.tmp), block.getLight());
        }

        if ((flags & BlockFlags.SYNC) != 0) this.sync(x, y, z, block);
        if ((flags & BlockFlags.LIGHT) != 0 && chunk != null && isBlockSet) {
            updateLightChunks(chunk);
        }
        if ((flags & BlockFlags.UPDATE) != 0 && chunk != null && isBlockSet) {
            chunk.set(World.toLocalBlockPos(x, y, z, this.tmp), block);
            updateChunkAndNeighbours(chunk);

            for (CubicDirection direction : CubicDirection.values()) {
                BlockPos offset = blockPos.offset(direction);
                BlockProperties blockProperties = this.get(offset);
                blockProperties.update(this, offset);
            }
        }

        return isBlockSet;
    }

    private void updateLightChunks(ClientChunk chunk) {
        ClientChunk[] neighbourChunks = getNeighbourChunks(chunk);
        Vec3i offset = chunk.getOffset();

        chunk.clearLight();;
        for (ClientChunk neighbourChunk : neighbourChunks) {
            if (neighbourChunk != null)
                neighbourChunk.clearLight();
        }

        updateLightChunk(chunk);
        for (ClientChunk neighbourChunk : neighbourChunks) {
            if (neighbourChunk != null)
                updateLightChunk(neighbourChunk);
        }
    }

    private ClientChunk[] getNeighbourChunks(ClientChunk chunk) {
        return new ClientChunk[]{
                getChunk(chunk.getPos().x() - 1, chunk.getPos().z() - 1),
                getChunk(chunk.getPos().x(), chunk.getPos().z() - 1),
                getChunk(chunk.getPos().x() + 1, chunk.getPos().z() - 1),
                getChunk(chunk.getPos().x() - 1, chunk.getPos().z()),
                getChunk(chunk.getPos().x() + 1, chunk.getPos().z()),
                getChunk(chunk.getPos().x() - 1, chunk.getPos().z() + 1),
                getChunk(chunk.getPos().x(), chunk.getPos().z() + 1),
                getChunk(chunk.getPos().x() + 1, chunk.getPos().z() + 1)
        };
    }

    private void updateLightChunk(ClientChunk chunk) {
        if (client.world != null) {
            chunk.updateLight(client.world);
        }

        Vec3i offset = chunk.getOffset();
        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.x; z < offset.x + CHUNK_SIZE; z++) {
                setInitialSunlight(x, z);
            }
        }
    }

    public void setInitialSunlight(int startX, int startZ) {
        Queue<int[]> queue = new Queue<>();

        Chunk chunk = getChunkAt(startX, 0, startZ);
        if (chunk == null) return;

        // Start from the top of the world and move downward
        for (int y = WORLD_HEIGHT - 1; y >= 0; y--) {
            BlockPos localBlockPos = toLocalBlockPos(startX, y, startZ);
            int lightReduction = chunk.get(localBlockPos).getLightReduction();
            if (lightReduction < 15) {
                int intensity = 15 - lightReduction;
                setSunlight(localBlockPos.x(), localBlockPos.y(), localBlockPos.z(), intensity); // Assuming maximum sunlight intensity is 15
                queue.addLast(new int[]{localBlockPos.x(), localBlockPos.y(), localBlockPos.z(), intensity});
            } else {
                break; // Stop when hitting a non-transparent block
            }
        }

        // Process the queue to propagate sunlight horizontally and downward
        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int x = chunk.getOffset().x + current[0];
            int y = chunk.getOffset().y + current[1];
            int z = chunk.getOffset().z + current[2];
            BlockPos local = new BlockPos(current[0], current[1], current[2]);

            int currentLight = getSunlight(x, y, z);

            if (currentLight > 1) {
                int newLight = currentLight - 1;
                int reduction = getReduction(x, y, z);

                // Check and propagate to neighboring blocks
                if (!(currentLight == 15 && current[0] == 15)) propagateSunlight(queue, x + 1, y, z, newLight, reduction);
                if (!(currentLight == 15 && current[0] == 0)) propagateSunlight(queue, x - 1, y, z, newLight, reduction);
                if (!(currentLight == 15 && current[2] == 15)) propagateSunlight(queue, x, y, z + 1, newLight, reduction);
                if (!(currentLight == 15 && current[2] == 0)) propagateSunlight(queue, x, y, z - 1, newLight, reduction);

                // Check the block below to propagate sunlight downward
                if (y > 0 && getReduction(x, y - 1, z) < 15) {
                    propagateSunlight(queue, x, y - 1, z, newLight, reduction);
                }
            }
        }
    }

    private int getReduction(int x, int y, int z) {
        return max(get(x, y, z).getLightReduction(), 0);
    }

    private void propagateSunlight(Queue<int[]> queue, int x, int y, int z, int intensity, int reduction) {
        if (y >= 0 && y < WORLD_HEIGHT && reduction < 15 && getSunlight(x, y, z) < intensity) {
            int i = intensity - reduction;
            if (i < 0) return;
            setSunlight(x, y, z, intensity);
            queue.addLast(new int[]{x, y, z, i});
        }
    }

    private int getSunlight(int x, int y, int z) {
        ClientChunk chunk = getChunkAt(x, y, z);
        if (chunk != null) {
            return chunk.getSunlight(toLocalBlockPos(x, y, z));
        }

        return 0;
    }

    private void setSunlight(int x, int y, int z, int intensity) {
        ClientChunk chunk = getChunkAt(x, y, z);

        if (chunk != null) {
            chunk.setSunlight(toLocalBlockPos(x, y, z), intensity);
        }
    }

    public void resetQueue() {
        panelQueue.clear();
        panelMap.clear();
    }

    private void floodfill(int startX, int startY, int startZ, int light) {
        Queue<int[]> queue = new Queue<>();
        queue.addLast(new int[]{startX, startY, startZ, light});

        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int intensity = current[3];

            if (y < 0 || y >= WORLD_HEIGHT) continue;

            if (getBlockLight(x, y, z) < intensity) {
                setBlockLight(x, y, z, intensity);
            } else {
                continue; // Skip processing if the current block light is not less than the intensity
            }

            if (intensity > 0) {
                newState(queue, x + 1, y, z, intensity);
                newState(queue, x, y + 1, z, intensity);
                newState(queue, x, y, z + 1, intensity);
                newState(queue, x - 1, y, z, intensity);
                newState(queue, x, y - 1, z, intensity);
                newState(queue, x, y, z - 1, intensity);
            }
        }
    }

    private void newState(Queue<int[]> queue, int x, int y, int z, int intensity) {
        ClientChunk chunkAt = this.getChunkAt(x, y, z);
        if (chunkAt == null) return;
        BlockProperties blockProperties = chunkAt.get(toLocalBlockPos(x, y, z, this.tmp));
        int lightReduction = max(blockProperties.getLightReduction(), 1);
        queue.addLast(new int[]{x, y, z, intensity - lightReduction});
    }

    public int getBlockLight(Vec3i pos) {
        return getBlockLight(pos.x, pos.y, pos.z);
    }

    public void setBlockLight(LightData data, int level) {
        this.setBlockLight(data.pos, level);
    }

    public void setBlockLight(Vec3i pos, int level) {
        this.setBlockLight(pos.x, pos.y, pos.z, level);
    }

    public LightData[] getNeighbors(LightData data) {
        LightData[] neighbors = new LightData[6];
        neighbors[0] = getBlockLightData(data.pos.x + 1, data.pos.y, data.pos.z);
        neighbors[1] = getBlockLightData(data.pos.x - 1, data.pos.y, data.pos.z);
        if (data.pos.y + 1 < WORLD_HEIGHT) neighbors[2] = getBlockLightData(data.pos.x, data.pos.y + 1, data.pos.z);
        if (data.pos.y - 1 >= 0) neighbors[3] = getBlockLightData(data.pos.x, data.pos.y - 1, data.pos.z);
        neighbors[4] = getBlockLightData(data.pos.x, data.pos.y, data.pos.z + 1);
        neighbors[5] = getBlockLightData(data.pos.x, data.pos.y, data.pos.z - 1);
        return neighbors;
    }

    private LightData getBlockLightData(int x, int y, int z) {
        return new LightData(x, y, z, (byte) getBlockLight(x, y, z));
    }

    public int getBlockLight(int x, int y, int z) {
        ClientChunk chunk = getChunkAt(x, y, z);
        if (chunk != null) {
            return (byte) chunk.getBlockLight(toLocalBlockPos(x, y, z, this.tmp));
        } else {
            return 0;
        }
    }

    public void setBlockLight(int x, int y, int z, int light) {
        ClientChunk chunk = this.getChunkAt(x, y, z);
        if (chunk != null) {
            chunk.setBlockLight(toLocalBlockPos(x, y, z, this.tmp), light);
        }
    }

    @Override
    public void updateLightSources(@NotNull Vec3i offset, ObjectMap<Vec3i, LightSource> lights) {
        super.updateLightSources(offset, lights);

        for (int x = offset.x; x < offset.x + CHUNK_SIZE; x++) {
            for (int z = offset.x; z < offset.x + CHUNK_SIZE; z++) {
                setInitialSunlight(x, z);
            }
        }

        for (LightSource source : lights.values()) {
            int x = offset.x + source.x();
            int y = offset.y + source.y();
            int z = offset.z + source.z();
            int level = source.level();

            floodfill(x, y, z, level);
        }
    }

    private void updateSunlight(int blockX, int blockY, int blockZ, boolean isAdded) {
        if (blockY < 0 || blockY >= WORLD_HEIGHT) return;

        Queue<int[]> queue = new Queue<>();

        if (isAdded) {
            // Block is added, set its sunlight to 0 and start the queue
            queue.addLast(new int[]{blockX, blockY, blockZ});
            setSunlight(blockX, blockY, blockZ, 0);
        } else {
            // Block is removed, propagate sunlight downward from the removed block's position
            for (int y = blockY; y >= 0; y--) {
                int reduction = getReduction(blockX, y, blockZ);
                if (reduction < 15) {
                    int newLight = (y == WORLD_HEIGHT - 1) ? 15 : getSunlight(blockX, y + 1, blockZ) - reduction;
                    setSunlight(blockX, y, blockZ, newLight);
                    queue.addLast(new int[]{blockX, y, blockZ, newLight});
                } else {
                    break;
                }
            }
        }

        // Process the queue to update sunlight propagation
        while (!queue.isEmpty()) {
            int[] current = queue.removeFirst();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int currentLight = getSunlight(x, y, z);
            int reduction = getReduction(x, y, z);

            // Check and propagate to neighboring blocks
            updateNeighborSunlight(queue, x + 1, y, z, currentLight, reduction);
            updateNeighborSunlight(queue, x - 1, y, z, currentLight, reduction);
            updateNeighborSunlight(queue, x, y, z + 1, currentLight, reduction);
            updateNeighborSunlight(queue, x, y, z - 1, currentLight, reduction);

            // Check the block below to update sunlight downward
            if (y > 0) {
                updateNeighborSunlight(queue, x, y - 1, z, currentLight, reduction);
            }

            // Check the block above to update sunlight upward if the block is transparent
            if (y < WORLD_HEIGHT - 1 && reduction < 15) {
                updateNeighborSunlight(queue, x, y + 1, z, currentLight, reduction);
            }
        }
    }

    private void updateNeighborSunlight(Queue<int[]> queue, int x, int y, int z, int currentLight, int reduction) {
        if (y >= 0 && y < WORLD_HEIGHT) {
            int neighborLight = getSunlight(x, y, z);
            int reduced = currentLight - reduction;
            if (neighborLight != 0 && neighborLight < currentLight) {
                int max = max(neighborLight, max(reduced, 0));
                setSunlight(x, y, z, max);
                queue.addLast(new int[]{x, y, z, max});
            } else if (neighborLight > currentLight) {
                // If the neighboring light is greater than the current block light, reduce it
                setSunlight(x, y, z, reduced);
                queue.addLast(new int[]{x, y, z, reduced});
            }
        }
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
        if (new Vec2d(pos.x(), pos.z()).dst(new Vec2d(player.getChunkPos().x(), player.getChunkPos().z())) > ClientConfig.renderDistance) {
            this.client.connection.send(new C2SChunkStatusPacket(pos, Chunk.Status.SKIP));
            return;
        }

        ClientChunk finalChunk = chunk;
        QuantumClient.invoke(() -> {
            synchronized (this) {
                this.chunks.put(pos, finalChunk);
                this.totalChunks++;
                finalChunk.ready();
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
                    if (new Vec2d(chunkPos.x(), chunkPos.z()).dst(player.getChunkPos().x(), player.getChunkPos().z()) > ClientConfig.renderDistance) {
                        iterator.remove();
                        clientChunk.dispose();
                        this.updateNeighbours(clientChunk);
                    }
                }
            }
        }
    }

    public Array<Entity> getAllEntities() {
        return this.entitiesById.values().toArray();
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
    public RgbColor getSkyColor() {
        WorldRenderer worldRenderer = QuantumClient.get().worldRenderer;
        if (worldRenderer == null) return RgbColor.BLACK;
        return RgbColor.gdx(worldRenderer.getSkybox().topColor);
    }

    public int getDaytime() {
        return this.time % DAY_CYCLE;
    }

    static RgbColor mixColors(RgbColor color1, RgbColor color2, double percent) {
        percent = Mth.clamp(percent, 0.0, 1.0);
        double inversePercent = 1.0 - percent;
        int redPart = (int) (color1.getRed() * percent + color2.getRed() * inversePercent);
        int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inversePercent);
        int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inversePercent);
        int alphaPart = (int) (color1.getAlpha() * percent + color2.getAlpha() * inversePercent);
        return RgbColor.rgba(redPart, greenPart, bluePart, alphaPart);
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
