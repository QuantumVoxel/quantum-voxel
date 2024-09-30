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
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.config.ClientConfig;
import dev.ultreon.quantum.client.render.meshing.GreedyMesher;
import dev.ultreon.quantum.world.vec.ChunkVec;
import kotlin.Lazy;
import kotlin.LazyKt;
import lombok.Getter;

import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

@Getter
public class ChunkModel implements RenderableProvider {
    private static final Lazy<Model> gizmo = LazyKt.lazy(ChunkModel::createBorderGizmo);
    private static final Color CHUNK_GIZMO_COLOR = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    private final ChunkVec pos;
    private final ClientChunk chunk;
    private final Material material;
    private final Material transparentMaterial;
    private Model model = null;
    private ModelInstance modelInstance = null;
    private ModelInstance gizmoInstance = null;

    private final Model[] lodModels = new Model[ClientConfig.lodLevels];
    private final Vector3 relativePosition = new Vector3();

    public ChunkModel(ChunkVec pos, ClientChunk chunk, WorldRenderer renderer) {
        this.material = renderer.getMaterial();
        this.transparentMaterial = renderer.getTransparentMaterial();
        this.pos = pos;
        this.chunk = chunk;
    }

    public boolean build() {
        if (model != null || modelInstance != null) return false;
        model = generateModel();
        return true;
    }

    private Model generateModel() {
        try (var ignored = QuantumClient.PROFILER.start("build")) {
            chunk.immediateRebuild = false;
            this.gizmoInstance = new ModelInstance(gizmo.getValue(), "gizmos/chunk/" + pos.x + "-" + pos.y + "-" + pos.z);

            chunk.whileLocked(() -> {
                if (modelInstance == null) {
                    ChunkVec pos = chunk.getVec();
                    ModelBuilder modelBuilder = new ModelBuilder();
                    MeshBuilder meshBuilder = new MeshBuilder();
                    modelBuilder.begin();

                    boolean skip = true;

                    List<GreedyMesher.Face> prepareSolid = chunk.mesher.prepare(
                            block -> block.doesRender() && !block.isTransparent()
                    );
                    skip &= prepareSolid.isEmpty();

                    List<GreedyMesher.Face> prepareTransparent = chunk.mesher.prepare(
                            block -> block.doesRender() && block.isTransparent()
                    );
                    skip &= prepareTransparent.isEmpty();

                    if (skip) {
                        chunk.markEmpty();
                        return;
                    }

                    chunk.markNotEmpty();

                    if (!prepareSolid.isEmpty()) {
                        try (var ignored2 = QuantumClient.PROFILER.start("solid-mesh")) {
                            meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);
                            chunk.mesher.meshFaces(prepareSolid, meshBuilder);
                            Mesh end = meshBuilder.end();
                            modelBuilder.part("generated/chunk_part_solid", end, GL_TRIANGLES, material);
                        }
                    }

                    if (!prepareTransparent.isEmpty()) {
                        try (var ignored3 = QuantumClient.PROFILER.start("transparent-mesh")) {
                            meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);
                            chunk.mesher.meshFaces(prepareTransparent, meshBuilder);
                            Mesh end2 = meshBuilder.end();
                            modelBuilder.part("generated/chunk_part_transparent", end2, GL_TRIANGLES, transparentMaterial);
                        }
                    }

                    this.model = modelBuilder.end();

                    if (this.model == null) {
                        throw new IllegalStateException("Failed to generate chunk model: " + pos);
                    }

                    this.modelInstance = new ModelInstance(model, 0, 0, 0);
                    this.modelInstance.userData = chunk;
                }
                chunk.loadCustomRendered();

                chunk.dirty = false;
                chunk.onUpdated();
                chunk.initialized = true;
            });

            return model;
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
        try (var ignoredSection = QuantumClient.PROFILER.start("chunk-model-unload")) {
            model.dispose();
            model = null;
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
        if (modelInstance == null || model == null) return;
        modelInstance.getRenderables(array, pool);

        modelInstance.transform.getTranslation(relativePosition);

        float lodThreshold = ClientConfig.lodThreshold * 16.0f;
        float dst = relativePosition.dst(0, 0, 0);
        chunk.lod = (int) (dst / lodThreshold);

        for (int i = 0; i < array.size; i++) {
            Renderable renderable = array.get(i);
            renderable.userData = chunk;
        }

        if (gizmoInstance != null && GamePlatform.get().areChunkBordersVisible()) {
            gizmoInstance.transform = modelInstance.transform;
            gizmoInstance.getRenderables(array, pool);
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
        return modelInstance!= null && model!= null;
    }

    public boolean needsRebuild(ClientWorld world) {
        return world.isChunkInvalidated(chunk);
    }
}
