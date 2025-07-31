package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.api.event.EventSystem;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.api.events.ClientChunkEvent;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.ShowInNodeView;
import dev.ultreon.quantum.world.vec.ChunkVec;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;

public class ChunkModel extends GameObject {
    private static final LazyInitializer<Model> gizmo = LazyInitializer.<Model>builder().setInitializer(ChunkModel::createBorderGizmo).get();
    private static final Color CHUNK_GIZMO_COLOR = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    private final ChunkVec pos;
    private final ClientChunk chunk;
    private final Material material;
    @Nullable
    private ModelInstance gizmoInstance = null;

    @ShowInNodeView
    private boolean beingBuilt;

    private final ObjectMap<RenderPass, ChunkMesh> meshes = new ObjectMap<>();
    private final BoundingBox bounds = new BoundingBox();
    private final OpaqueFaces opaqueFaces = new OpaqueFaces();

    public ChunkModel(ChunkVec pos, ClientChunk chunk, WorldRenderer renderer) {
        this.material = renderer.getMaterial();
        this.pos = pos;
        this.chunk = chunk;
        this.chunk.opaqueFaces = opaqueFaces;

        if (Gdx.gl30 != null) {
            IntBuffer buf = BufferUtils.newIntBuffer(1);
            Gdx.gl30.glGenQueries(1, buf);
        }
    }

    /**
     *
     * @return a tight-fit bounding box of the chunk.
     */
    public @Nullable BoundingBox build() {
        bounds.inf();
        if (beingBuilt) return null;
        generateModel(bounds);
        chunk.dirty = false;
        chunk.initialized = true;
        return bounds;
    }

    private void generateModel(BoundingBox bounds) {
        chunk.immediateRebuild = false;
        try {
            this.gizmoInstance = new ModelInstance(gizmo.get(), "gizmos/chunk/" + pos.x + "-" + pos.y + "-" + pos.z);
        } catch (ConcurrentException e) {
            throw new RuntimeException(e);
        }

        this.beingBuilt = true;
        if (meshes.isEmpty()) {
            ChunkVec pos = chunk.vec;
            QuantumClient.invokeAndWait(() -> {
                ModelBuilder builder = new ModelBuilder();
                builder.begin();
                return builder;
            });

            buildAsync(pos, bounds);
        }
        QuantumClient.invokeAndWait(chunk::loadCustomRendered);

        chunk.dirty = false;
        QuantumClient.invoke(() -> {
            chunk.onUpdated();
            chunk.initialized = true;
        });
        this.beingBuilt = false;
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    private void buildAsync(ChunkVec pos, BoundingBox bounds) {
        long millis = System.currentTimeMillis();
        chunk.meshStatus = MeshStatus.MESHING;

        if (chunk.isUniform()) {
            chunk.meshStatus = MeshStatus.UNIFORM;
            chunk.meshDuration = System.currentTimeMillis() - millis;
            return;
        }

        try {
            for (ObjectMap.Entry<RenderPass, ChunkMesh> mesh : this.meshes.entries()) {
                if (mesh != null) mesh.value.dispose();
            }
            ChunkModelBuilder chunkModelBuilder = new ChunkModelBuilder(chunk);
            GamePlatform.get().supplyAsync(() -> {
                chunkModelBuilder.begin();

                if (!chunk.mesher.buildMesh(bounds, opaqueFaces, (blk, model, pass) -> {
                    if (model == null) return true;
                    return pass.equals(model.getRenderPass());
                }, chunkModelBuilder)) {
                    chunk.meshStatus = MeshStatus.SKIPPED;
                    chunk.meshDuration = System.currentTimeMillis() - millis;
                    return null;
                }

                return QuantumClient.invoke(() -> {
                    chunkModelBuilder.end(meshes);
                    EventSystem.postDefault(new ClientChunkEvent.ModelBuilt(chunk, this));
                });
            }).exceptionally(throwable -> {
                crash(new CrashLog("Failed to generate chunk model: " + pos, throwable), pos, millis);
                return null;
            }).thenAccept(v -> {
                if (v == null) {
                    return;
                } else {
                    v.getNow(null);
                }
                chunk.meshStatus = MeshStatus.MESHED;
                chunk.meshDuration = System.currentTimeMillis() - millis;
            }).exceptionally(throwable -> {
                crash(new CrashLog("Failed to generate chunk model: " + pos, throwable), pos, millis);
                return null;
            });
        } catch (Throwable t) {
            crashDirect(new CrashLog("Failed to generate chunk model: " + pos, t), pos, millis);
        } finally {
            this.beingBuilt = false;
        }
    }

    private static void crash(CrashLog pos, ChunkVec pos1, long millis) {
        QuantumClient.invoke(() -> crashDirect(pos, pos1, millis));
    }

    private static void crashDirect(CrashLog pos, ChunkVec pos1, long millis) {
        CrashCategory category = new CrashCategory("Chunk Details");
        category.add("Position", pos1.toString());
        category.add("Time", System.currentTimeMillis() - millis);
        pos.addCategory(category);
        QuantumClient.crash(pos);
    }

    private static Model createBorderGizmo() {
        ModelBuilder modelBuilder = new ModelBuilder();
        MeshBuilder meshBuilder = new MeshBuilder();
        modelBuilder.begin();
        meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_LINES);

        Vector3 start = new Vector3();
        Vector3 end = new Vector3();

        meshBuilder.line(start.set(0, -64, 0), CHUNK_GIZMO_COLOR, end.set(0, 320, 0), CHUNK_GIZMO_COLOR);
        meshBuilder.line(start.set(16, -64, 0), CHUNK_GIZMO_COLOR, end.set(0, 320, 0), CHUNK_GIZMO_COLOR);
        meshBuilder.line(start.set(16, -64, 0), CHUNK_GIZMO_COLOR, end.set(16, 320, 0), CHUNK_GIZMO_COLOR);
        meshBuilder.line(start.set(0, -64, 0), CHUNK_GIZMO_COLOR, end.set(16, 320, 0), CHUNK_GIZMO_COLOR);

        meshBuilder.end();
        return modelBuilder.end();
    }

    public void rebuild() {
        if (beingBuilt) return;
        BoundingBox build = build();
        if (build == null) return;
        chunk.tightBounds.set(build);
        chunk.dirty = false;
        chunk.onUpdated();
        chunk.initialized = true;
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void dispose() {
        super.dispose();
        if (gizmoInstance != null) gizmoInstance = null;
        for (ObjectMap.Entry<RenderPass, ChunkMesh> instance : meshes.entries()) {
            instance.value.dispose();
        }
        meshes.clear();
    }

    public boolean isLoaded() {
        return !meshes.isEmpty();
    }

    public boolean needsRebuild(ClientWorld world) {
        return chunk.immediateRebuild;
    }

    public static LazyInitializer<Model> getGizmo() {
        return gizmo;
    }

    public ChunkVec getPos() {
        return pos;
    }

    public ClientChunk getChunk() {
        return chunk;
    }

    public Material getMaterial() {
        return material;
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void setTranslation(float x, float y, float z) {
        for (ChunkMesh instance : meshes.values()) {
            instance.instance.worldTransform.setTranslation(x, y, z);
        }
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    public void render(GameCamera camera, RenderBufferSource bufferSource) {
        this.chunk.vertexCount = 0;
        this.chunk.indexCount = 0;
        for (ObjectMap.Entry<RenderPass, ChunkMesh> instance : meshes.entries()) {
            instance.value.render(camera, bufferSource);

            this.chunk.vertexCount += instance.value.numVertices;
            this.chunk.indexCount += instance.value.numIndices;
        }
    }
}
