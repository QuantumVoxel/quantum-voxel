package dev.ultreon.quantum.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.quantum.client.util.GameCamera;
import dev.ultreon.quantum.util.BoundingBox;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;

import java.util.Arrays;

public class LineRenderer {
    private final Mesh mesh;
    private final ShaderProgram shader;
    private final Vector3 tmp1 = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3[] corners = new Vector3[8];

    public LineRenderer() {
        String vertexShader = QuantumClient.resource(NamespaceID.of("shaders/line.vert")).readString();
        String fragmentShader = QuantumClient.resource(NamespaceID.of("shaders/line.frag")).readString();
        shader = new ShaderProgram(vertexShader, fragmentShader);
        if (!shader.isCompiled()) throw new GdxRuntimeException(shader.getLog());

        mesh = new Mesh(true, 6, 0,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"));

        Arrays.setAll(corners, i -> new Vector3());
    }



    public void renderBoxFace(Camera cam, Vector3 min, Vector3 max, Direction direction, float pixelThickness, Color color) {
        // Define 8 corners of the box using the min and max vectors
        Vector3[] boxCorners = new Vector3[8];
        boxCorners[0] = new Vector3(min.x, min.y, min.z); // Front-Bottom-Left
        boxCorners[1] = new Vector3(max.x, min.y, min.z); // Front-Bottom-Right
        boxCorners[2] = new Vector3(max.x, max.y, min.z); // Back-Bottom-Right
        boxCorners[3] = new Vector3(min.x, max.y, min.z); // Back-Bottom-Left
        boxCorners[4] = new Vector3(min.x, min.y, max.z); // Front-Top-Left
        boxCorners[5] = new Vector3(max.x, min.y, max.z); // Front-Top-Right
        boxCorners[6] = new Vector3(max.x, max.y, max.z); // Back-Top-Right
        boxCorners[7] = new Vector3(min.x, max.y, max.z); // Back-Top-Left

        // Depending on the direction, render the corresponding face
        switch (direction) {
            case NORTH:
                renderFace(cam, boxCorners, 0, 1, 2, 3, pixelThickness, color); // Front Face
                break;
            case SOUTH:
                renderFace(cam, boxCorners, 4, 5, 6, 7, pixelThickness, color); // Back Face
                break;
            case UP:
                renderFace(cam, boxCorners, 5, 4, 7, 6, pixelThickness, color); // Top Face
                break;
            case DOWN:
                renderFace(cam, boxCorners, 0, 1, 5, 4, pixelThickness, color); // Bottom Face
                break;
            case WEST:
                renderFace(cam, boxCorners, 0, 3, 7, 4, pixelThickness, color); // Left Face
                break;
            case EAST:
                renderFace(cam, boxCorners, 1, 2, 6, 5, pixelThickness, color); // Right Face
                break;
        }
    }

    // This method renders the edges of a given face (4 points)
    private void renderFace(Camera cam, Vector3[] boxCorners, int i0, int i1, int i2, int i3, float pixelThickness, Color color) {
        renderLine(cam, boxCorners[i0], boxCorners[i1], pixelThickness, color); // Edge 1
        renderLine(cam, boxCorners[i1], boxCorners[i2], pixelThickness, color); // Edge 2
        renderLine(cam, boxCorners[i2], boxCorners[i3], pixelThickness, color); // Edge 3
        renderLine(cam, boxCorners[i3], boxCorners[i0], pixelThickness, color); // Edge 4
    }
    public void renderBoxFaceByNormal(Camera cam, Vector3 min, Vector3 max, Vector3 normal, float pixelThickness, Color color) {
        Vector3[] boxCorners = new Vector3[8];
        boxCorners[0] = new Vector3(min.x, min.y, min.z); // 0
        boxCorners[1] = new Vector3(max.x, min.y, min.z); // 1
        boxCorners[2] = new Vector3(max.x, max.y, min.z); // 2
        boxCorners[3] = new Vector3(min.x, max.y, min.z); // 3
        boxCorners[4] = new Vector3(min.x, min.y, max.z); // 4
        boxCorners[5] = new Vector3(max.x, min.y, max.z); // 5
        boxCorners[6] = new Vector3(max.x, max.y, max.z); // 6
        boxCorners[7] = new Vector3(min.x, max.y, max.z); // 7

        // Tolerance for float comparison
        final float EPS = 0.99f;

        if (normal.epsilonEquals(Vector3.Z, EPS)) {
            // Front (+Z)
            renderFace(cam, boxCorners[4], boxCorners[5], boxCorners[6], boxCorners[7], pixelThickness, color);
        } else if (normal.epsilonEquals(Vector3.Z.cpy().scl(-1), EPS)) {
            // Back (-Z)
            renderFace(cam, boxCorners[0], boxCorners[1], boxCorners[2], boxCorners[3], pixelThickness, color);
        } else if (normal.epsilonEquals(Vector3.Y, EPS)) {
            // Top (+Y)
            renderFace(cam, boxCorners[3], boxCorners[2], boxCorners[6], boxCorners[7], pixelThickness, color);
        } else if (normal.epsilonEquals(Vector3.Y.cpy().scl(-1), EPS)) {
            // Bottom (-Y)
            renderFace(cam, boxCorners[0], boxCorners[1], boxCorners[5], boxCorners[4], pixelThickness, color);
        } else if (normal.epsilonEquals(Vector3.X, EPS)) {
            // Right (+X)
            renderFace(cam, boxCorners[1], boxCorners[2], boxCorners[6], boxCorners[5], pixelThickness, color);
        } else if (normal.epsilonEquals(Vector3.X.cpy().scl(-1), EPS)) {
            // Left (-X)
            renderFace(cam, boxCorners[0], boxCorners[3], boxCorners[7], boxCorners[4], pixelThickness, color);
        }
    }

    private void renderFace(Camera cam, Vector3 a, Vector3 b, Vector3 c, Vector3 d, float thickness, Color color) {
        // Center of the face
        Vector3 center = new Vector3();
        center.add(a).add(b).add(c).add(d).scl(0.25f);

        float inset = 0.00250f;

        // Move corners slightly inward
        Vector3 aInset = a.cpy().lerp(center, inset);
        Vector3 bInset = b.cpy().lerp(center, inset);
        Vector3 cInset = c.cpy().lerp(center, inset);
        Vector3 dInset = d.cpy().lerp(center, inset);

        renderLine(cam, aInset, bInset, thickness, color);
        renderLine(cam, bInset, cInset, thickness, color);
        renderLine(cam, cInset, dInset, thickness, color);
        renderLine(cam, dInset, aInset, thickness, color);
    }

    public void renderLine(Camera cam, Vector3 start, Vector3 end, float pixelThickness, Color color) {
        // Line direction
        Vector3 lineDir = new Vector3(end).sub(start).nor();

        // View direction from camera to line midpoint
        Vector3 midpoint = new Vector3(start).lerp(end, 0.5f);
        Vector3 viewDir = new Vector3(midpoint).sub(cam.position).nor();

        // Get pixel thickness in world units at midpoint
        float worldThickness = getPixelWorldScale(cam, midpoint, pixelThickness);

        // Thickness offset: perpendicular to both view and line
        Vector3 offset = new Vector3(viewDir).crs(lineDir).nor().scl(worldThickness / 2f);

        // Compute quad corners
        Vector3 v1 = new Vector3(start).add(offset);
        Vector3 v2 = new Vector3(start).sub(offset);
        Vector3 v3 = new Vector3(end).sub(offset);
        Vector3 v4 = new Vector3(end).add(offset);

        float[] verts = new float[]{
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z,
                v3.x, v3.y, v3.z,

                v3.x, v3.y, v3.z,
                v4.x, v4.y, v4.z,
                v1.x, v1.y, v1.z
        };

        mesh.setVertices(verts);

        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        shader.bind();
        shader.setUniformMatrix("u_projTrans", cam.combined);
        shader.setUniformf("u_color", color);
        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    private float getPixelWorldScale(Camera cam, Vector3 worldPos, float pixelSize) {
        Vector3 p1 = cam.project(new Vector3(worldPos));
        Vector3 p2 = new Vector3(p1).add(pixelSize, 0, 0);
        Vector3 worldP1 = cam.unproject(p1);
        Vector3 worldP2 = cam.unproject(p2);
        return worldP2.dst(worldP1);
    }

    public void dispose() {
        shader.dispose();
        mesh.dispose();
    }

    public void renderBox(GameCamera camera, BoundingBox boundingBox, Direction direction, float thickness, Color color) {
        Vector3 min = camera.relative(boundingBox.min, tmp1);
        Vector3 max = camera.relative(boundingBox.max, tmp2);
        renderBoxFace(camera, min, max, direction, thickness, color);
    }

    public void renderBox(GameCamera camera, BoundingBox boundingBox, Vector3 normal, float thickness, Color color) {
        Vector3 min = camera.relative(boundingBox.min, tmp1);
        Vector3 max = camera.relative(boundingBox.max, tmp2);
        renderBoxFaceByNormal(camera, min, max, normal, thickness, color);
    }
}