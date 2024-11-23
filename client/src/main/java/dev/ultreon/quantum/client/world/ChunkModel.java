package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.debug.ValueTracker;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.world.vec.ChunkVec;
import kotlin.Lazy;
import kotlin.LazyKt;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class ChunkModel extends GameObject implements RenderableProvider {
    private static final Lazy<Model> gizmo = LazyKt.lazy(ChunkModel::createBorderGizmo);
    private static final Color CHUNK_GIZMO_COLOR = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    private final ChunkVec pos;
    private final ClientChunk chunk;
    private final Material material;
    private final Material transparentMaterial;
    @Nullable
    private volatile Model model = null;
    @Nullable
    private volatile ModelInstance modelInstance = null;
    @Nullable
    private volatile ModelInstance gizmoInstance = null;

    private final Model[] lodModels = new Model[ClientConfig.lodLevels];
    private final Vector3 relativePosition = new Vector3();
    private boolean beingBuilt;

    @Nullable
    private CompletableFuture<Void> task;

    public ChunkModel(ChunkVec pos, ClientChunk chunk, WorldRenderer renderer) {
        this.material = renderer.getMaterial();
        this.transparentMaterial = renderer.getTransparentMaterial();
        this.pos = pos;
        this.chunk = chunk;
    }

    public boolean build() {
        if (beingBuilt || model != null || modelInstance != null) return false;
        generateModel();
        return true;
    }

    private void generateModel() {
        try (var ignored = QuantumClient.PROFILER.start("build")) {
            chunk.immediateRebuild = false;
            this.gizmoInstance = new ModelInstance(gizmo.getValue(), "gizmos/chunk/" + pos.x + "-" + pos.y + "-" + pos.z);

            chunk.whileLocked(() -> {
                if (modelInstance == null) {
                    ChunkVec pos = chunk.getVec();
                    ModelBuilder modelBuilder = new ModelBuilder();
                    modelBuilder.begin();

                    this.beingBuilt = true;

                    buildAsync(modelBuilder, pos);
                    this.beingBuilt = false;
                }
                chunk.loadCustomRendered();

                chunk.dirty = false;
                chunk.onUpdated();
                chunk.initialized = true;
            });
        }
    }

    private void buildAsync(ModelBuilder modelBuilder, ChunkVec pos) {
        long millis = System.currentTimeMillis();

        if (chunk.isUniform()) {
            var model = this.model = new Model();
            this.modelInstance = new ModelInstance(model);
            return;
        }

        try {
            try (var ignored2 = QuantumClient.PROFILER.start("solid-mesh")) {
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);
                chunk.mesher.buildMesh(blk -> !blk.isTransparent(), meshBuilder);
                Mesh end = QuantumClient.invokeAndWait(() -> meshBuilder.end());
                modelBuilder.part("generated/chunk_part_solid", end, GL_TRIANGLES, material);
            }

            try (var ignored3 = QuantumClient.PROFILER.start("transparent-mesh")) {
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);
                chunk.mesher.buildMesh(Block::isTransparent, meshBuilder);
                Mesh end2 = QuantumClient.invokeAndWait(() -> meshBuilder.end());
                modelBuilder.part("generated/chunk_part_transparent", end2, GL_TRIANGLES, transparentMaterial);
            }

            var model = this.model = QuantumClient.invokeAndWait(modelBuilder::end);

            if (this.model == null) {
                throw new IllegalStateException("Failed to generate chunk model: " + pos);
            }

            var modelInstance = this.modelInstance = new ModelInstance(model, 0, 0, 0);
            modelInstance.userData = chunk;
        } catch (Throwable t) {
            CrashLog crashLog = new CrashLog("Failed to generate chunk model: " + pos, t);
            CrashCategory category = new CrashCategory("Chunk Details");
            category.add("Position", pos.toString());
            category.add("Time", System.currentTimeMillis() - millis);
            crashLog.addCategory(category);
            QuantumClient.crash(crashLog);
        } finally {
            this.beingBuilt = false;
            task = null;
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

    public void unload() {
        if (beingBuilt) {
            CompletableFuture<Void> cachedTask = task;
            if (cachedTask != null) {
                cachedTask.cancel(true);
            }
            task = null;
            beingBuilt = false;
            return;
        }

        try (var ignoredSection = QuantumClient.PROFILER.start("chunk-model-unload")) {
            Model model = this.model;
            if (model != null) {
                model.dispose();
            }
            this.model = null;
            modelInstance = null;
        }
    }

    public boolean rebuild() {
        if (model == null && modelInstance == null) return build();
        unload();
        return build();
    }

    @Override
    public void getRenderables(Array<Renderable> array, Pool<Renderable> pool) {
        ModelInstance cModelInstance = modelInstance;
        Model cModel = model;
        if (cModelInstance == null || cModel == null) return;

        int count = array.size;
        cModelInstance.getRenderables(array, pool);
        ValueTracker.trackRenderables(array.size - count);

        cModelInstance.transform.getTranslation(relativePosition);

        float lodThreshold = ClientConfig.lodThreshold * 16.0f;
        float dst = relativePosition.dst(0, 0, 0);
        chunk.lod = (int) (dst / lodThreshold);

        for (int i = 0; i < array.size; i++) {
            Renderable renderable = array.get(i);
            renderable.userData = chunk;
        }

        ModelInstance cGizmoInstance = gizmoInstance;
        if (cGizmoInstance != null && GamePlatform.get().areChunkBordersVisible()) {
            cGizmoInstance.transform = cModelInstance.transform;
            cGizmoInstance.getRenderables(array, pool);
        }
    }

    public void dispose() {
        Model model = this.model;
        if (modelInstance != null) this.modelInstance = null;
        if (model != null) this.model = null;
        if (gizmoInstance != null) gizmoInstance = null;
        if (model != null) model.dispose();
    }

    public boolean isLoaded() {
        return modelInstance != null && model != null;
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

	public Material getTransparentMaterial() {
		return transparentMaterial;
	}

	public @Nullable Model getModel() {
		return model;
	}

	public @Nullable ModelInstance getModelInstance() {
		return modelInstance;
	}

	public @Nullable ModelInstance getGizmoInstance() {
		return gizmoInstance;
	}

	public Model[] getLodModels() {
		return lodModels;
	}

	public Vector3 getRelativePosition() {
		return relativePosition;
	}

	public boolean isBeingBuilt() {
		return beingBuilt;
	}

	public @Nullable CompletableFuture<Void> getTask() {
		return task;
	}
}
