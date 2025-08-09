package dev.ultreon.quantum.client.world;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.ultreon.quantum.client.render.RenderPass;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkModelBuilder {
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Map<RenderPass, MeshBuilder> builders = new HashMap<>();
    private boolean started = false;
    private final ClientChunk chunk;

    public ChunkModelBuilder(ClientChunk chunk) {
        this.chunk = chunk;
    }

    public void begin() {
        started = true;
    }

    public List<ChunkMesh> end(List<ChunkMesh> meshes) {
        started = false;

        for (Map.Entry<RenderPass, MeshBuilder> entry : this.builders.entrySet()) {
            if (entry == null)
                continue;
            RenderPass pass = entry.getKey();
            MeshBuilder builder = entry.getValue();
            Mesh part = builder.end();
            meshes.add(new ChunkMesh(pass, part, chunk));
        }

        builders.clear();

        return meshes;
    }

    public MeshPartBuilder get(RenderPass pass) {
        if (!started) throw new IllegalStateException();
        MeshBuilder builder = builders.get(pass);
        if (builder == null) {
            builder = new MeshBuilder();
            builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
            builders.put(pass, builder);
        }
        return builder;
    }
}
