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
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3f;

import java.io.IOException;
import java.util.*;

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

    // Test if point "check" is on the other side of the line between "base1" and "base2", compared to "top"
    boolean test(Vec3f base1, Vec3f base2, Vec3f top, Vec3f check) {
        Vector3 temp1 = new Vector3(base1.x, base1.y, base1.z);
        Vector3 temp2 = new Vector3(base2.x, base2.y, base2.z);
        Vector3 temp3 = new Vector3(top.x, top.y, top.z);
        Vector3 temp4 = new Vector3(check.x, check.y, check.z);

        // Construct a normal towards "top"
        Vector3 normal = new Vector3();
        temp1.sub(temp2).crs(temp3.sub(temp2)).nor();

        // Create a plane with the normal and a point on the plane
        Plane plane = new Plane(normal, temp2);

        // Calculate the distance from the plane to the point "check"
        float distance = plane.distance(temp4);
        return distance > 0;
    }

    public List<BBModelVertex> getSortedVertices() {
        if (this.vertices.size() < 4) return this.vertices;

        if (test(vertices.get(1).vertex(), vertices.get(2).vertex(), vertices.get(0).vertex(), vertices.get(3).vertex())) {
            return Arrays.asList(vertices.get(2), vertices.get(0), vertices.get(1), vertices.get(3));
        } else if (test(vertices.get(0).vertex(), vertices.get(1).vertex(), vertices.get(2).vertex(), vertices.get(3).vertex())) {
            return Arrays.asList(vertices.get(0), vertices.get(2), vertices.get(1), vertices.get(3));
        }

        return this.vertices;
    }

    private void rect(MeshPartBuilder builder, Vec2f resolution) {
        BBModelVertex[] vertices = this.vertices.toArray(BBModelVertex[]::new);
        
        v0.setPos(vertices[0].vertex().x, vertices[0].vertex().y, vertices[0].vertex().z).setUV(uvs.get(0).x / resolution.x, uvs.get(0).y / resolution.y);
        v1.setPos(vertices[1].vertex().x, vertices[1].vertex().y, vertices[1].vertex().z).setUV(uvs.get(1).x / resolution.x, uvs.get(1).y / resolution.y);
        v2.setPos(vertices[2].vertex().x, vertices[2].vertex().y, vertices[2].vertex().z).setUV(uvs.get(2).x / resolution.x, uvs.get(2).y / resolution.y);
        v3.setPos(vertices[3].vertex().x, vertices[3].vertex().y, vertices[3].vertex().z).setUV(uvs.get(3).x / resolution.x, uvs.get(3).y / resolution.y);

        builder.rect(v0, v1, v2, v3);
    }

    // Method to calculate the normal for a quad
    public static Vector3 calculateQuadNormal(BBModelVertex v1, BBModelVertex v2, BBModelVertex v3, BBModelVertex v4) {
        // Calculate normals for the two triangles
        Vector3 normal1 = calculateTriangleNormal(v1, v2, v3);
        Vector3 normal2 = calculateTriangleNormal(v1, v3, v4);

        // Average the two normals
        Vector3 quadNormal = new Vector3(
                (normal1.x + normal2.x) / 2.0f,
                (normal1.y + normal2.y) / 2.0f,
                (normal1.z + normal2.z) / 2.0f
        );

        // Normalize the result
        quadNormal.nor();

        return quadNormal;
    }


    // Method to ensure the correct winding order for a quad
    public static void ensureQuadWindingOrder(BBModelVertex... quadVertices) {
        // Calculate the normal for the quad
        Vector3 normal = calculateQuadNormal(quadVertices[0], quadVertices[1], quadVertices[2], quadVertices[3]);

        // Assume positive Z direction as the front face reference normal
        Vector3 referenceNormal = new Vector3(0, 0, 1);

        // Check if the normal is pointing in the same direction as the reference normal
        if (normal.dot(referenceNormal) < 0) {
            // If the normal is pointing in the opposite direction, flip the vertices
            flipQuadVertices(quadVertices);
        }
    }

    // Method to flip the vertices of the quad (swap v2 and v4)
    public static void flipQuadVertices(BBModelVertex[] quadVertices) {
        BBModelVertex temp = quadVertices[1];
        quadVertices[1] = quadVertices[3];
        quadVertices[3] = temp;
    }

    // Method to calculate the normal for a triangle
    public static Vector3 calculateTriangleNormal(BBModelVertex v1, BBModelVertex v2, BBModelVertex v3) {
        Vector3 edge1 = new Vector3(v2.vertex().x - v1.vertex().x, v2.vertex().y - v1.vertex().y, v2.vertex().z - v1.vertex().z);
        Vector3 edge2 = new Vector3(v3.vertex().x - v1.vertex().x, v3.vertex().y - v1.vertex().y, v3.vertex().z - v1.vertex().z);

        return new Vector3(
                edge1.y * edge2.z - edge1.z * edge2.y,
                edge1.z * edge2.x - edge1.x * edge2.z,
                edge1.x * edge2.y - edge1.y * edge2.x
        );
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
