/*
Copyright 2024 Kevin James

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.block.entity.BlockEntity;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.input.GameCamera;
import dev.ultreon.quantum.client.management.MaterialManager;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.render.TerrainRenderer;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.client.world.Skybox;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.EntityType;
import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.item.ItemStack;
import dev.ultreon.quantum.menu.ContainerMenu;
import dev.ultreon.quantum.registry.RegistryKey;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.server.util.Utils;
import dev.ultreon.quantum.util.*;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.noise.SimplexNoise;
import dev.ultreon.quantum.world.particles.ParticleType;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import dev.ultreon.quantum.world.vec.ChunkVec;
import dev.ultreon.quantum.world.vec.ChunkVecSpace;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static dev.ultreon.quantum.client.world.ClientWorld.DAY_BOTTOM_COLOR;
import static dev.ultreon.quantum.client.world.ClientWorld.DAY_TOP_COLOR;

@SuppressWarnings({"GDXJavaUnsafeIterator", "GDXJavaStaticResource"})
public class VoxelTerrain implements TerrainRenderer, ClientWorldAccess {
    private static final Environment ENVIRONMENT = new Environment();
    public Model PREFAB;
    public TerrainNode root;

    public GameCamera camera;

    public int camX = 0;
    public int camY = 0;
    public int camZ = 0;

    public static SimplexNoise noise = new SimplexNoise(16, 0.5, 0);

    public static final Array<TerrainNode> GENERATING_QUEUE = new Array<>();
    public static final Array<TerrainNode> MESHING_QUEUE = new Array<>();
    private final Skybox skybox = new Skybox();
    private final ObjectMap<ClientChunkAccess, ModelInstance> instances = new ObjectMap<>();
    private final Vec3d tmp = new Vec3d();
    private final IntMap<Entity> entities = new IntMap<>();
    private final ObjectMap<ChunkVec, ClientChunkAccess> chunks = new ObjectMap<>();

    public VoxelTerrain(GameCamera camera) {
        this.camera = camera;
        this.skybox.topColor.set(DAY_TOP_COLOR);
        this.skybox.bottomColor.set(DAY_BOTTOM_COLOR);
    }

    public void create() {
        // Initialize the noise and root node here
        //noise.SetFractalOctaves(8);
        //4096
        root = new TerrainNode(4096 * 64, 0, 0, 0, null, 0, this);
    }

    public void render() {
        root.updateVisibility();
        Vec3d camPos = camera.getCamPos();
        int cx = (int) (camPos.x / TerrainNode.TERRAIN_SIZE);
        int cy = (int) (camPos.y / TerrainNode.TERRAIN_SIZE);
        int cz = (int) (camPos.z / TerrainNode.TERRAIN_SIZE);

        if (camX != cx || camY != cy || camZ != cz) {
            root.update((int) camPos.x, (int) camPos.y, (int) camPos.z);
            camX = cx;
            camY = cy;
            camZ = cz;
        }

        if (GENERATING_QUEUE.size > 0) {
            int closeIdx = 0;
            int smallestSize = root.size;
            double closeDist = 1e30f;
            for (int i = 0; i < GENERATING_QUEUE.size; i++) {
                Vec3d pos = new Vec3d(GENERATING_QUEUE.get(i).x, GENERATING_QUEUE.get(i).y, GENERATING_QUEUE.get(i).z);
                double d = pos.dst(camPos);
                if (GENERATING_QUEUE.get(i).size <= smallestSize) {
                    if (GENERATING_QUEUE.get(i).size < smallestSize) {
                        closeDist = 1e30f;
                    }
                    if (d <= closeDist) {
                        closeDist = d;
                        closeIdx = i;
                        smallestSize = GENERATING_QUEUE.get(i).size;
                    }
                }
            }
            TerrainNode generating = GENERATING_QUEUE.get(closeIdx);
            GENERATING_QUEUE.removeIndex(closeIdx);

            if (generating.getModelInstance() == null) {
                return;
            }
            generating.generate();
            MESHING_QUEUE.add(generating);
        }

        if (MESHING_QUEUE.size > 0) {
            int closeIdx = 0;
            int smallestSize = root.size;
            double closeDist = 1e30f;
            for (int i = 0; i < MESHING_QUEUE.size; i++) {
                Vec3d pos = new Vec3d(MESHING_QUEUE.get(i).x, MESHING_QUEUE.get(i).y, MESHING_QUEUE.get(i).z);
                double d = pos.dst(camPos);
                if (MESHING_QUEUE.get(i).size <= smallestSize) {
                    if (MESHING_QUEUE.get(i).size < smallestSize) {
                        closeDist = 1e30f;
                    }
                    if (d <= closeDist) {
                        closeDist = d;
                        closeIdx = i;
                        smallestSize = MESHING_QUEUE.get(i).size;
                    }
                }
            }
            TerrainNode meshing = MESHING_QUEUE.get(closeIdx);
            ModelInstance modelInstance = meshing.getModelInstance();
            if (modelInstance != null) {
                MESHING_QUEUE.removeIndex(closeIdx);
                meshing.buildMesh();
                RenderLayer.WORLD.add(modelInstance);

                this.instances.put(meshing, modelInstance);
                this.chunks.put(toChunkVec(meshing.x, meshing.y, meshing.z), meshing);
            }
        }
    }

    @Override
    public Environment getEnvironment() {
        return ENVIRONMENT;
    }

    @Override
    public void free(ClientChunkAccess chunk) {
        this.remove(chunk);
    }

    @Override
    public void renderBackground(ModelBatch batch, float deltaTime) {
        batch.render(this.skybox);
    }

    @Override
    public void render(ModelBatch batch, RenderLayer renderLayer, float deltaTime) {
        if (renderLayer != RenderLayer.WORLD) {
            return;
        }

        Gdx.gl.glClearColor(.5f, .5f, .5f, .5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        render();

        for (var node : this.instances.entries()) {
            ClientChunkAccess key = node.key;
            ModelInstance value = node.value;

            this.tmp.set(camera.getCamPos()).sub(key.getVec().vec3d());
            value.transform.setTranslation((float) this.tmp.x, (float) this.tmp.y, (float) this.tmp.z);
        }
    }

    @Override
    public void collectEntity(Entity entity, ModelBatch batch) {

    }

    @Override
    public boolean unloadChunk(@NotNull ChunkVec chunkVec) {
        return false;
    }

    @Override
    public boolean unloadChunk(@NotNull Chunk chunk, @NotNull ChunkVec pos) {
        return false;
    }

    @Override
    public boolean set(BlockVec pos, BlockState block) {
        return false;
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        return false;
    }

    @Override
    public Array<Entity> getEntities() {
        return entities.values().toArray();
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block, int flags) {
        ChunkAccess chunk = getChunkAt(x, y, z);
        if (chunk == null) {
            return false;
        }
        chunk.set(localize(x, y, z), block);
        return true;
    }

    private BlockVec localize(int x, int y, int z) {
        return new BlockVec(x, y, z, BlockVecSpace.WORLD);
    }

    @Override
    public boolean set(BlockVec pos, BlockState block, int flags) {
        ChunkAccess chunk = getChunkAt(pos);
        if (chunk == null) {
            return false;
        }
        chunk.set(localize(pos), block);
        return true;
    }

    @Override
    public ClientChunkAccess getChunkAt(@NotNull BlockVec pos) {
        return getChunkAt(pos.getIntX(), pos.getIntY(), pos.getIntZ());
    }

    @Override
    public @Nullable ClientChunkAccess getChunk(ChunkVec pos) {
        return this.chunks.get(pos);
    }

    @Override
    public @Nullable ClientChunkAccess getChunk(int x, int y, int z) {
        return this.chunks.get(new ChunkVec(x, y, z, ChunkVecSpace.WORLD));
    }

    @Override
    public ClientChunkAccess getChunkAt(int x, int y, int z) {
        return this.chunks.get(toChunkVec(x, y, z));
    }

    private ChunkVec toChunkVec(int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkY = y >> 4;
        int chunkZ = z >> 4;
        return new ChunkVec(chunkX, chunkY, chunkZ, ChunkVecSpace.WORLD);
    }

    @Override
    public Collection<? extends ClientChunkAccess> getLoadedChunks() {
        return List.of(instances.keys().toArray().toArray(ClientChunkAccess.class));
    }

    @Override
    public boolean isOutOfWorldBounds(BlockVec pos) {
        return false;
    }

    @Override
    public boolean isOutOfWorldBounds(int x, int y, int z) {
        return false;
    }

    @Override
    public int getHeight(int x, int z, HeightmapType type) {
        return 0;
    }

    @Override
    public Heightmap heightMapAt(int x, int z, HeightmapType type) {
        return null;
    }

    @Override
    public void setColumn(int x, int z, BlockState block) {

    }

    @Override
    public void setColumn(int x, int z, int maxY, BlockState block) {

    }

    @Override
    public CompletableFuture<Void> set(int x, int y, int z, int width, int height, int depth, BlockState block) {
        return null;
    }

    @Override
    public int getLoadedChunksCount() {
        return 0;
    }

    @Override
    public void setBlockEntity(BlockVec pos, BlockEntity blockEntity) {

    }

    @Override
    public BlockEntity getBlockEntity(BlockVec pos) {
        return null;
    }

    @Override
    public void drop(ItemStack itemStack, Vec3d position) {

    }

    @Override
    public void drop(ItemStack itemStack, Vec3d position, Vec3d velocity) {

    }

    @Override
    public Iterable<Entity> entitiesWithinDst(Entity entity, int distance) {
        return null;
    }

    @Override
    public Iterable<Entity> collideEntities(Entity droppedItem, BoundingBox ext) {
        return null;
    }

    @Override
    public void spawnParticles(ParticleType particleType, Vec3d position, Vec3d motion, int count) {

    }

    @Override
    public boolean destroyBlock(BlockVec breaking, @Nullable Player breaker) {
        return false;
    }

    @Override
    public int getBlockLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public void setBlockLight(int x, int y, int z, int intensity) {

    }

    @Override
    public void updateLightSources(Vec3i offset, ObjectMap<Vec3i, LightSource> lights) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void despawn(int id) {

    }

    @Override
    public Entity getEntity(int id) {
        return null;
    }

    @Override
    public List<BoundingBox> collide(BoundingBox ext, boolean b) {
        return List.of();
    }

    @Override
    public void openMenu(ContainerMenu containerMenu) {

    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public boolean isServerSide() {
        return false;
    }

    @Override
    public RegistryKey<Biome> getBiome(BlockVec pos) {
        return null;
    }

    @Override
    public RegistryKey<DimensionInfo> getDimension() {
        return null;
    }

    @Override
    public boolean isChunkInvalidated(@NotNull Chunk chunk) {
        return false;
    }

    @Override
    public void updateNeighbours(Chunk chunk) {

    }

    @Override
    public void updateChunkAndNeighbours(Chunk chunk) {

    }

    @Override
    public void updateChunk(@Nullable Chunk chunk) {

    }

    @Override
    public <T extends Entity> T spawn(T entity) {
        entity.onPrepareSpawn(new MapType());
        this.entities.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <T extends Entity> T spawn(T entity, MapType spawnData) {
        entity.onPrepareSpawn(spawnData);
        this.entities.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void despawn(Entity entity) {

    }

    @Override
    public void fillCrashInfo(CrashLog crashLog) {

    }

    @Override
    public BreakResult continueBreaking(BlockVec breaking, float v, Player player) {
        return null;
    }

    @Override
    public boolean stopBreaking(BlockVec blockVec, Player player) {

        return false;
    }

    @Override
    public void startBreaking(BlockVec blockVec, Player player) {

    }

    @Override
    public float getBreakProgress(BlockVec blockVec) {
        return 0;
    }

    @Override
    public long getSeed() {
        return 0;
    }

    @Override
    public void setSpawnPoint(int spawnX, int spawnZ) {

    }

    @Override
    public boolean isSpawnChunk(ChunkVec pos) {
        return false;
    }

    @Override
    public BlockVec getSpawnPoint() {
        return null;
    }

    @Override
    public int getChunksLoaded() {
        return 0;
    }

    @Override
    public long getDaytime() {
        return 0;
    }

    @Override
    public WorldAccess getWorld() {
        return null;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void onChunkUpdated(Chunk chunk) {

    }

    @Override
    public void playSound(SoundEvent sound, double x, double y, double z) {

    }

    @Override
    public void closeMenu(ContainerMenu containerMenu) {

    }

    @Override
    public boolean intersectEntities(BoundingBox boundingBox) {
        return false;
    }

    @Override
    public void reload(ReloadContext context, MaterialManager materialManager) {

    }

    @Override
    public Skybox getSkybox() {
        return skybox;
    }

    @Override
    public void updateBackground() {
        // TODO
    }

    @Override
    public void remove(ClientChunkAccess clientChunk) {
        ModelInstance remove = this.instances.remove(clientChunk);
        if (remove != null) {
            RenderLayer.WORLD.destroy(remove);
        }

        clientChunk.dispose();
    }

    @Override
    public void addParticles(ParticleEffect obtained, Vec3d position, Vec3d motion, int count) {
        // TODO
    }

    @Override
    public void unload(ClientChunkAccess clientChunk) {
        remove(clientChunk);
    }

    @Override
    public Entity removeEntity(int id) {
        return entities.remove(id);
    }

    @Override
    public void onPlayerAttack(int playerId, int entityId) {

    }

    @Override
    public void setDaytime(long time) {

    }

    @Override
    public void addEntity(int id, EntityType<?> type, Vec3d position, MapType pipeline) {
        // TODO
    }

    @Override
    public Array<Entity> getAllEntities() {
        return entities.values().toArray();
    }

    @Override
    public int getSunlight(int x, int y, int z) {
        return 0;
    }

    @Override
    public float getGlobalSunlight() {
        return 1;
    }

    @Override
    public boolean isLoaded(ChunkVec chunkVec) {
        return false;
    }

    @Override
    public void onBlockSet(BlockVec pos, BlockState block) {

    }

    @Override
    public int getVisibleChunks() {
        return 0;
    }

    @Override
    public void reloadChunks() {

    }

    @Override
    public void renderForeground(ModelBatch modelBatch, float deltaTime) {
        
    }

    @Override
    public void setWorld(@Nullable ClientWorldAccess world) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isAlwaysLoaded(ChunkVec pos) {
        return false;
    }

    @Override
    public @NotNull BlockState get(BlockVec pos) {
        ChunkAccess chunkAt = getChunkAt(pos);
        if (chunkAt == null) {
            return BlockState.AIR;
        }
        return chunkAt.get((Point) localize(pos));
    }

    private BlockVec localize(BlockVec pos) {
        return pos.sectionLocal();
    }

    @Override
    public @NotNull BlockState get(int x, int y, int z) {
        return get(new BlockVec(x, y, z, BlockVecSpace.WORLD));
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray) {
        return EntityHit.MISS;
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance) {
        return EntityHit.MISS;
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance, Predicate<Entity> filter) {
        return EntityHit.MISS;
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance, EntityType<?> type) {
        return EntityHit.MISS;
    }

    @Override
    public @NotNull EntityHit rayCastEntity(Ray ray, float distance, Class<? extends Entity> type) {
        return EntityHit.MISS;
    }

    @Override
    public @NotNull BlockHit rayCast(Ray ray, float distance) {
        return BlockHit.MISS;
    }

    @Override
    public @NotNull <T extends Entity> Iterable<Entity> getEntitiesByClass(Class<T> clazz) {
        return List.of();
    }

    @Override
    public @NotNull UUID getUID() {
        return Utils.ZEROED_UUID;
    }
}
