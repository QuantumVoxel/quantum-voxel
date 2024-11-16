package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.registry.BlockRenderTypeRegistry;
import dev.ultreon.quantum.client.registry.BlockRendererRegistry;
import dev.ultreon.quantum.client.render.BlockRenderer;
import dev.ultreon.quantum.client.render.NormalBlockRenderer;
import dev.ultreon.quantum.client.world.ClientChunkAccess;
import dev.ultreon.quantum.client.world.ClientWorldAccess;
import dev.ultreon.quantum.util.PosOutOfBoundsException;
import dev.ultreon.quantum.util.Vec3i;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import dev.ultreon.quantum.world.vec.BlockVecSpace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.ultreon.quantum.world.World.CHUNK_SIZE;

/**
 * Mesher using the "greedy meshing" technique.
 * <p>
 * Similar to the method described by Mikola Lysenko at <a href="http://0fps.net/2012/06/30/meshing-in-a-client-game/">0fps.net (Meshing in a client game)</a>
 * <p>
 * Goes through each direction and attempts to merge multiple faces into rectangles quickly in one pass.
 * <p>
 * Begins at the origin, the steps one block at a time horizontal. Keeps going horizontal until the next block reached
 * would not render the same as the block that was started with, or the next block is dirty.
 * When a different or already used block is found, the horizontal line stops and a vertical march begins.
 * Vertical stepping occurs until one of the blocks in the next row would not render the same as the initial block,
 * or one of the blocks in the next row is dirty. When an incompatible row is found, the marching stops, and a rectangle is completed.
 * All of the blocks in the completed rectangle are marked as dirty and the rectangle is used as a face.
 * This process is repeated with the origin at the next non-dirty block until there are no more dirty blocks on the face.
 * <p>
 * Example:
 * <p>
 * Source
 * <pre>
 *  [][][] [][][]
 *  [][]   [][][]
 *  []
 * </pre>
 * <p>
 * Result
 * <pre>
 *  [   ]  |---|
 *  [  ]   |___|
 *  []
 * </pre>
 */
public class GreedyMesher implements Mesher {

    private static final int OFF_X = 0;
    private static final int OFF_Z = 0;
    private static final int OFF_Y = 0;
    private final @NotNull ClientChunkAccess chunk;
    private final boolean perCornerLight;
    private final Vec3i tmp3i = new Vec3i();

    /**
     * @param chunk          Chunk to mesh
     * @param perCornerLight Whether to average light on a per-corner basis
     */
    public GreedyMesher(@NotNull ClientChunkAccess chunk, boolean perCornerLight) {
        this.chunk = chunk;
        this.perCornerLight = perCornerLight;
    }

    /**
     * Generates a list of visible faces for the chunk mesh based on the given conditions.
     *
     * @param condition   Condition to determine the use of a face.
     * @param ocCond      Condition to determine if a block face should be occluded.
     * @param shouldMerge Condition to determine if adjacent faces should be merged.
     * @return List of visible faces for the chunk mesh.
     */
    public List<Face> getFaces(UseCondition condition, OccludeCondition ocCond, MergeCondition shouldMerge) {
        List<Face> faces = new ArrayList<>();

        int width = CHUNK_SIZE;
        int depth = CHUNK_SIZE;
        int height = CHUNK_SIZE;

        for (int y = 0; y < height; y++) {
            boolean[][] topMask = new boolean[width][depth];
            PerCornerLightData[][] topPcld = this.perCornerLight ? new PerCornerLightData[width][depth] : null;

            boolean[][] btmMask = new boolean[width][depth];
            PerCornerLightData[][] btmPcld = this.perCornerLight ? new PerCornerLightData[width][depth] : null;

            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    try {
                        if (!topMask[x][z] || !btmMask[x][z]) {
                            facesY(condition, ocCond, x, y, z, topMask, topPcld, btmMask, btmPcld);
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                        break;
                    }
                }
            }

            applyGreedyMeshing(faces, Direction.UP, topMask, topPcld, y, shouldMerge);
            applyGreedyMeshing(faces, Direction.DOWN, btmMask, btmPcld, y, shouldMerge);
        }

        for (int x = 0; x < width; x++) {
            boolean[][] westMask = new boolean[depth][height];
            PerCornerLightData[][] westPcld = this.perCornerLight ? new PerCornerLightData[depth][height] : null;

            boolean[][] eastMask = new boolean[depth][height];
            PerCornerLightData[][] eastPcld = this.perCornerLight ? new PerCornerLightData[depth][height] : null;

            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    try {
                        if (!westMask[z][y] || !eastMask[z][y]) {
                            facesX(condition, ocCond, x, y, z, westMask, westPcld, eastMask, eastPcld);
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                        break;
                    }
                }
            }

