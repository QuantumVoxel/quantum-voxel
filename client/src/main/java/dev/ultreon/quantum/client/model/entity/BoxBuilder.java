package dev.ultreon.quantum.client.model.entity;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.MathHelper;
import dev.ultreon.quantum.world.Direction;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class BoxBuilder {
    static float[] backUv = {1, 1, 1, 0, 0, 0, 0, 1,};
    static float[] frontUv = {1, 1, 0, 1, 0, 0, 1, 0,};
    static float[] rightUv = {1, 1, 0, 1, 0, 0, 1, 0,};
    static float[] leftUv = {1, 1, 1, 0, 0, 0, 0, 1,};
    static float[] bottomUv = {0, 0, 0, 1, 1, 1, 0, 1,};
    static float[] topUv = {0, 0, 1, 0, 1, 1, 0, 1};
    static float[] topVertices = {0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1};
    static float[] bottomVertices = {0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0};
    static float[] leftVertices = {0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1};
    static float[] rightVertices = {1, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0};
    static float[] frontVertices = {0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0,};
    static float[] backVertices = {0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 1,};

    private final float x;
    private final float y;
    private final float z;
    private final float width;
    private final float height;
    private final float depth;
    private int u;
    private int v;

    public BoxBuilder(MeshBuilder builder, int x, int y, int z, int width, int height, int depth) {

        this.x = x / 16f;
        this.y = y / 16f;
        this.z = z / 16f;
        this.width = width / 16f;
        this.height = height / 16f;
        this.depth = depth / 16f;
    }

    public BoxBuilder uv(int u, int v) {
        this.u = u;
        this.v = v;
        return this;
    }

    private void face(Vec3i pos, Direction face, TextureRegion region, FloatArray output) {
        float[] vertices;
        switch (face) {
            case UP:
                vertices = BoxBuilder.topVertices;
                break;
            case DOWN:
                vertices = BoxBuilder.bottomVertices;
                break;
            case WEST:
                vertices = BoxBuilder.leftVertices;
                break;
            case EAST:
                vertices = BoxBuilder.rightVertices;
                break;
            case NORTH:
                vertices = BoxBuilder.frontVertices;
                break;
            case SOUTH:
                vertices = BoxBuilder.backVertices;
                break;
            default:
                throw new IllegalArgumentException();
        }

        float[] uvs;
        switch (face) {
            case UP:
                uvs = BoxBuilder.topUv;
                break;
            case DOWN:
                uvs = BoxBuilder.bottomUv;
                break;
            case WEST:
                uvs = BoxBuilder.leftUv;
                break;
            case EAST:
                uvs = BoxBuilder.rightUv;
                break;
            case NORTH:
                uvs = BoxBuilder.frontUv;
                break;
            case SOUTH:
                uvs = BoxBuilder.backUv;
                break;
            default:
                throw new IllegalArgumentException();
        }

        Vector3 normal = face.getNormal();

        // Loop vertices and uvs and add them to the output.
        for (int vertexIdx = 0, uvIdx = 0; vertexIdx < vertices.length; vertexIdx += 3, uvIdx += 2) {
            float x = pos.x + vertices[vertexIdx];
            float y = pos.y + vertices[vertexIdx + 1];
            float z = pos.z + vertices[vertexIdx + 2];

            // Calculate the UV coordinates from the texture region.
            float u = MathHelper.lerp(uvs[uvIdx], region.getU(), region.getU2());
            float v = MathHelper.lerp(uvs[uvIdx + 1], region.getV(), region.getV2());

            output.add(x);
            output.add(y);
            output.add(z);
            output.add(normal.x);
            output.add(normal.y);
            output.add(normal.z);
            output.add(u);
            output.add(v);
        }
    }

    public Model build(Texture texture, Material material) {
        /*
         *        +--------+
         *        |  top   |
         * +------+--------+-------+------+
         * | left | front  | right | back |
         * +------+--------+-------+------+
         *        | bottom |
         *        +-------+
         */

        FloatArray output = new FloatArray();
        int ix = (int) this.x;
        int iy = (int) this.y;
        int iz = (int) this.z;
        int ix2 = (int) (this.x + this.width);
        int iy2 = (int) (this.y + this.height);
        int iz2 = (int) (this.z + this.depth);
        int iw = (int) this.width;
        int ih = (int) this.height;
        int id = (int) this.depth;
        int iu = this.u;
        int iv = this.v;
        this.face(new Vec3i(ix, iy2, iz), Direction.UP, new TextureRegion(texture, iu + id, iv, iw, id), output);
        this.face(new Vec3i(ix, iy, iz), Direction.DOWN, new TextureRegion(texture, iu + id, iv + id + ih, iw, id), output);

        this.face(new Vec3i(ix, iy, iz), Direction.WEST, new TextureRegion(texture, iu, iv + id, id, ih), output);
        this.face(new Vec3i(ix2, iy, iz), Direction.EAST, new TextureRegion(texture, iu + id + iw, iv + id + ih, id, ih), output);

        this.face(new Vec3i(ix, iy, iz), Direction.NORTH, new TextureRegion(texture, iu + id, id + id, iw, ih), output);
        this.face(new Vec3i(ix, iy, iz2), Direction.SOUTH, new TextureRegion(texture, iu + id * 2 + iw, id + id), output);

        return QuantumClient.invokeAndWait(() -> {
            var modelBuilder = new ModelBuilder();
            modelBuilder.begin();
            modelBuilder.part("cube",
                    new Mesh(false, output.size, 0, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)),
                    GL_TRIANGLES, material);
            return modelBuilder.end();
        });
    }
}
