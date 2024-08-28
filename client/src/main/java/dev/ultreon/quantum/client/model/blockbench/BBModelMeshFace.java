package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.client.texture.TextureManager;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BBModelMeshFace {
    private final List<Vector2> uvs;
    private final List<BBModelVertex> vertices;
    private final int texture;

    public BBModelMeshFace(List<Vector2> uvs, List<BBModelVertex> vertices, int texture) {
        this.uvs = uvs;
        this.vertices = vertices;
        this.texture = texture;
    }

    private final VertexInfo v0 = new VertexInfo();
    private final VertexInfo v1 = new VertexInfo();
    private final VertexInfo v2 = new VertexInfo();
    private final VertexInfo v3 = new VertexInfo();

    public void write(ModelBuilder model, Map<Integer, BBTexture> texture2builder, Map<BBTexture, MeshPartBuilder> meshes, Vec2f resolution) {
        MeshPartBuilder builder = meshes.computeIfAbsent(texture2builder.get(texture), integer -> {
            Material attributes = new Material();
            attributes.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            attributes.set(new DepthTestAttribute(GL20.GL_LEQUAL));

            try {
                attributes.set(TextureAttribute.createDiffuse(texture2builder.get(texture).loadOrGetTexture()));
            } catch (IOException e) {
                attributes.set(TextureAttribute.createDiffuse(TextureManager.getDefaultTex()));
            }
            return model.part(integer.uuid().toString(), GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates, attributes);
        });

        builder.setUVRange(0, 0, 1, 1); // FIXME is this right?

        switch (vertices.size()) {
            case 2:
                line(builder);
                break;
            case 3:
                triangle(builder, resolution);
                break;
            case 4:
                rect(builder, resolution);
                break;
            default:
                throw new IllegalStateException("Unsupported number of vertices for face: " + vertices.size());
        }
    }

    private void line(MeshPartBuilder builder) {
        builder.line(getVertex(0), getVertex(1));
    }

    private void triangle(MeshPartBuilder builder, Vec2f resolution) {
        v0.setPos(getVertex(0)).setUV(uvs.get(0).x / resolution.x, uvs.get(0).y / resolution.y).setNor(0, 0, 0);
        v1.setPos(getVertex(1)).setUV(uvs.get(1).x / resolution.x, uvs.get(1).y / resolution.y).setNor(0, 0, 0);
        v2.setPos(getVertex(2)).setUV(uvs.get(2).x / resolution.x, uvs.get(2).y / resolution.y).setNor(0, 0, 0);
        builder.triangle(v0, v1, v2);
    }

    private void rect(MeshPartBuilder builder, Vec2f resolution) {
        v0.setPos(getVertex(0)).setUV(uvs.get(0).x / resolution.x, uvs.get(0).y / resolution.y).setNor(0, 0, 0);
        v1.setPos(getVertex(1)).setUV(uvs.get(1).x / resolution.x, uvs.get(1).y / resolution.y).setNor(0, 0, 0);
        v2.setPos(getVertex(2)).setUV(uvs.get(2).x / resolution.x, uvs.get(2).y / resolution.y).setNor(0, 0, 0);
        v3.setPos(getVertex(3)).setUV(uvs.get(3).x / resolution.x, uvs.get(3).y / resolution.y).setNor(0, 0, 0);
        builder.rect(v0, v1, v3, v2);
    }

    private Vector3 getVertex(int index) {
        Vec3f vertex = vertices.get(index).vertex();
        return new Vector3(vertex.x / 16f, vertex.y / 16f, vertex.z / 16f);
    }

    @Override
    public String toString() {
        return "BBModelMeshFace[" +
               "uvs=" + uvs + ", " +
               "vertices=" + vertices + ", " +
               "texture=" + texture + ']';
    }

    public List<Vector2> uvs() {
        return uvs;
    }

    public List<BBModelVertex> vertices() {
        return vertices;
    }

    public int texture() {
        return texture;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBModelMeshFace) obj;
        return Objects.equals(this.uvs, that.uvs) &&
               Objects.equals(this.vertices, that.vertices) &&
               this.texture == that.texture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uvs, vertices, texture);
    }


}
