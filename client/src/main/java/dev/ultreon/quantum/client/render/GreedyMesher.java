package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.management.MaterialManager;
import dev.ultreon.quantum.client.render.meshing.Mesher;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.world.CubicDirection;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.NotNull;

public class GreedyMesher implements Mesher {

    private static final int CHUNK_WIDTH = World.CHUNK_SIZE;
    private static final int CHUNK_HEIGHT = World.CHUNK_SIZE;

    private static final int SOUTH = 0;
    private static final int NORTH = 1;
    private static final int EAST = 2;
    private static final int WEST = 3;
    private static final int TOP = 4;
    private static final int BOTTOM = 5;

    private final ClientChunk chunk;
    private Quad[][][] voxels;
    private ShaderProgram shader;
    private final Vector3 VOXEL_SIZE = new Vector3(1, 1, 1);
    private final ClientWorldAccess world;
    private final QuantumClient client = QuantumClient.get();
    private final MaterialManager materialManager = client.getMaterialManager();

    public GreedyMesher(ClientChunk chunk) {
        this.chunk = chunk;
        this.world = chunk.getWorld();
    }

    void greedy(ModelBuilder builder, MeshBuilder meshBuilder, UseCondition condition) {

        int i, j, k, l, w, h, u, v, n, side = 0;

        final int[] x = new int[]{0, 0, 0};
        final int[] q = new int[]{0, 0, 0};
        final int[] du = new int[]{0, 0, 0};
        final int[] dv = new int[]{0, 0, 0};

        int quadId = 0;
        final Quad[] mask = new Quad[CHUNK_WIDTH * CHUNK_HEIGHT];

        Quad quad, quad1;

        for (boolean backFace = true, b = false; b != backFace; backFace = backFace && b, b = !b) {

            for (int d = 0; d < 3; d++) {

                u = (d + 1) % 3;
                v = (d + 2) % 3;

                x[0] = 0;
                x[1] = 0;
                x[2] = 0;

                q[0] = 0;
                q[1] = 0;
                q[2] = 0;
                q[d] = 1;

                if (d == 0) {
                    side = backFace ? WEST : EAST;
                } else if (d == 1) {
                    side = backFace ? BOTTOM : TOP;
                } else if (d == 2) {
                    side = backFace ? SOUTH : NORTH;
                }

                for (x[d] = -1; x[d] < CHUNK_WIDTH; ) {

                    n = 0;

                    for (x[v] = 0; x[v] < CHUNK_HEIGHT; x[v]++) {

                        for (x[u] = 0; x[u] < CHUNK_WIDTH; x[u]++) {

                            quad = (x[d] >= 0) ? getBlock(x[0], x[1], x[2], side) : null;
                            quad1 = (x[d] < CHUNK_WIDTH - 1) ? getBlock(x[0] + q[0], x[1] + q[1], x[2] + q[2], side) : null;

                            mask[n++] = ((quad != null && quad1 != null && quad.equals(quad1)))
                                    ? null
                                    : backFace ? quad1 : quad;
                        }
                    }

                    x[d]++;

                    n = 0;

                    for (j = 0; j < CHUNK_HEIGHT; j++) {

                        for (i = 0; i < CHUNK_WIDTH; ) {

                            if (mask[n] != null) {

                                for (w = 1; i + w < CHUNK_WIDTH && mask[n + w] != null && mask[n + w].equals(mask[n]); w++) {
                                }

                                boolean done = false;

                                for (h = 1; j + h < CHUNK_HEIGHT; h++) {

                                    for (k = 0; k < w; k++) {
                                        Quad quad2 = mask[n + k + h * CHUNK_WIDTH];
                                        if (quad2 == null || !quad2.equals(mask[n])) {
                                            done = true;
                                            break;
                                        }
                                    }

                                    if (done) {
                                        break;
                                    }
                                }

                                if (!mask[n].blockState.isTransparent()) {

                                    x[u] = i;
                                    x[v] = j;

                                    du[0] = 0;
                                    du[1] = 0;
                                    du[2] = 0;
                                    du[u] = w;

                                    dv[0] = 0;
                                    dv[1] = 0;
                                    dv[2] = 0;
                                    dv[v] = h;

                                    Mesh builtQuad = quad(new Vector3(x[0], x[1], x[2]),
                                            new Vector3(x[0] + du[0], x[1] + du[1], x[2] + du[2]),
                                            new Vector3(x[0] + du[0] + dv[0], x[1] + du[1] + dv[1], x[2] + du[2] + dv[2]),
                                            new Vector3(x[0] + dv[0], x[1] + dv[1], x[2] + dv[2]),
                                            w,
                                            h,
                                            mask[n],
                                            backFace);

                                    builder.part("greedy_" + (quadId++), builtQuad, GL20.GL_TRIANGLES, materialManager.getMaterialFor(mask[n].blockState.getBlock()));
                                }

                                for (l = 0; l < h; ++l) {

                                    for (k = 0; k < w; ++k) {
                                        mask[n + k + l * CHUNK_WIDTH] = null;
                                    }
                                }

                                i += w;
                                n += w;

                            } else {

                                i++;
                                n++;
                            }
                        }
                    }
                }
            }
        }
    }

