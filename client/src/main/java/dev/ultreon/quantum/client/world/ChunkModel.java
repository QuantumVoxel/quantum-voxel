package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.world.ChunkVec;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class ChunkModel implements RenderableProvider {
    private final ChunkVec pos;
    private final ClientChunk chunk;
    private final Material material;
    private final Material transparentMaterial;
    private Model model = null;
    private ModelInstance modelInstance = null;

    public ChunkModel(ChunkVec pos, ClientChunk chunk, WorldRenderer renderer) {
        this.material = renderer.getMaterial();
        this.transparentMaterial = renderer.getTransparentMaterial();
        this.pos = pos;
        this.chunk = chunk;
    }

    public Model getModel() {
        return model;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public boolean build() {
        if (model != null || modelInstance != null) return false;
        model = generateModel();
        modelInstance = new ModelInstance(model);
        return true;
    }

    private Model generateModel() {
        chunk.immediateRebuild = false;
        chunk.whileLocked(() -> {
            if (modelInstance == null) {
                ModelManager modelManager = ModelManager.INSTANCE;
                ChunkVec pos = chunk.getPos();
                ModelBuilder modelBuilder = new ModelBuilder();
                MeshBuilder meshBuilder = new MeshBuilder();
                modelBuilder.begin();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);
                chunk.mesher.meshVoxels(modelBuilder,
                        meshBuilder,
                        block -> block.doesRender() && !block.isTransparent()
                );
                Mesh end = meshBuilder.end();
                modelBuilder.part("generated/chunk_part_solid", end, GL_TRIANGLES, material);

                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, GL_TRIANGLES);
                chunk.mesher.meshVoxels(modelBuilder,
                        meshBuilder,
                        block -> block.doesRender() && block.isTransparent()
                );
                Mesh end2 = meshBuilder.end();
                modelBuilder.part("generated/chunk_part_transparent", end2, GL_TRIANGLES, transparentMaterial);

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

    public void unload() {
        model.dispose();
        model = null;
        modelInstance = null;
    }

    public boolean rebuild() {
        if (model == null && modelInstance == null) return build();
        unload();
        return build();
    }

    public ChunkVec getPos() {
        return pos;
    }

    public ClientChunk getChunk() {
        return chunk;
    }

    @Override
    public void getRenderables(Array<Renderable> array, Pool<Renderable> pool) {
        if (modelInstance == null) return;
        modelInstance.getRenderables(array, pool);
        for (int i = 0; i < array.size; i++) {
            Renderable renderable = array.get(i);
            renderable.userData = chunk;
        }
    }

    public void dispose() {
        Model model = this.model;
        this.modelInstance = null;
        this.model = null;

        if (model != null)
            model.dispose();
    }

    public boolean needsRebuild(ClientWorld world) {
        return world.isChunkInvalidated(chunk);
    }
}
