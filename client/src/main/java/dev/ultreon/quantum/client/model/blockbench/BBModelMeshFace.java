package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import dev.ultreon.quantum.client.texture.TextureManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public record BBModelMeshFace(Map<String, Vec2f> uvs, List<BBModelVertex> vertices, int texture) {
    public void write(ModelBuilder model, Map<Integer, BBTexture> texture2builder, Map<BBTexture, MeshPartBuilder> meshes, Vec2f resolution) {
        MeshPartBuilder builder = meshes.computeIfAbsent(texture2builder.get(texture), integer -> {
            Material attributes = new Material();
            attributes.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            attributes.set(new DepthTestAttribute(GL20.GL_LEQUAL));

            try {
                attributes.set(TextureAttribute.createDiffuse(texture2builder.get(texture).loadOrGetTexture()));
            } catch (IOException e) {
                attributes.set(TextureAttribute.createDiffuse(TextureManager.DEFAULT_TEX));
            }
            return model.part(integer.uuid().toString(), GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, attributes);
        });

        builder.setUVRange(0, 0, resolution.x, resolution.y); // FIXME is this right?

        switch (vertices.size()) {
            case 2 -> line(builder);
            case 3 -> triangle(builder);
            case 4 -> rect(builder);
            default -> throw new IllegalStateException("Unsupported number of vertices for face: " + vertices.size());
        }
    }

    private void line(MeshPartBuilder builder) {
        builder.line(getVertex(0), getVertex(1));
    }

    private void triangle(MeshPartBuilder builder) {
        builder.triangle(getVertex(0), getVertex(1), getVertex(2));
    }

    private void rect(MeshPartBuilder builder) {
        builder.triangle(getVertex(0), getVertex(1), getVertex(2));
        builder.triangle(getVertex(0), getVertex(2), getVertex(3));
    }

    private Vector3 getVertex(int index) {
        Vec3f vertex = vertices.get(index).vertex();
        return new Vector3(vertex.x / 16f, vertex.y / 16f, vertex.z / 16f);
    }
}
