package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.ObjectMap;
import dev.ultreon.quantum.client.render.RenderBuffer;
import dev.ultreon.quantum.client.render.RenderBufferSource;
import dev.ultreon.quantum.client.render.RenderPass;

public class ChunkModelBuilder {
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final ObjectMap<RenderPass, MeshBuilder> builders = new ObjectMap<>();
    private boolean started = false;
    private RenderBufferSource bufferSource;
    private ClientChunk chunk;

    public ChunkModelBuilder(ClientChunk chunk) {
        this.chunk = chunk;
    }

    public void begin(RenderBufferSource source) {
        bufferSource = source;
        started = true;
    }

    public ObjectMap<RenderPass, ChunkMesh> end(ObjectMap<RenderPass, ChunkMesh> meshes, RenderBufferSource bufferSource) {
        started = false;

        for (ObjectMap.Entry<RenderPass, MeshBuilder> entry : this.builders.entries()) {
            if (entry == null)
                continue;
            RenderPass pass = entry.key;
            MeshBuilder builder = entry.value;
            Mesh part = builder.end();
            meshes.put(pass, new ChunkMesh(pass, part, chunk));
        }

        builders.clear();

        return meshes;
    }

    public MeshPartBuilder get(RenderPass pass) {
        if (!started) throw new IllegalStateException();
        MeshBuilder builder = builders.get(pass);
        if (builder == null) {
            builder = new MeshBuilder();
            builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
            builders.put(pass, builder);
        }
        return builder;
    }
}