            applyGreedyMeshing(faces, Direction.EAST, eastMask, eastPcld, x, shouldMerge);
            applyGreedyMeshing(faces, Direction.WEST, westMask, westPcld, x, shouldMerge);
        }

        for (int z = 0; z < depth; z++) {
            boolean[][] northMask = new boolean[width][height];
            PerCornerLightData[][] northPcld = this.perCornerLight ? new PerCornerLightData[width][height] : null;

            boolean[][] southMask = new boolean[width][height];
            PerCornerLightData[][] southPcld = this.perCornerLight ? new PerCornerLightData[width][height] : null;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    try {
                        if (!northMask[x][y] || !southMask[x][y]) {
                            facesZ(condition, ocCond, x, y, z, northMask, northPcld, southMask, southPcld);
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                        break;
                    }
                }
            }

            applyGreedyMeshing(faces, Direction.NORTH, northMask, northPcld, z, shouldMerge);
            applyGreedyMeshing(faces, Direction.SOUTH, southMask, southPcld, z, shouldMerge);
        }

        return faces;
    }

    // Common meshing logic
    private void applyGreedyMeshing(List<Face> faces, Direction direction, boolean[][] mask, PerCornerLightData[][] pcld, int index, MergeCondition shouldMerge) {
        this.greedy(faces, direction, shouldMerge, mask, pcld, index);
    }

    private boolean facesY(UseCondition condition, OccludeCondition ocCond, int x, int y, int z, boolean[][] topMask, PerCornerLightData[][] topPcld, boolean[][] btmMask, PerCornerLightData[][] btmPcld) {
        BlockState curBlock = this.block(this.chunk, x, y, z);
        if (curBlock == null) return true;

        BlockModel blockModel = BlockModelRegistry.get().get(curBlock);
        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
            this.chunk.addModel(new BlockVec(x, y, z, BlockVecSpace.CHUNK), new ModelInstance(blockModel.getModel()));
            return true;
        }

        if (!condition.shouldUse(curBlock.getBlock())) return true;

        int bottomNeighborY = y - 1;
        int topNeighborY = y + 1;

        ClientChunkAccess bottomNeighborChunk = this.chunk;
        ClientChunkAccess topNeighborChunk = this.chunk;

        // Cache the chunk coordinates
        int chunkX = this.chunk.getVec().getIntX();
        int chunkY = this.chunk.getVec().getIntY();
        int chunkZ = this.chunk.getVec().getIntZ();

        // Get neighboring chunks if needed
        if (bottomNeighborY < 0) {
            bottomNeighborChunk = this.chunk.getWorld().getChunk(chunkX, chunkY - 1, chunkZ);
            bottomNeighborY += CHUNK_SIZE;
        }
        if (topNeighborY >= CHUNK_SIZE) {
            topNeighborChunk = this.chunk.getWorld().getChunk(chunkX, chunkY + 1, chunkZ);
            topNeighborY -= CHUNK_SIZE;
        }

        // Check top neighbor
        if (topNeighborChunk != null && !topMask[x][z]) {
            BlockState topNeighborBlk = topNeighborChunk.get(x, topNeighborY, z);
            if (!ocCond.shouldOcclude(curBlock.getBlock(), topNeighborBlk.getBlock())) {
                topMask[x][z] = true;

                if (this.perCornerLight) {
                    topPcld[x][z] = calculatePerCornerLightAndSunlight(Direction.UP, x, y, z);
                }
            }
        }

        // Check bottom neighbor
        if (bottomNeighborChunk != null && !btmMask[x][z]) {
            BlockState bottomNeighborBlk = bottomNeighborChunk.get(x, bottomNeighborY, z);
            if (!ocCond.shouldOcclude(curBlock.getBlock(), bottomNeighborBlk.getBlock())) {
                btmMask[x][z] = true;

                if (this.perCornerLight) {
                    btmPcld[x][z] = calculatePerCornerLightAndSunlight(Direction.DOWN, x, y, z);
                }
            }
        }

        return false;
    }

    private boolean facesX(UseCondition condition, OccludeCondition ocCond, int x, int y, int z, boolean[][] westMask, PerCornerLightData[][] westPcld, boolean[][] eastMask, PerCornerLightData[][] eastPcld) {
        BlockState curBlock = this.block(this.chunk, x, y, z);
        if (curBlock == null) return true;

        BlockModel blockModel = BlockModelRegistry.get().get(curBlock);
        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
            return true;
        }

        if (!condition.shouldUse(curBlock.getBlock())) return true;

        int westNeighborX = x - 1;
        int eastNeighborX = x + 1;

        ClientChunkAccess westNeighborChunk = this.chunk;
        ClientChunkAccess eastNeighborChunk = this.chunk;

        // Cache chunk coordinates
        int chunkX = this.chunk.getVec().getIntX();
        int chunkY = this.chunk.getVec().getIntY();
        int chunkZ = this.chunk.getVec().getIntZ();

        // Get neighboring chunks if needed
        if (westNeighborX < 0) {
            westNeighborChunk = this.chunk.getWorld().getChunk(chunkX - 1, chunkY, chunkZ);
            westNeighborX += CHUNK_SIZE;
        }
        if (eastNeighborX >= CHUNK_SIZE) {
            eastNeighborChunk = this.chunk.getWorld().getChunk(chunkX + 1, chunkY, chunkZ);
            eastNeighborX -= CHUNK_SIZE;
        }

        // Check west neighbor
        if (westNeighborChunk != null && !westMask[z][y]) {
            BlockState westNeighborBlk = westNeighborChunk.get(westNeighborX, y, z);
            if (!ocCond.shouldOcclude(curBlock.getBlock(), westNeighborBlk.getBlock())) {
                westMask[z][y] = true;

                if (this.perCornerLight) {
                    westPcld[z][y] = calculatePerCornerLightAndSunlight(Direction.WEST, x, y, z);
                }
            }
        }

        // Check east neighbor
        if (eastNeighborChunk != null && !eastMask[z][y]) {
            BlockState eastNeighborBlk = eastNeighborChunk.get(eastNeighborX, y, z);
            if (!ocCond.shouldOcclude(curBlock.getBlock(), eastNeighborBlk.getBlock())) {
                eastMask[z][y] = true;

                if (this.perCornerLight) {
                    eastPcld[z][y] = calculatePerCornerLightAndSunlight(Direction.EAST, x, y, z);
                }
            }
        }

        return false;
    }

    private boolean facesZ(UseCondition condition, OccludeCondition ocCond, int x, int y, int z, boolean[][] northMask, PerCornerLightData[][] northPcld, boolean[][] southMask, PerCornerLightData[][] southPcld) {
        BlockState curBlock = this.block(this.chunk, x, y, z);
        if (curBlock == null) return true;

        BlockModel blockModel = BlockModelRegistry.get().get(curBlock);
        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
            return true;
        }

        if (!condition.shouldUse(curBlock.getBlock())) return true;

        int northNeighborZ = z + 1;
        int southNeighborZ = z - 1;

        ClientChunkAccess northNeighborChunk = this.chunk;
        ClientChunkAccess southNeighborChunk = this.chunk;

        // Cache chunk coordinates
        int chunkX = this.chunk.getVec().getIntX();
        int chunkY = this.chunk.getVec().getIntY();
        int chunkZ = this.chunk.getVec().getIntZ();

        // Get neighboring chunks if needed
        if (northNeighborZ >= CHUNK_SIZE) {
            northNeighborChunk = this.chunk.getWorld().getChunk(chunkX, chunkY, chunkZ + 1);
            northNeighborZ -= CHUNK_SIZE;
        }
        if (southNeighborZ < 0) {
            southNeighborChunk = this.chunk.getWorld().getChunk(chunkX, chunkY, chunkZ - 1);
            southNeighborZ += CHUNK_SIZE;
        }

        // Check north neighbor
        if (northNeighborChunk != null && !northMask[x][y]) {
            BlockState northNeighborBlock = northNeighborChunk.get(x, y, northNeighborZ);
            if (!ocCond.shouldOcclude(curBlock.getBlock(), northNeighborBlock.getBlock())) {
                northMask[x][y] = true;

                if (this.perCornerLight) {
                    northPcld[x][y] = calculatePerCornerLightAndSunlight(Direction.NORTH, x, y, z);
                }
            }
        }

        // Check south neighbor
        if (southNeighborChunk != null && !southMask[x][y]) {
            BlockState southNeighborBlock = southNeighborChunk.get(x, y, southNeighborZ);
            if (!ocCond.shouldOcclude(curBlock.getBlock(), southNeighborBlock.getBlock())) {
                southMask[x][y] = true;

                if (this.perCornerLight) {
                    southPcld[x][y] = calculatePerCornerLightAndSunlight(Direction.SOUTH, x, y, z);
                }
            }
        }

        return false;
    }

    private PerCornerLightData calculatePerCornerLightAndSunlight(Direction dir, int x, int y, int z) {
        PerCornerLightData lightData = new PerCornerLightData();
        lightData.l00 = this.calcPerCornerLight(dir, x, y, z);
        lightData.l01 = this.calcPerCornerLight(dir, x, y, z + 1);
        lightData.l10 = this.calcPerCornerLight(dir, x + 1, y, z);
        lightData.l11 = this.calcPerCornerLight(dir, x + 1, y, z + 1);
        lightData.s00 = this.calcPerCornerSunlight(dir, x, y, z);
        lightData.s01 = this.calcPerCornerSunlight(dir, x, y, z + 1);
        lightData.s10 = this.calcPerCornerSunlight(dir, x + 1, y, z);
        lightData.s11 = this.calcPerCornerSunlight(dir, x + 1, y, z + 1);
        return lightData;
    }

    private float calcLightLevel(Direction side, int x, int y, int z) throws PosOutOfBoundsException {
        LightLevelData result = this.calcLightLevels(side, x, y, z);
        if (result == null) return 1;

        return Math.min(1, result.sunBrightness() + result.blockBrightness());
    }

    @Nullable
    private LightLevelData calcLightLevels(Direction side, int x, int y, int z) {
        if (this.chunk == null) return null;  // Early exit if chunk is null

        // Adjust coordinates based on the side direction
        switch (side) {
            case UP -> y++;
            case DOWN -> y--;
            case WEST -> x--;
            case EAST -> x++;
            case NORTH -> z++;
            case SOUTH -> z--;
        }

        ClientWorldAccess world = this.chunk.getWorld();

        // Adjust coordinates if out of chunk bounds
        ClientChunkAccess sChunk = adjustCoordinatesForChunk(world, x, y, z);

        if (sChunk == null) return null;  // Early exit if neighboring chunk is not found

        // Calculate brightness
        float sunBrightness = sChunk.getBrightness(this.sunlight(sChunk, x, y, z));
        float blockBrightness = sChunk.getBrightness(this.blockLight(sChunk, x, y, z));

        return new LightLevelData(blockBrightness, sunBrightness);
    }

    @Nullable
    private ClientChunkAccess adjustCoordinatesForChunk(ClientWorldAccess world, int x, int y, int z) {
        int chunkSize = CHUNK_SIZE;

        if (x < 0 || x >= chunkSize) {
            x = (x + chunkSize) % chunkSize;
            return world.getChunk(GreedyMesher.OFF_X + (x < 0 ? -1 : 1), GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
        }
        if (y < 0 || y >= chunkSize) {
            y = (y + chunkSize) % chunkSize;
            return world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + (y < 0 ? -1 : 1), GreedyMesher.OFF_Z + z);
        }
        if (z < 0 || z >= chunkSize) {
            z = (z + chunkSize) % chunkSize;
            return world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + (z < 0 ? -1 : 1));
        }

        return this.chunk;  // Default to current chunk if still within bounds
    }

    private boolean shouldNotRenderNormally(Block blockToBlockFace) {
        return blockToBlockFace == null || !blockToBlockFace.doesRender() || blockToBlockFace.hasCustomRender();
    }

    private static boolean isFullCubeRender(Block id2) {
        return BlockRendererRegistry.get(id2).getClass() == NormalBlockRenderer.class;
    }

    /**
     * @param outputList List to put faces in
     * @param side       BlockFace being meshed
     * @param z          Depth on the plane
     */
    private void greedy(List<Face> outputList, Direction side, MergeCondition mergeCond, boolean[][] mask, PerCornerLightData[][] lightDatas, int z) {
        int width = mask.length;
        int height = mask[0].length;
        boolean[][] used = new boolean[width][height];  // Reuse existing mask sizes for used array

        // Loop through each y, x coordinate
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!mask[x][y] || used[x][y]) continue;  // Skip if already processed or not in mask

                // Calculate real-world coordinates for the current face
                int realX = this.realX(side, x, z);
                int realY = this.realY(side, y, z);
                int realZ = this.realZ(side, x, y, z);

                BlockState block = this.block(this.chunk, realX, realY, realZ);
                if (block == null || block.isAir()) continue;  // Skip if air or null block

                used[x][y] = true;

                // Light level handling
                float ll = 1.0f;
                PerCornerLightData lightData = null;
                if (this.perCornerLight) {
                    lightData = lightDatas[x][y];
                } else {
                    ll = this.calcLightLevel(side, realX, realY, realZ);
                }

                int endX = x + 1, endY = y + 1;

                // Horizontal (x-axis) expansion
                while (endX < width) {
                    if (!canMerge(mergeCond, block, ll, lightData, side, endX, y, z, lightDatas, used)) break;
                    used[endX][y] = true;
                    endX++;
                }

                // Vertical (y-axis) expansion
                while (endY < height) {
                    if (!canExpandVertically(mergeCond, block, ll, lightData, side, x, endX, endY, z, lightDatas, used))
                        break;
                    for (int lx = x; lx < endX; lx++) {
                        used[lx][endY] = true;
                    }
                    endY++;
                }

                // Add the resulting face to the output list
                outputList.add(new Face(side, block, ll, lightData,
                        x + GreedyMesher.OFF_X, y + GreedyMesher.OFF_Y,
                        endX + GreedyMesher.OFF_X, endY + GreedyMesher.OFF_Y,
                        z + GreedyMesher.OFF_Z, 1));
            }
        }
    }

    private boolean canMerge(MergeCondition mergeCond, BlockState block, float ll, PerCornerLightData lightData, Direction side, int newX, int y, int z, PerCornerLightData[][] lightDatas, boolean[][] used) {
        int realX = this.realX(side, newX, z);
        int realY = this.realY(side, y, z);
        int realZ = this.realZ(side, newX, y, z);

        if (used[newX][y]) return false;  // If already used, cannot merge

        BlockState newBlock = this.block(this.chunk, realX, realY, realZ);
        if (newBlock == null || newBlock.isAir()) return false;

        float newLight = 15;
        PerCornerLightData newPcld = null;

        if (this.perCornerLight) {
            newPcld = lightDatas[newX][y];
        } else {
            newLight = this.calcLightLevel(side, realX, realY, realZ);
        }

        return mergeCond.shouldMerge(block, ll, lightData, newBlock, newLight, newPcld);
    }

    private boolean canExpandVertically(MergeCondition mergeCond, BlockState block, float ll, PerCornerLightData lightData, Direction side, int startX, int endX, int endY, int z, PerCornerLightData[][] lightDatas, boolean[][] used) {
        for (int lx = startX; lx < endX; lx++) {
            int realX = this.realX(side, lx, z);
            int realY = this.realY(side, endY, z);
            int realZ = this.realZ(side, lx, endY, z);

            if (used[lx][endY]) return false;

            BlockState lblk = this.block(this.chunk, realX, realY, realZ);
            if (lblk == null || lblk.isAir()) return false;

            float llight = 15;
            PerCornerLightData lPcld = null;

            if (this.perCornerLight) {
                lPcld = lightDatas[lx][endY];
            } else {
                llight = this.calcLightLevel(side, realX, realY, realZ);
            }

            if (!mergeCond.shouldMerge(block, ll, lightData, lblk, llight, lPcld)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Averages block light values in a corner.
     *
     * @param side BlockFace of the face being calculated
     * @param cx   Chunk-relative X coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param y    Chunk-relative Y coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param cz   Chunk-relative Z coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     */
    private float calcPerCornerLight(Direction side, int cx, int y, int cz) {
        // coordinate offsets for getting the blocks to average
        int posX = 0, negX = 0, posY = 0, negY = 0, posZ = 0, negZ = 0;
        switch (side) {
            case UP:// Use the light values from the blocks above the face
                negY = posY = 1;
                // Get blocks around the point
                negZ = negX = -1;
                break;
            case DOWN:// Use the light values from the blocks below the face
                negY = posY = -1;
                // Get blocks around the point
                negZ = negX = -1;
                break;
            case WEST:// Use the light values from the blocks to the west of the face
                negX = posX = -1;
                // Get blocks around the point
                negY = negZ = -1;
                break;
            case EAST:// Use the light values from the blocks to the east of the face
                negX = posX = 1;
                // Get blocks around the point
                negY = negZ = -1;
                break;
            case NORTH:// Use the light values from the blocks to the north of the face
                negZ = posZ = 1;
                // Get blocks around the point
                negY = negX = -1;
                break;
            case SOUTH:// Use the light values from the blocks to the south of the face
                negZ = posZ = -1;
                // Get blocks around the point
                negY = negX = -1;
                break;
        }
        // sx,sy,sz are the setX, setY, and z positions of the side block
        int count = 0;
        float lightSum = 0;
        for (int sy = y + negY; sy <= y + posY; sy++) {
            for (int sz = cz + negZ; sz <= cz + posZ; sz++) {
                for (int sx = cx + negX; sx <= cx + posX; sx++) {
                    @Nullable ClientChunkAccess sChunk = this.chunk;
                    boolean getChunk = false; // whether the block is not in the current chunk and a new chunk should be found
                    int getChunkX = GreedyMesher.OFF_X + sx;
                    int getChunkY = GreedyMesher.OFF_Y + sy;
                    int getChunkZ = GreedyMesher.OFF_Z + sz;
                    int fixedSz = sz;
                    int fixedSy = sy;
                    int fixedSx = sx;
                    if (sz < 0) {
                        fixedSz = CHUNK_SIZE + sz;
                        getChunk = true;
                    } else if (sz >= CHUNK_SIZE) {
                        fixedSz = sz - CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (sy < 0) {
                        fixedSy = CHUNK_SIZE + sy;
                        getChunk = true;
                    } else if (sy >= CHUNK_SIZE) {
                        fixedSy = sy - CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (sx < 0) {
                        fixedSx = CHUNK_SIZE + sx;
                        getChunk = true;
                    } else if (sx >= CHUNK_SIZE) {
                        fixedSx = sx - CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (getChunk) {
                        sChunk = this.chunk.getWorld().getChunk(getChunkX, getChunkY, getChunkZ);
                    }
                    if (sChunk == null)
                        continue;

                    try {
                        // Convert to chunk-relative coords
                        lightSum += sChunk.getBlockLightLevel(fixedSx, fixedSy, fixedSz);
                        count++;
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }
        }
        return lightSum / count;
    }

    /**
     * Averages sunlight values at a corner.
     *
     * @param side BlockFace of the face being calculated
     * @param cx   Chunk-relative X coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param y    Chunk-relative Y coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param cz   Chunk-relative Z coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     */
    private float calcPerCornerSunlight(Direction side, int cx, int y, int cz) {
        // coordinate offsets for getting the blocks to average
        int posX = 0, negX = 0, posY = 0, negY = 0, posZ = 0, negZ = 0;
        switch (side) {
            case UP -> {
                negY = posY = 1;
                // Get blocks around the point
                negZ = negX = -1;
            }
            case DOWN -> {
                negY = posY = -1;
                // Get blocks around the point
                negZ = negX = -1;
            }
            case WEST -> {
                negX = posX = -1;
                // Get blocks around the point
                negY = negZ = -1;
            }
            case EAST -> {
                negX = posX = 1;
                // Get blocks around the point
                negY = negZ = -1;
            }
            case NORTH -> {
                negZ = posZ = 1;
                // Get blocks around the point
                negY = negX = -1;
            }
            case SOUTH -> {
                negZ = posZ = -1;
                // Get blocks around the point
                negY = negX = -1;
            }
        }

        // sx,sy,sz are the setX, setY, and z positions of the side block
        int count = 0;
        float lightSum = 0;
        for (int sy = y + negY; sy <= y + posY; sy++) {
            for (int sz = cz + negZ; sz <= cz + posZ; sz++) {
                for (int sx = cx + negX; sx <= cx + posX; sx++) {
                    @Nullable ClientChunkAccess sChunk = this.chunk;
                    boolean getChunk = false; // whether the block is not in the current chunk and a new chunk should be found
                    int getChunkX = GreedyMesher.OFF_X + sx;
                    int getChunkY = GreedyMesher.OFF_Y + sy;
                    int getChunkZ = GreedyMesher.OFF_Z + sz;
                    int fixedSz = sz;
                    int fixedSy = sy;
                    int fixedSx = sx;
                    if (sz < 0) {
                        fixedSz = CHUNK_SIZE + sz;
                        getChunk = true;
                    } else if (sz >= CHUNK_SIZE) {
                        fixedSz = sz - CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (sy < 0) {
                        fixedSy = CHUNK_SIZE + sy;
                        getChunk = true;
                    } else if (sy >= CHUNK_SIZE) {
                        fixedSy = sy - CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (sx < 0) {
                        fixedSx = CHUNK_SIZE + sx;
                        getChunk = true;
                    } else if (sx >= CHUNK_SIZE) {
                        fixedSx = sx - CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (getChunk) {
                        sChunk = this.chunk.getWorld().getChunk(getChunkX, getChunkY, getChunkZ);
                    }
                    if (sChunk == null)
                        continue;

                    try {
                        // Convert to chunk-relative coords
                        lightSum += sChunk.getSunlightLevel(fixedSx, fixedSy, fixedSz);
                        count++;
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }
        }
        return lightSum / count;
    }

    private boolean shouldOcclude(Block curBlock, Block blockToBlockFace) {
        boolean bothOcclude = curBlock.doesOcclude() && blockToBlockFace.doesOcclude();
        return !(shouldNotRenderNormally(blockToBlockFace) || blockToBlockFace.isTransparent() && bothOcclude || (BlockRenderTypeRegistry.get(curBlock) != BlockRenderTypeRegistry.get(blockToBlockFace) && !bothOcclude));
    }

    private boolean shouldMerge(BlockState id1, float light1, PerCornerLightData lightData1, BlockState id2, float light2, PerCornerLightData lightData2) {
        if (!id1.getBlock().shouldGreedyMerge()) return false;

        boolean sameBlock = Objects.equals(id1, id2);
        boolean sameLight = light1 == light2;
        boolean tooDarkToTell = light1 < 0.1f; // Too dark to tell they're not the same block

        if (this.perCornerLight) {
            sameLight = lightData1.equals(lightData2);
        }

        // Other block renderers may alter shape in an unpredictable way
        boolean considerAsSame = sameLight && !sameBlock && tooDarkToTell
                && QuantumClient.get().getBlockModel(id1) == QuantumClient.get().getBlockModel(id2)
                && GreedyMesher.isFullCubeRender(id1.getBlock()) && GreedyMesher.isFullCubeRender(id2.getBlock())
                && (!id1.isTransparent() && !id2.isTransparent());

        if (considerAsSame)
            sameBlock = true; // Consider them the same block

        return sameBlock && sameLight;
    }

    @Override
    public void buildMesh(UseCondition condition, MeshPartBuilder builder) {
        if (!chunk.isLoaded())
            throw new IllegalStateException("Chunk is not loaded");

        List<Face> faces;
        try (var ignoredSection = QuantumClient.PROFILER.start("chunk-get-faces")) {
            faces = this.getFaces(condition, this::shouldOcclude, this::shouldMerge);
        }

        try (var section = QuantumClient.PROFILER.start("mesh-faces")) {
            if (section != null) section.addStat("face-count", faces.size());
            builder.ensureVertices(faces.size() * 4);
            for (Face f : faces) {
                f.render(builder);
            }
        }
    }

    /**
     * Represents light level data for a specific block, encapsulating both block brightness
     * and sunlight brightness.
     *
     * @param blockBrightness The brightness level emitted by the block itself.
     * @param sunBrightness   The brightness level contributed by sunlight on the block.
     */
    public record LightLevelData(float blockBrightness, float sunBrightness) {

        @Override
        public String toString() {
            return "LightLevelData[" +
                    "sunBrightness=" + sunBrightness + ", " +
                    "blockBrightness=" + blockBrightness + ']';
        }
    }

    /**
     * The Face class represents a single face of a block in the game world. This includes the coordinates, lighting
     * information, and rendering details required to draw the face on the screen.
     */
    public static class Face {

        private final Direction side;
        private final int x1, y1, x2, y2, z;
        private final float lightLevel;
        private final PerCornerLightData lightData;
        private final BlockRenderer renderer;
        private final BakedCubeModel bakedBlockModel;
        private final float sunlightLevel;

        /**
         * Constructs a new Face instance.
         *
         * @param side the direction of the face
         * @param block the block state to which this face belongs
         * @param lightLevel the general light level of this face
         * @param lightData the per-corner light data for this face
         * @param startX the starting X coordinate of the face
         * @param startY the starting Y coordinate of the face
         * @param endX the ending X coordinate of the face
         * @param endY the ending Y coordinate of the face
         * @param z the Z coordinate of the face
         * @param sunlightLevel the sunlight level of this face
         */
        public Face(Direction side, BlockState block, float lightLevel, PerCornerLightData lightData, int startX, int startY, int endX, int endY, int z, float sunlightLevel) {
            this.lightLevel = lightLevel;
            this.x1 = startX;
            this.y1 = startY;
            this.x2 = endX;
            this.y2 = endY;
            this.z = z;
            this.side = side;
            this.sunlightLevel = sunlightLevel;
            this.lightData = lightData == null ? PerCornerLightData.EMPTY : lightData;

            QuantumClient client = QuantumClient.get();
            this.renderer = BlockRendererRegistry.get(block.getBlock());
            this.bakedBlockModel = (BakedCubeModel) client.getBlockModel(block);
        }

        /**
         * Renders a face of a block using the specified MeshPartBuilder.
         *
         * @param builder the MeshPartBuilder used to build the mesh for the face
         */
        public void render(MeshPartBuilder builder) {
            try (var ignored = QuantumClient.PROFILER.start("face")) {
                LightLevelData lld = new LightLevelData(this.lightLevel, this.sunlightLevel);
                if (this.bakedBlockModel == null) return;
                switch (this.side) {
                    case UP ->
                            this.renderer.renderTop(this.bakedBlockModel.top(), this.x1, this.y1, this.x2, this.y2, this.z + 1, lld, this.lightData, builder);
                    case DOWN ->
                            this.renderer.renderBottom(this.bakedBlockModel.bottom(), this.x1, this.y1, this.x2, this.y2, this.z, lld, this.lightData, builder);
                    case NORTH ->
                            this.renderer.renderNorth(this.bakedBlockModel.north(), this.x1, this.y1, this.x2, this.y2, this.z + 1, lld, this.lightData, builder);
                    case SOUTH ->
                            this.renderer.renderSouth(this.bakedBlockModel.south(), this.x1, this.y1, this.x2, this.y2, this.z, lld, this.lightData, builder);
                    case EAST ->
                            this.renderer.renderEast(this.bakedBlockModel.east(), this.x1, this.y1, this.x2, this.y2, this.z + 1, lld, this.lightData, builder);
                    case WEST ->
                            this.renderer.renderWest(this.bakedBlockModel.west(), this.x1, this.y1, this.x2, this.y2, this.z, lld, this.lightData, builder);
                }
            }
        }

    }

    // Find "real" setX based on relative position in the greedy method
    private int realX(Direction side, int x, int z) {
        return switch (side) {
            case UP, DOWN, NORTH, SOUTH -> x;
            case EAST, WEST -> z;
        };
    }

    // Find "real" setY based on relative position in the greedy method
    private int realY(Direction side, int y, int z) {
        return switch (side) {
            case EAST, WEST, NORTH, SOUTH -> y;
            case UP, DOWN -> z;
        };
    }

    // Find "real" z based on relative position in the greedy method
    private int realZ(Direction side, int x, int y, int z) {
        return switch (side) {
            case UP, DOWN -> y;
            case WEST, EAST -> x;
            case NORTH, SOUTH -> z;
        };
    }

    /**
     * Represents a condition to determine when the side of a given block
     * should be occluded based on the block it faces.
     */
    public interface OccludeCondition {
        /**
         * @param curBlock    current block being checked
         * @param facingBlock block in the facing direction
         * @return {@code true} if the side of the curBlock should be occluded, {@code false} otherwise
         */
        boolean shouldOcclude(Block curBlock, Block facingBlock);
    }

    /**
     * Represents a condition to determine whether two adjacent block faces
     * should be merged during the mesh generation process.
     * <p>
     * The `shouldMerge` method will be implemented to define the logic
     * for merging faces based on their properties, such as block state and
     * lighting conditions.
     */
    public interface MergeCondition {
        /**
         * Determines whether two adjacent block faces should be merged during the mesh generation process.
         *
         * @param data1 The block state of the first face.
         * @param light1 The light level of the first face.
         * @param lightData1 The per-corner light data of the first face.
         * @param data2 The block state of the second face.
         * @param light2 The light level of the second face.
         * @param lightData2 The per-corner light data of the second face.
         * @return true if the two block faces should be merged; false otherwise.
         */
        boolean shouldMerge(BlockState data1, float light1, PerCornerLightData lightData1, BlockState data2, float light2, PerCornerLightData lightData2);
    }

    private BlockState block(@NotNull ClientChunkAccess chunk, int x, int y, int z) {
        ClientWorldAccess world = chunk.getWorld();
        this.tmp3i.set(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ()).mul(CHUNK_SIZE).add(x, y, z);
        if (y < 0) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY() - 1, chunk.getVec().getIntZ());
            y += CHUNK_SIZE;
        } else if (y >= CHUNK_SIZE) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY() + 1, chunk.getVec().getIntZ());
            y -= CHUNK_SIZE;
        }

        if (x < 0 && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX() - 1, chunk.getVec().getIntY(), chunk.getVec().getIntZ());
            x += CHUNK_SIZE;
        } else if (x >= CHUNK_SIZE && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX() + 1, chunk.getVec().getIntY(), chunk.getVec().getIntZ());
            x -= CHUNK_SIZE;
        }

        if (z < 0 && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ() - 1);
            z += CHUNK_SIZE;
        } else if (z >= CHUNK_SIZE && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ() + 1);
            z -= CHUNK_SIZE;
        }

        if (chunk != null)
            return chunk.get(World.toLocalBlockVec(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return Blocks.AIR.getDefaultState();
    }

    private int blockLight(@NotNull ClientChunkAccess chunk, int x, int y, int z) {
        ClientWorldAccess world = chunk.getWorld();
        this.tmp3i.set(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ()).mul(CHUNK_SIZE).add(x, y, z);
        if (y < 0) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY() - 1, chunk.getVec().getIntZ());
            y += CHUNK_SIZE;
        } else if (y >= CHUNK_SIZE) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY() + 1, chunk.getVec().getIntZ());
            y -= CHUNK_SIZE;
        }

        if (x < 0 && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX() - 1, chunk.getVec().getIntY(), chunk.getVec().getIntZ());
            x += CHUNK_SIZE;
        } else if (x >= CHUNK_SIZE && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX() + 1, chunk.getVec().getIntY(), chunk.getVec().getIntZ());
            x -= CHUNK_SIZE;
        }

        if (z < 0 && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ() - 1);
            z += CHUNK_SIZE;
        } else if (z >= CHUNK_SIZE && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ() + 1);
            z -= CHUNK_SIZE;
        }

        if (chunk != null)
            return chunk.getBlockLight(World.toLocalBlockVec(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return 0;
    }

    private int sunlight(@NotNull ClientChunkAccess chunk, int x, int y, int z) {
        ClientWorldAccess world = chunk.getWorld();
        this.tmp3i.set(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ()).mul(CHUNK_SIZE).add(x, y, z);
        if (y < 0) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY() - 1, chunk.getVec().getIntZ());
            y += CHUNK_SIZE;
        } else if (y >= CHUNK_SIZE) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY() + 1, chunk.getVec().getIntZ());
            y -= CHUNK_SIZE;
        }

        if (x < 0 && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX() - 1, chunk.getVec().getIntY(), chunk.getVec().getIntZ());
            x += CHUNK_SIZE;
        } else if (x >= CHUNK_SIZE && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX() + 1, chunk.getVec().getIntY(), chunk.getVec().getIntZ());
            x -= CHUNK_SIZE;
        }

        if (z < 0 && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ() - 1);
            z += CHUNK_SIZE;
        } else if (z >= CHUNK_SIZE && chunk != null) {
            chunk = world.getChunk(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ() + 1);
            z -= CHUNK_SIZE;
        }

        if (chunk != null)
            return chunk.getSunlight(World.toLocalBlockVec(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return 15;
    }
}