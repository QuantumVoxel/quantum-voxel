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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.ShowInNodeView;
import dev.ultreon.quantum.world.vec.ChunkVec;
import kotlin.Lazy;
import kotlin.LazyKt;
import org.jetbrains.annotations.Nullable;

import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;

public class ChunkModel extends GameObject {
    private static final Lazy<Model> gizmo = LazyKt.lazy(ChunkModel::createBorderGizmo);
    private static final Color CHUNK_GIZMO_COLOR = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    private final ChunkVec pos;
    private final ClientChunk chunk;
    private final Material material;
    private final WorldRenderer worldRenderer;
    @Nullable
    private ModelInstance gizmoInstance = null;
    private final Array<RenderPass> usedPasses = new Array<>();

    @ShowInNodeView
    private boolean beingBuilt;

    private final ChunkModelBuilder chunkModelBuilder;
    private final MeshBuilder meshBuilder = new MeshBuilder();
    private final ObjectMap<RenderPass, ChunkMesh> meshes = new ObjectMap<>();

    public ChunkModel(ChunkVec pos, ClientChunk chunk, WorldRenderer renderer) {
        this.material = renderer.getMaterial();
        this.pos = pos;
        this.chunk = chunk;
        this.worldRenderer = renderer;
        this.chunkModelBuilder = new ChunkModelBuilder(chunk);

        if (Gdx.gl30 != null) {
            IntBuffer buf = BufferUtils.newIntBuffer(1);
            Gdx.gl30.glGenQueries(1, buf);
        }
    }

    public boolean build() {
        if (beingBuilt) return true;
        generateModel();
        chunk.dirty = false;
        chunk.initialized = true;
        return true;
    }

    private void generateModel() {
        chunk.immediateRebuild = false;
        this.gizmoInstance = new ModelInstance(gizmo.getValue(), "gizmos/chunk/" + pos.x + "-" + pos.y + "-" + pos.z);

        this.beingBuilt = true;
        if (meshes.isEmpty()) {
            ChunkVec pos = chunk.getVec();
            QuantumClient.invokeAndWait(() -> {
                ModelBuilder builder = new ModelBuilder();
                builder.begin();
                return builder;
            });


            buildAsync(pos);
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
    private void buildAsync(ChunkVec pos) {
        long millis = System.currentTimeMillis();
        chunk.meshStatus = MeshStatus.MESHING;

        if (chunk.isUniform()) {
            chunk.meshStatus = MeshStatus.UNIFORM;
            chunk.meshDuration = System.currentTimeMillis() - millis;
            return;
        }

        try {
            for (ObjectMap.Entry<RenderPass, ChunkMesh> model : this.meshes.entries()) {
                if (model != null) model.value.dispose();
            }
            AtomicBoolean passed = new AtomicBoolean(false);
            RenderBufferSource bufferSource = QuantumClient.get().renderBuffers();
            chunkModelBuilder.begin(bufferSource);

            if (!chunk.mesher.buildMesh((blk, model, pass) -> {
                if (model == null) return true;
                return pass.equals(model.getRenderPass());
            }, chunkModelBuilder)) {
                chunk.meshStatus = MeshStatus.SKIPPED;
                chunk.meshDuration = System.currentTimeMillis() - millis;
                return;
            }

            chunkModelBuilder.end(meshes, bufferSource);
            chunk.meshStatus = MeshStatus.MESHED;
            chunk.meshDuration = System.currentTimeMillis() - millis;
        } catch (Throwable t) {
            CrashLog crashLog = new CrashLog("Failed to generate chunk model: " + pos, t);
            CrashCategory category = new CrashCategory("Chunk Details");
            category.add("Position", pos.toString());
            category.add("Time", System.currentTimeMillis() - millis);
            crashLog.addCategory(category);
            QuantumClient.crash(crashLog);
        } finally {
            this.beingBuilt = false;
        }
    }

    @SuppressWarnings("GDXJavaUnsafeIterator")
    private void reset() {
        meshBuilder.clear();
        for (ObjectMap.Entry<RenderPass, ChunkMesh> entry : this.meshes.entries()) {
            if (entry == null) continue;
            entry.value.dispose();
        }
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
        build();
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
        return world.isChunkInvalidated(chunk);
    }

    public static Lazy<Model> getGizmo() {
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
