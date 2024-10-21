package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;

public class GreedyMesherWithCulling implements Mesher {
    private final ClientChunk chunk;
    private final QuantumClient client;

    public GreedyMesherWithCulling(ClientChunk chunk, QuantumClient client) {
        this.chunk = chunk;
        this.client = client;
    }

    public MeshData generateMesh(MeshData meshData) {
        boolean[][][] processed = new boolean[World.CHUNK_SIZE][World.CHUNK_SIZE][World.CHUNK_SIZE];

        // Loop through each block in the chunk
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (processed[x][y][z]) continue;

                    BlockState blockState = chunk.get(x, y, z);
                    if (blockState.isAir()) continue;

                    // Process each face (north, south, east, west, up, down)
                    for (Direction direction : Direction.values()) {
                        if (isFaceVisible(chunk, x, y, z, direction)) {
                            // Greedy merge for visible faces in the current direction
                            int width = findWidth(chunk, x, y, z, direction);
                            int height = findHeight(chunk, x, y, z, direction);

                            addQuad(meshData, x, y, z, width, height, direction, blockState);
                            markProcessed(processed, x, y, z, width, height, direction);
                        }
                    }
                }
            }
        }

        return meshData;
    }

    private boolean isFaceVisible(ClientChunk chunk, int x, int y, int z, Direction direction) {
        // Check neighboring block in the given direction
        int nx = x + direction.getOffsetX();
        int ny = y + direction.getOffsetY();
        int nz = z + direction.getOffsetZ();

        BlockState neighbor = chunk.get(nx, ny, nz);
        return neighbor.isAir();  // Only consider air or null blocks as transparent
    }

    private int findWidth(ClientChunk chunk, int x, int y, int z, Direction direction) {
        BlockState blockState = chunk.get(x, y, z);
        int width = 0;

        while (canMerge(chunk, x + width * direction.getOffsetX(), y, z, blockState, direction)) {
            width++;
        }

        return width;
    }

    private int findHeight(ClientChunk chunk, int x, int y, int z, Direction direction) {
        int height = 0;
        
        while (canMerge(chunk, x, y + height * direction.getOffsetY(), z, chunk.get(x, y, z), direction)) {
            height++;
        }

        return height;
    }

    private boolean canMerge(ClientChunk chunk, int x, int y, int z, BlockState blockState, Direction direction) {
        // Ensure the block is the same type and face in the same direction
        if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
            return false;
        }

        return blockState.equals(chunk.get(x, y, z)) && isFaceVisible(chunk, x, y, z, direction);
    }

    private void addQuad(MeshData meshData, int x, int y, int z, int width, int height, Direction direction, BlockState blockState) {
        // Add vertices and indices for the quad in the given direction
        BlockModel blockModel = client.getBlockModel(blockState);
        if (blockModel instanceof BakedCubeModel bakedCubeModel) {
            meshData.addQuad(x, y, z, width, height, direction, bakedCubeModel.tex(direction));
        }
    }

    private void markProcessed(boolean[][][] processed, int x, int y, int z, int width, int height, Direction direction) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int nx = x + i * direction.getOffsetX();
                int ny = y + j * direction.getOffsetY();
                processed[nx][ny][z] = true;
            }
        }
    }

    @Override
    public void buildMesh(UseCondition condition, MeshPartBuilder builder) {
        MeshData meshData = new MeshData(builder);
        generateMesh(meshData);
    }
}