    Quad getBlock(final int x, final int y, final int z, final int side) {

        Quad Block = createFace(world.get(x, y, z), side);

        Block.side = side;

        return Block;
    }

    private Quad createFace(BlockState blockState, int side) {
        return new Quad(blockState, side);
    }

    private CubicDirection toDirection(int side) {
        return switch (side) {
            case 0 -> CubicDirection.EAST;
            case 1 -> CubicDirection.WEST;
            case 2 -> CubicDirection.NORTH;
            case 3 -> CubicDirection.SOUTH;
            case 4 -> CubicDirection.UP;
            case 5 -> CubicDirection.DOWN;
            default -> throw new IllegalStateException("Unexpected value: " + side);
        };
    }

    Mesh quad(final Vector3 bottomLeft,
              final Vector3 topLeft,
              final Vector3 topRight,
              final Vector3 bottomRight,
              final int width,
              final int height,
              final Quad voxel,
              final boolean backFace) {

        final Vector3[] vertices = new Vector3[4];

        vertices[2] = new Vector3(topLeft).scl(VOXEL_SIZE);
        vertices[3] = new Vector3(topRight).scl(VOXEL_SIZE);
        vertices[0] = new Vector3(bottomLeft).scl(VOXEL_SIZE);
        vertices[1] = new Vector3(bottomRight).scl(VOXEL_SIZE);

        final short[] indexes = backFace ? new short[]{2, 0, 1, 1, 3, 2} : new short[]{2, 3, 1, 1, 0, 2};

        float[] colorArray = new float[4 * 4];
        float blockLight = world.getBlockLight((int) bottomLeft.x, (int) bottomLeft.y, (int) bottomLeft.z) / 15.0f;
        float sunLight = world.getSunlight((int) bottomLeft.x, (int) bottomLeft.y, (int) bottomLeft.z) / 15.0f;
        float color = blockLight;

        for (int i = 0; i < colorArray.length; i += 4) {
            colorArray[i] = color;
            colorArray[i + 1] = color;
            colorArray[i + 2] = color;
            colorArray[i + 3] = sunLight;
        }

        return createMesh(vertices, colorArray, indexes);
    }

    private static @NotNull Mesh createMesh(Vector3[] vertices, float[] colorArray, short[] indexes) {
        MeshBuilder mesh = new MeshBuilder();
        mesh.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked, GL20.GL_TRIANGLES);

        mesh.vertex(vertices[0], Vector3.Zero, new Color(colorArray[0], colorArray[1], colorArray[2], colorArray[3]), Vector2.Zero);
        mesh.vertex(vertices[1], Vector3.Zero, new Color(colorArray[4], colorArray[5], colorArray[6], colorArray[7]), Vector2.Zero);
        mesh.vertex(vertices[2], Vector3.Zero, new Color(colorArray[8], colorArray[9], colorArray[10], colorArray[11]), Vector2.Zero);
        mesh.vertex(vertices[3], Vector3.Zero, new Color(colorArray[12], colorArray[13], colorArray[14], colorArray[15]), Vector2.Zero);

        return mesh.end();
    }

    @Override
    public void meshVoxels(ModelBuilder builder, MeshBuilder meshBuilder, UseCondition condition) {
        this.greedy(builder, meshBuilder, condition);
    }

    private class Quad {
        public final BlockState blockState;
        public int side;

        public Quad(BlockState blockState, int side) {
            this.blockState = blockState;
            this.side = side;
        }

        public BlockState getBlockProperties() {
            return blockState;
        }

        public int getSide() {
            return side;
        }
    }

    // You need to provide a ShaderProgram and constants for VOXEL_SIZE, WEST, EAST, BOTTOM, TOP, SOUTH, NORTH
}
