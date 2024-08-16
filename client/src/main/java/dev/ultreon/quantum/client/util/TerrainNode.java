/*
Copyright 2024 Kevin James

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.libs.commons.v0.vector.Vec3i;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockProperties;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.collection.FlatStorage;
import dev.ultreon.quantum.util.PosOutOfBoundsException;
import dev.ultreon.quantum.world.BlockVec;
import dev.ultreon.quantum.world.ChunkVec;
import dev.ultreon.quantum.world.HeightMap;

public class TerrainNode implements Disposable, RenderableProvider, ClientChunkAccess {
    public static final int TERRAIN_SIZE = 16;

    static int nodes = 0;

    public int size;
    public int x, y, z;
    public int idx;

    public TerrainNode parent;
    public TerrainNode[] children = new TerrainNode[8];

    public FlatStorage<BlockProperties> materials = new FlatStorage<>(TERRAIN_SIZE * TERRAIN_SIZE * TERRAIN_SIZE, BlockProperties.AIR);

    public ModelInstance modelInstance;
    public Mesh mesh;
    public boolean generated = false;
    public boolean meshBuilt = false;

    private final Object obj = new Object();

    private final Array<Vector3> vertices = new Array<>();
    private final Array<Color> colors = new Array<>();
    private final Array<Short> indices = new Array<>();
    private final Array<Vector3> normals = new Array<>();
    private final HeightMap heightMap = new HeightMap(TERRAIN_SIZE);
    private final ClientWorldAccess voxelWorld;
    private boolean disposed = false;

    public TerrainNode(int size, int x, int y, int z, TerrainNode parent, int idx) {
        this(size, x, y, z, parent, idx, null);
    }

    public TerrainNode(int size, int x, int y, int z, TerrainNode parent, int idx, ClientWorldAccess voxelWorld) {
        this.size = size;
        this.x = x;
        this.y = y;
        this.z = z;
        this.parent = parent;
        this.idx = idx;
        this.voxelWorld = voxelWorld != null ? voxelWorld : parent.voxelWorld;

        if (this.voxelWorld == null) {
            throw new IllegalStateException("Voxel world not found in parent node or passed in");
        }
        ModelBuilder modelBuilder = new ModelBuilder();
        modelInstance = new ModelInstance(modelBuilder.createBox(1, 1, 1,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal));
        modelInstance.transform.setTranslation(x, y, z);

        synchronized (VoxelTerrain.GENERATING_QUEUE) {
            VoxelTerrain.GENERATING_QUEUE.add(this);
        }
    }

    public static int getNodes() {
        return nodes;
    }

    public Block getMaterial(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= TERRAIN_SIZE || y >= TERRAIN_SIZE || z >= TERRAIN_SIZE) return Blocks.AIR;
        BlockProperties blockProperties = materials.get(x + y * TERRAIN_SIZE + z * TERRAIN_SIZE * TERRAIN_SIZE);
        if (blockProperties == null) return Blocks.AIR;
        return blockProperties.getBlock();
    }

    /**
     * Generates the terrain
     */
    public void generate() {
        float blockSize = size / (float) TERRAIN_SIZE;

        for (int x = 0; x < TERRAIN_SIZE; x++) {
            for (int y = 0; y < TERRAIN_SIZE; y++) {
                for (int z = 0; z < TERRAIN_SIZE; z++) {
                    float wx = x * blockSize + this.x;
                    float wy = y * blockSize + this.y;
                    float wz = z * blockSize + this.z;

                    double noise = VoxelTerrain.noise.eval(wx / 8.0f, wy / 8.0f, wz / 8.0f) + ((wy - 256) / 512.0f);
                    if (noise < 0) {
                        materials.set(x + y * TERRAIN_SIZE + z * TERRAIN_SIZE * TERRAIN_SIZE, Blocks.DIRT.createMeta());
                    }
                }
            }
        }

        int index = 0;
        for (int x = 0; x < TERRAIN_SIZE; x++) {
            for (int y = 0; y < TERRAIN_SIZE; y++) {
                for (int z = 0; z < TERRAIN_SIZE; z++) {
                    float wx = x * blockSize + this.x;
                    float wy = y * blockSize + this.y;
                    float wz = z * blockSize + this.z;
                    if (!getMaterial(x, y, z).isAir()) {
                        if (!getMaterial(x, y + 1, z).isAir()) {
                            vertices.add(new Vector3(wx, wy + blockSize, wz));
                            vertices.add(new Vector3(wx, wy + blockSize, wz + blockSize));
                            vertices.add(new Vector3(wx + blockSize, wy + blockSize, wz + blockSize));
                            vertices.add(new Vector3(wx + blockSize, wy + blockSize, wz));

                            Color color = new Color(1, 0, 0, 1);

                            colors.add(color);
                            colors.add(color);
                            colors.add(color);
                            colors.add(color);

                            Vector3 normal = new Vector3(0, 1, 0);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);

                            indices.add((short) index);
                            indices.add((short) (index + 1));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 3));
                            indices.add((short) index);

                            index += 4;
                        }

                        if (getMaterial(x, y - 1, z).isAir()) {
                            vertices.add(new Vector3(wx, wy, wz + blockSize));
                            vertices.add(new Vector3(wx, wy, wz));
                            vertices.add(new Vector3(wx + blockSize, wy, wz));
                            vertices.add(new Vector3(wx + blockSize, wy, wz + blockSize));

                            Color color = new Color(0, 1, 0, 1);

                            colors.add(color);
                            colors.add(color);
                            colors.add(color);
                            colors.add(color);

                            Vector3 normal = new Vector3(0, -1, 0);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);

                            indices.add((short) index);
                            indices.add((short) (index + 1));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 3));
                            indices.add((short) index);

                            index += 4;
                        }

                        if (getMaterial(x - 1, y, z).isAir()) {
                            vertices.add(new Vector3(wx, wy, wz + blockSize));
                            vertices.add(new Vector3(wx, wy + blockSize, wz + blockSize));
                            vertices.add(new Vector3(wx, wy + blockSize, wz));
                            vertices.add(new Vector3(wx, wy, wz));

                            Color color = new Color(0, 0, 1, 1);

                            colors.add(color);
                            colors.add(color);
                            colors.add(color);
                            colors.add(color);

                            Vector3 normal = new Vector3(-1, 0, 0);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);

                            indices.add((short) index);
                            indices.add((short) (index + 1));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 3));
                            indices.add((short) index);

                            index += 4;
                        }

                        if (getMaterial(x + 1, y, z).isAir()) {
                            vertices.add(new Vector3(wx + blockSize, wy, wz));
                            vertices.add(new Vector3(wx + blockSize, wy + blockSize, wz));
                            vertices.add(new Vector3(wx + blockSize, wy + blockSize, wz + blockSize));
                            vertices.add(new Vector3(wx + blockSize, wy, wz + blockSize));

                            Color color = new Color(1, 1, 0, 1);

                            colors.add(color);
                            colors.add(color);
                            colors.add(color);
                            colors.add(color);

                            Vector3 normal = new Vector3(1, 0, 0);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);

                            indices.add((short) index);
                            indices.add((short) (index + 1));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 3));
                            indices.add((short) index);

                            index += 4;
                        }

                        if (getMaterial(x, y, z - 1).isAir()) {
                            vertices.add(new Vector3(wx, wy, wz));
                            vertices.add(new Vector3(wx, wy + blockSize, wz));
                            vertices.add(new Vector3(wx + blockSize, wy + blockSize, wz));
                            vertices.add(new Vector3(wx + blockSize, wy, wz));

                            Color color = new Color(1, 0, 1, 1);

                            colors.add(color);
                            colors.add(color);
                            colors.add(color);
                            colors.add(color);

                            Vector3 normal = new Vector3(0, 0, -1);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);

                            indices.add((short) index);
                            indices.add((short) (index + 1));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 3));
                            indices.add((short) index);

                            index += 4;
                        }

                        if (getMaterial(x, y, z + 1).isAir()) {
                            vertices.add(new Vector3(wx + blockSize, wy, wz + blockSize));
                            vertices.add(new Vector3(wx + blockSize, wy + blockSize, wz + blockSize));
                            vertices.add(new Vector3(wx, wy + blockSize, wz + blockSize));
                            vertices.add(new Vector3(wx, wy, wz + blockSize));

                            Color color = new Color(0, 1, 1, 1);

                            colors.add(color);
                            colors.add(color);
                            colors.add(color);
                            colors.add(color);

                            Vector3 normal = new Vector3(0, 0, 1);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);
                            normals.add(normal);

                            indices.add((short) index);
                            indices.add((short) (index + 1));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 2));
                            indices.add((short) (index + 3));
                            indices.add((short) index);

                            index += 4;
                        }
                    }
                }
            }
        }

        generated = true;
    }

    public void buildMesh() {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder meshPartBuilder = modelBuilder.part("terrain", GL20.GL_LINES,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)));

        for (int i = 0; i < vertices.size; i++) {
            meshPartBuilder.vertex(vertices.get(i), normals.get(i), colors.get(i), Vector2.Zero);
        }

        for (int i = 0; i < indices.size; i += 3) {
            meshPartBuilder.index(indices.get(i), indices.get(i + 1), indices.get(i + 2));
        }

        mesh = modelBuilder.end().meshParts.get(0).mesh;
        modelInstance.model.meshes.add(mesh);

        vertices.clear();
        indices.clear();
        colors.clear();
        normals.clear();

        meshBuilt = true;
    }

    public void update(int x, int y, int z) {
        int update_size = size;
        Rectangle my_rect = new Rectangle(this.x, this.y, size, size);
        Rectangle my_rect2 = new Rectangle(this.z, this.y, size, size);

        Rectangle p_rect = new Rectangle(x - update_size, y - update_size, update_size * 2, update_size * 2);
        Rectangle p_rect2 = new Rectangle(z - update_size, y - update_size, update_size * 2, update_size * 2);

        if ((my_rect.overlaps(p_rect)) && my_rect2.overlaps(p_rect2) && size > TERRAIN_SIZE) {
            int half_size = size / 2;
            // split!
            for (int i = 0; i <= 1; i++) {
                for (int j = 0; j <= 1; j++) {
                    for (int k = 0; k <= 1; k++) {
                        int X = this.x + half_size * i;
                        int Y = this.y + half_size * j;
                        int Z = this.z + half_size * k;

                        int idx = i + j * 2 + k * 4;
                        if (children[idx] == null) {
                            children[idx] = new TerrainNode(half_size, X, Y, Z, this, idx);
                            nodes++;
                        }

                    }
                }
            }
        } else {
            for (int i = 0; i < children.length; i++) {
                if (children[i] != null) {
                    synchronized (children[i].obj) {
                        children[i].dispose();
                    }
                    nodes--;
                }
                children[i] = null;
            }
        }

        for (TerrainNode child : children) {
            if (child != null) {
                child.update(x, y, z);
            }
        }
    }

    public void updateVisibility() {
        modelInstance.model.meshes.clear();
        modelInstance.model.meshes.add(mesh);

        int built = 0;
        for (TerrainNode child : children) {
            if (child != null) {
                if (child.meshBuilt) {
                    built++;
                }
                child.updateVisibility();
            }
        }
        if (built == children.length) {
            modelInstance.model.meshes.add(mesh);
        }
    }

    @Override
    public void dispose() {
        if (disposed) return;

        disposed = true;
        if (mesh != null) {
            mesh.dispose();
        }
        if (modelInstance != null) {
            RenderLayer.WORLD.destroy(modelInstance);
        }
        for (TerrainNode child : children) {
            if (child != null) {
                child.dispose();
            }
        }

        mesh = null;
        modelInstance = null;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (modelInstance != null) {
            modelInstance.getRenderables(renderables, pool);
        }

        for (TerrainNode child : children) {
            if (child != null) {
                child.getRenderables(renderables, pool);
            }
        }
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    @Override
    public ClientWorldAccess getWorld() {
        return voxelWorld;
    }

    @Override
    public int getSunlight(Vec3i pos) {
        return 15;
    }

    @Override
    public int getBlockLight(Vec3i pos) {
        return 0;
    }

    @Override
    public Vector3 getRenderOffset() {
        return new Vector3(0, 0, 0);
    }

    @Override
    public float getBrightness(int lightLevel) {
        return 1;
    }

    @Override
    public ModelInstance addModel(BlockVec blockVec, ModelInstance modelInstance) {
        // TODO
        return modelInstance;
    }

    @Override
    public BlockProperties get(Vec3i tmp3i) {
        return materials.get(tmp3i.x + tmp3i.y * TERRAIN_SIZE + tmp3i.z * TERRAIN_SIZE * TERRAIN_SIZE);
    }

    public Mesh getMesh() {
        return mesh;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public boolean isMeshBuilt() {
        return meshBuilt;
    }

    public void setMeshBuilt(boolean meshBuilt) {
        this.meshBuilt = meshBuilt;
    }

    @Override
    public float getLightLevel(int x, int y, int z) throws PosOutOfBoundsException {
        return 15;
    }

    @Override
    public float getSunlightLevel(int x, int y, int z) {
        return 15;
    }

    @Override
    public float getBlockLightLevel(int x, int y, int z) {
        return 0;
    }

    @Override
    public ChunkVec getPos() {
        return new ChunkVec(x, y, z);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void revalidate() {
        this.updateVisibility();
    }

    @Override
    public boolean setFast(int x, int y, int z, BlockProperties block) {
        return materials.set(x + y * TERRAIN_SIZE + z * TERRAIN_SIZE * TERRAIN_SIZE, block);
    }

    @Override
    public boolean set(int x, int y, int z, BlockProperties block) {
        return materials.set(x + y * TERRAIN_SIZE + z * TERRAIN_SIZE * TERRAIN_SIZE, block);
    }

    @Override
    public BlockProperties getFast(int x, int y, int z) {
        return materials.get(x + y * TERRAIN_SIZE + z * TERRAIN_SIZE * TERRAIN_SIZE);
    }

    @Override
    public BlockProperties get(int x, int y, int z) {
        return materials.get(x + y * TERRAIN_SIZE + z * TERRAIN_SIZE * TERRAIN_SIZE);
    }

    @Override
    public Vec3i getOffset() {
        return new Vec3i(x, y, z);
    }

    @Override
    public int getHighest(int x, int z) {
        return heightMap.get(x, z);
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
