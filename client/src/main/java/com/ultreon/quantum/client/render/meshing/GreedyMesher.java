package com.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ultreon.quantum.block.Block;
import com.ultreon.quantum.block.Blocks;
import com.ultreon.quantum.block.state.BlockProperties;
import com.ultreon.quantum.client.QuantumClient;
import com.ultreon.quantum.client.model.block.BakedCubeModel;
import com.ultreon.quantum.client.model.block.BlockModel;
import com.ultreon.quantum.client.model.block.BlockModelRegistry;
import com.ultreon.quantum.client.registry.BlockRenderTypeRegistry;
import com.ultreon.quantum.client.registry.BlockRendererRegistry;
import com.ultreon.quantum.client.render.BlockRenderer;
import com.ultreon.quantum.client.render.NormalBlockRenderer;
import com.ultreon.quantum.client.world.ClientChunk;
import com.ultreon.quantum.client.world.ClientWorld;
import com.ultreon.quantum.util.PosOutOfBoundsException;
import com.ultreon.quantum.world.BlockPos;
import com.ultreon.quantum.world.CubicDirection;
import com.ultreon.quantum.world.World;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ultreon.quantum.world.World.WORLD_DEPTH;

/**
 * Mesher using the "greedy meshing" technique.
 * <p>
 * Similar to the method described by Mikola Lysenko at <a href="http://0fps.net/2012/06/30/meshing-in-a-minecraft-game/">0fps.net (Meshing in a Minecraft game)</a>
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
    private final ClientChunk chunk;
    private final boolean perCornerLight;
    private final Vec3i tmp3i = new Vec3i();

    /**
     * @param chunk          Chunk to mesh
     * @param perCornerLight Whether to average light on a per-corner basis
     */
    public GreedyMesher(ClientChunk chunk, boolean perCornerLight) {
        this.chunk = chunk;
        this.perCornerLight = perCornerLight;
    }

    @Override
    public void meshVoxels(ModelBuilder builder, MeshPartBuilder meshBuilder, UseCondition condition) {
        List<Face> faces = this.getFaces(builder, condition);

        this.meshFaces(faces, meshBuilder);
    }

    public List<Face> getFaces(ModelBuilder builder, UseCondition condition, OccludeCondition ocCond, MergeCondition shouldMerge) {
        List<Face> faces = new ArrayList<>();

        PerCornerLightData bright;
        if (this.perCornerLight) {
            bright = new PerCornerLightData();
            bright.l00 = 1;
            bright.l01 = 1;
            bright.l10 = 1;
            bright.l11 = 1;
        }

        int width = World.CHUNK_SIZE;
        int depth = World.CHUNK_SIZE;
        int height = World.CHUNK_HEIGHT + 1;
        // Top, bottom
        for (int y = 0; y <= World.CHUNK_HEIGHT; y++) {
            boolean[][] topMask = new boolean[width][depth];
            PerCornerLightData[][] topPcld = null;
            if (this.perCornerLight) {
                topPcld = new PerCornerLightData[width][depth];
            }
            boolean[][] btmMask = new boolean[width][depth];
            PerCornerLightData[][] btmPcld = null;
            if (this.perCornerLight) {
                btmPcld = new PerCornerLightData[width][depth];
            }
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    try {
                        BlockProperties curBlock = this.block(this.chunk, x, y, z);
                        if (curBlock == null) continue;
                        BlockModel blockModel = BlockModelRegistry.get(curBlock);
                        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
                            this.chunk.addModel(new BlockPos(x, y, z), new ModelInstance(blockModel.getModel()));
                            continue;
                        }

                        if (!condition.shouldUse(curBlock.getBlock())) continue;

                        BlockProperties block = this.block(this.chunk, x, y + 1, z);
                        if (block == null) continue;
                        if (y < height - 1 && !ocCond.shouldOcclude(curBlock.getBlock(), block.getBlock())) {
                            topMask[x][z] = true;

                            if (this.perCornerLight) {
                                PerCornerLightData lightData = new PerCornerLightData();
                                lightData.l00 = this.calcPerCornerLight(CubicDirection.UP, x, y, z);
                                lightData.l01 = this.calcPerCornerLight(CubicDirection.UP, x, y, z + 1);
                                lightData.l10 = this.calcPerCornerLight(CubicDirection.UP, x + 1, y, z);
                                lightData.l11 = this.calcPerCornerLight(CubicDirection.UP, x + 1, y, z + 1);
                                topPcld[x][z] = lightData;
                            }
                        }
                        BlockProperties block1 = this.block(this.chunk, x, y - 1, z);
                        if (block1 == null) continue;
                        if (y > 0 && !ocCond.shouldOcclude(curBlock.getBlock(), block1.getBlock())) {
                            btmMask[x][z] = true;

                            if (this.perCornerLight) {
                                PerCornerLightData lightData = new PerCornerLightData();
                                lightData.l00 = this.calcPerCornerLight(CubicDirection.DOWN, x, y, z);
                                lightData.l01 = this.calcPerCornerLight(CubicDirection.DOWN, x, y, z + 1);
                                lightData.l10 = this.calcPerCornerLight(CubicDirection.DOWN, x + 1, y, z);
                                lightData.l11 = this.calcPerCornerLight(CubicDirection.DOWN, x + 1, y, z + 1);
                                btmPcld[x][z] = lightData;
                            }
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }
            this.greedy(faces, CubicDirection.UP, shouldMerge, topMask, topPcld, y, GreedyMesher.OFF_X, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y);
            this.greedy(faces, CubicDirection.DOWN, shouldMerge, btmMask, btmPcld, y, GreedyMesher.OFF_X, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y);
        }

        // East, west
        for (int x = 0; x < width; x++) {
            boolean[][] westMask = new boolean[depth][World.CHUNK_HEIGHT + 1];
            PerCornerLightData[][] westPcld = null;
            if (this.perCornerLight) {
                westPcld = new PerCornerLightData[depth][World.CHUNK_HEIGHT + 1];
            }
            boolean[][] eastMask = new boolean[depth][World.CHUNK_HEIGHT + 1];
            PerCornerLightData[][] eastPcld = null;
            if (this.perCornerLight) {
                eastPcld = new PerCornerLightData[depth][World.CHUNK_HEIGHT + 1];
            }
            for (int y = 0; y <= World.CHUNK_HEIGHT; y++) {
                for (int z = 0; z < depth; z++) {
                    try {
                        BlockProperties curBlock = this.block(this.chunk, x, y, z);
                        if (curBlock == null) continue;
                        BlockModel blockModel = BlockModelRegistry.get(curBlock);
                        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
                            continue;
                        }
                        if (!condition.shouldUse(curBlock.getBlock())) continue;

                        int westNeighborX = x - 1;
                        int eastNeighborX = x + 1;
                        ClientChunk westNeighborChunk = this.chunk;
                        ClientChunk eastNeighborChunk = this.chunk;
                        if (westNeighborX < 0) {
                            westNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getPos().x() - 1, this.chunk.getPos().z());
                            westNeighborX += World.CHUNK_SIZE;
                        }
                        if (eastNeighborX >= World.CHUNK_SIZE) {
                            eastNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getPos().x() + 1, this.chunk.getPos().z());
                            eastNeighborX -= World.CHUNK_SIZE;
                        }
                        if (westNeighborChunk != null) {
                            BlockProperties westNeighborBlk = westNeighborChunk.get(westNeighborX, y, z);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), westNeighborBlk.getBlock())) {
                                westMask[z][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(CubicDirection.WEST, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(CubicDirection.WEST, x, y, z + 1);
                                    lightData.l10 = this.calcPerCornerLight(CubicDirection.WEST, x, y + 1, z);
                                    lightData.l11 = this.calcPerCornerLight(CubicDirection.WEST, x, y + 1, z + 1);
                                    westPcld[z][y] = lightData;
                                }
                            }
                        }

                        if (eastNeighborChunk != null) {
                            BlockProperties eastNeighborBlk = eastNeighborChunk.get(eastNeighborX, y, z);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), eastNeighborBlk.getBlock())) {
                                eastMask[z][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(CubicDirection.EAST, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(CubicDirection.EAST, x, y, z + 1);
                                    lightData.l10 = this.calcPerCornerLight(CubicDirection.EAST, x, y + 1, z);
                                    lightData.l11 = this.calcPerCornerLight(CubicDirection.EAST, x, y + 1, z + 1);
                                    eastPcld[z][y] = lightData;
                                }
                            }
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }

            this.greedy(faces, CubicDirection.EAST, shouldMerge, eastMask, eastPcld, x, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y, GreedyMesher.OFF_X);
            this.greedy(faces, CubicDirection.WEST, shouldMerge, westMask, westPcld, x, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y, GreedyMesher.OFF_X);
        }

        // North, south
        for (int z = 0; z < depth; z++) {
            boolean[][] northMask = new boolean[width][World.CHUNK_HEIGHT + 1];
            PerCornerLightData[][] northPcld = null;
            if (this.perCornerLight) {
                northPcld = new PerCornerLightData[width][World.CHUNK_HEIGHT + 1];
            }
            boolean[][] southMask = new boolean[width][World.CHUNK_HEIGHT + 1];
            PerCornerLightData[][] southPcld = null;
            if (this.perCornerLight) {
                southPcld = new PerCornerLightData[width][World.CHUNK_HEIGHT + 1];
            }
            for (int y = 0; y <= World.CHUNK_HEIGHT; y++) {
                for (int x = 0; x < width; x++) {
                    try {
                        BlockProperties curBlock = this.block(this.chunk, x, y, z);
                        if (curBlock == null) continue;
                        BlockModel blockModel = BlockModelRegistry.get(curBlock);
                        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
                            continue;
                        }
                        if (!condition.shouldUse(curBlock.getBlock())) continue;

                        int northNeighborZ = z + 1;
                        int southNeighborZ = z - 1;
                        ClientChunk northNeighborChunk = this.chunk;
                        ClientChunk southNeighborChunk = this.chunk;
                        if (northNeighborZ >= World.CHUNK_SIZE) {
                            northNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getPos().x(), this.chunk.getPos().z() + 1);
                            northNeighborZ -= World.CHUNK_SIZE;
                        } else if (southNeighborZ < 0) {
                            southNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getPos().x(), this.chunk.getPos().z() - 1);
                            southNeighborZ += World.CHUNK_SIZE;
                        }

                        if (northNeighborChunk != null) {
                            BlockProperties northNeighborBlock = northNeighborChunk.get(x, y, northNeighborZ);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), northNeighborBlock.getBlock())) {
                                northMask[x][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(CubicDirection.NORTH, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(CubicDirection.NORTH, x, y + 1, z);
                                    lightData.l10 = this.calcPerCornerLight(CubicDirection.NORTH, x + 1, y, z);
                                    lightData.l11 = this.calcPerCornerLight(CubicDirection.NORTH, x + 1, y + 1, z);
                                    northPcld[x][y] = lightData;
                                }
                            }
                        }

                        if (southNeighborChunk != null) {
                            BlockProperties southNeighborBlock = southNeighborChunk.get(x, y, southNeighborZ);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), southNeighborBlock.getBlock())) {
                                southMask[x][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(CubicDirection.SOUTH, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(CubicDirection.SOUTH, x, y + 1, z);
                                    lightData.l10 = this.calcPerCornerLight(CubicDirection.SOUTH, x + 1, y, z);
                                    lightData.l11 = this.calcPerCornerLight(CubicDirection.SOUTH, x + 1, y + 1, z);
                                    southPcld[x][y] = lightData;
                                }
                            }
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }


            this.greedy(faces, CubicDirection.NORTH, shouldMerge, northMask, northPcld, z, GreedyMesher.OFF_X, GreedyMesher.OFF_Y, GreedyMesher.OFF_Z);
            this.greedy(faces, CubicDirection.SOUTH, shouldMerge, southMask, southPcld, z, GreedyMesher.OFF_X, GreedyMesher.OFF_Y, GreedyMesher.OFF_Z);
        }

        return faces;
    }

    private float calcLightLevel(CubicDirection side, int x, int y, int z) throws PosOutOfBoundsException {
        LightLevelData result = this.calcLightLevels(side, x, y, z);
        if (result == null) return 1;

        return Math.min(1, result.sunBrightness() + result.blockBrightness());
    }

    @Nullable
    private LightLevelData calcLightLevels(CubicDirection side, int x, int y, int z) {
        switch (side) {
            case UP:
                y += 1;
                break;
            case DOWN:
                y -= 1;
                break;
            case WEST:
                x -= 1;
                break;
            case EAST:
                x += 1;
                break;
            case NORTH:
                z += 1;
                break;
            case SOUTH:
                z -= 1;
                break;
        }

        ClientWorld world = this.chunk.getWorld();
        int chunkSize = World.CHUNK_SIZE;
        ClientChunk sChunk = this.chunk;
        if (z < 0) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Z + z);
            z += chunkSize;
        } else if (z > chunkSize - 1) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Z + z);
            z -= chunkSize;
        } else if (x < 0) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Z + z);
            x += chunkSize;
        } else if (x > chunkSize - 1) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Z + z);
            x -= chunkSize;
        }

        if (sChunk == null) return null;
        float sunBrightness = sChunk.getBrightness(this.sunlight(sChunk, x, y, z));
        float blockBrightness = sChunk.getBrightness(this.blockLight(sChunk, x, y, z));
        return new LightLevelData(sunBrightness, blockBrightness);
    }

    public List<Face> getFaces(ModelBuilder builder, UseCondition condition) {
        return this.getFaces(builder, condition, this::shouldOcclude, this::shouldMerge);
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
    private void greedy(List<Face> outputList, CubicDirection side, MergeCondition mergeCond, boolean[][] mask, PerCornerLightData[][] lightDatas, int z, int offsetX, int offsetY, int offsetZ) {
        int width = mask.length;
        int height = mask[0].length;
        boolean[][] used = new boolean[mask.length][mask[0].length];

        try {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!mask[x][y]) continue;

                    // "real" values of x,y,z
                    int realX = this.realX(side, x, y, z);
                    int realY = this.realY(side, x, y, z);
                    int realZ = this.realZ(side, x, y, z);

                    BlockProperties block = this.block(this.chunk, realX, realY, realZ);
                    if (block == null || block.isAir() || used[x][y]) continue;
                    used[x][y] = true;
                    float ll = 1;
                    PerCornerLightData lightData = null;
                    if (this.perCornerLight) {
                        lightData = lightDatas[x][y];
                    } else {
                        ll = this.calcLightLevel(side, realX, realY, realZ);
                    }
                    int endX = x + 1;
                    int endY = y + 1;
                    while (true) {
                        int newX = endX;
                        boolean shouldPass = false;
                        if (newX < width) {
                            int newRealX = this.realX(side, newX, y, z);
                            int newRealY = this.realY(side, newX, y, z);
                            int newRealZ = this.realZ(side, newX, y, z);
                            BlockProperties newBlock = this.block(this.chunk, newRealX, newRealY, newRealZ);
                            float newLight = 15;
                            PerCornerLightData newPcld = null;
                            if (this.perCornerLight) {
                                newPcld = lightDatas[newX][y];
                            } else {
                                newLight = this.calcLightLevel(side, newRealX, newRealY, newRealZ);
                            }
                            shouldPass = !used[newX][y] && !(newBlock == null || newBlock.isAir()) && mergeCond.shouldMerge(block, ll, lightData, newBlock, newLight, newPcld);
                        }
                        // expand right if the same block
                        if (shouldPass) {
                            endX++;
                            used[newX][y] = true;
                        } else { // Done on initial pass right. Start passing up.
                            while (true) {
                                if (endY == height) break;
                                boolean allPassed = true;
                                // sweep right
                                for (int lx = x; lx < endX; lx++) {
                                    // "real" coordinates for the length block
                                    int lRX = this.realX(side, lx, endY, z);
                                    int lRY = this.realY(side, lx, endY, z);
                                    int lRZ = this.realZ(side, lx, endY, z);

                                    BlockProperties lblk = this.block(this.chunk, lRX, lRY, lRZ);
                                    if (lblk == null || lblk.isAir()) {
                                        allPassed = false;
                                        break;
                                    }
                                    float llight = 15;
                                    PerCornerLightData lPcld = null;
                                    if (this.perCornerLight) {
                                        lPcld = lightDatas[lx][endY];
                                    } else {
                                        llight = this.calcLightLevel(side, lRX, lRY, lRZ);
                                    }

                                    if (used[lx][endY] || !mergeCond.shouldMerge(block, ll, lightData, lblk, llight, lPcld)) {
                                        allPassed = false;
                                        break;
                                    }
                                }
                                if (allPassed) {
                                    for (int lx = x; lx < endX; lx++) {
                                        used[lx][endY] = true;
                                    }
                                    endY++;
                                } else {
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    outputList.add(new Face(side, block, ll, lightData, x + offsetX, y + offsetY, endX + offsetX, endY + offsetY, z + offsetZ, 1));
                }
            }
        } catch (PosOutOfBoundsException ex) {
            throw new GdxRuntimeException(ex);
        }
    }

    public void meshFaces(List<Face> faces, MeshPartBuilder builder) {
        builder.ensureVertices(faces.size() * 4);
        for (Face f : faces) {
            f.render(builder);
        }
    }

    /**
     * Averages light values at a corner.
     *
     * @param side BlockFace of the face being calculated
     * @param cx   Chunk-relative X coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param y    Chunk-relative Y coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     * @param cz   Chunk-relative Z coordinate for the corner. NOT PRE-OFFSET FOR THE FACE!
     */
    private float calcPerCornerLight(CubicDirection side, int cx, int y, int cz) {
        // coordinate offsets for getting the blocks to average
        int posX = 0, negX = 0, posY = 0, negY = 0, posZ = 0, negZ = 0;
        switch (side) {
            case UP -> {
                // Use the light values from the blocks above the face
                negY = posY = 1;
                // Get blocks around the point
                negZ = negX = -1;
            }
            case DOWN -> {
                // Use the light values from the blocks below the face
                negY = posY = -1;
                // Get blocks around the point
                negZ = negX = -1;
            }
            case WEST -> {
                // Use the light values from the blocks to the west of the face
                negX = posX = -1;
                // Get blocks around the point
                negY = negZ = -1;
            }
            case EAST -> {
                // Use the light values from the blocks to the east of the face
                negX = posX = 1;
                // Get blocks around the point
                negY = negZ = -1;
            }
            case NORTH -> {
                // Use the light values from the blocks to the north of the face
                negZ = posZ = 1;
                // Get blocks around the point
                negY = negX = -1;
            }
            case SOUTH -> {
                // Use the light values from the blocks to the south of the face
                negZ = posZ = -1;
                // Get blocks around the point
                negY = negX = -1;
            }
        }
        // sx,sy,sz are the x, y, and z positions of the side block
        int count = 0;
        float lightSum = 0;
        for (int sy = y + negY; sy <= y + posY; sy++) {
            if (sy < 0 || sy >= World.CHUNK_HEIGHT) continue;
            for (int sz = cz + negZ; sz <= cz + posZ; sz++) {
                for (int sx = cx + negX; sx <= cx + posX; sx++) {
                    ClientChunk sChunk = this.chunk;
                    boolean getChunk = false; // whether the block is not in the current chunk and a new chunk should be found
                    int getChunkX = GreedyMesher.OFF_X + sx;
                    int getChunkZ = GreedyMesher.OFF_Z + sz;
                    int fixedSz = sz;
                    int fixedSx = sx;
                    if (sz < 0) {
                        fixedSz = World.CHUNK_SIZE + sz;
                        getChunk = true;
                    } else if (sz >= World.CHUNK_SIZE) {
                        fixedSz = sz - World.CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (sx < 0) {
                        fixedSx = World.CHUNK_SIZE + sx;
                        getChunk = true;
                    } else if (sx >= World.CHUNK_SIZE) {
                        fixedSx = sx - World.CHUNK_SIZE;
                        getChunk = true;
                    }
                    if (getChunk) {
                        sChunk = this.chunk.getWorld().getChunk(getChunkX, getChunkZ);
                    }
                    if (sChunk == null) continue;

                    try {
                        // Convert to chunk-relative coords
                        lightSum += sChunk.getLightLevel(fixedSx, sy, fixedSz);
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

    private boolean shouldMerge(BlockProperties id1, float light1, PerCornerLightData lightData1, BlockProperties id2, float light2, PerCornerLightData lightData2) {
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

    public record LightLevelData(float sunBrightness, float blockBrightness) {

        public float lightLevel() {
            return Mth.clamp(this.sunBrightness + this.blockBrightness, 0, 1);
        }
    }

    public static class Face {

        private final CubicDirection side;
        private final int x1, y1, x2, y2, z;
        private final float lightLevel;
        private final PerCornerLightData lightData;
        private final BlockRenderer renderer;
        private final BakedCubeModel bakedBlockModel;
        private final float sunlightLevel;

        /**
         * @param lightData     Per corner light data. Pass null if per corner lighting is disabled.
         * @param sunlightLevel
         */
        public Face(CubicDirection side, BlockProperties block, float lightLevel, PerCornerLightData lightData, int startX, int startY, int endX, int endY, int z, float sunlightLevel) {
            this.lightLevel = lightLevel;
            this.x1 = startX;
            this.y1 = startY;
            this.x2 = endX;
            this.y2 = endY;
            this.z = z;
            this.side = side;
            this.sunlightLevel = sunlightLevel;
            this.lightData = null;

            QuantumClient client = QuantumClient.get();
            this.renderer = BlockRendererRegistry.get(block.getBlock());
            this.bakedBlockModel = (BakedCubeModel) client.getBlockModel(block);
        }

        public void render(MeshPartBuilder builder) {
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

    // Find "real" x based on relative position in the greedy method
    private int realX(CubicDirection side, int x, int y, int z) {
        return switch (side) {
            case UP, DOWN, NORTH, SOUTH -> x;
            case EAST, WEST -> z;
        };
    }

    // Find "real" y based on relative position in the greedy method
    private int realY(CubicDirection side, int x, int y, int z) {
        return switch (side) {
            case EAST, WEST, NORTH, SOUTH -> y;
            case UP, DOWN -> z;
        };
    }

    // Find "real" z based on relative position in the greedy method
    private int realZ(CubicDirection side, int x, int y, int z) {
        return switch (side) {
            case UP, DOWN -> y;
            case WEST, EAST -> x;
            case NORTH, SOUTH -> z;
        };
    }

    public interface OccludeCondition {
        /**
         * @param curBlock    current block being checked
         * @param facingBlock block in the facing direction
         * @return {@code true} if the side of the curBlock should be occluded, {@code false} otherwise
         */
        boolean shouldOcclude(Block curBlock, Block facingBlock);
    }

    public interface MergeCondition {
        boolean shouldMerge(BlockProperties data1, float light1, PerCornerLightData lightData1, BlockProperties data2, float light2, PerCornerLightData lightData2);
    }

    private BlockProperties block(ClientChunk chunk, int x, int y, int z) {
        if (y < WORLD_DEPTH) return null;
        ClientWorld world = chunk.getWorld();
        this.tmp3i.set(chunk.getPos().x(), 0, chunk.getPos().z()).mul(16).add(x, y, z);
        ClientChunk chunkAt = world.getChunkAt(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z);
        if (chunkAt != null)
            return chunkAt.get(World.toLocalBlockPos(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return Blocks.AIR.createMeta();
    }

    private int blockLight(ClientChunk chunk, int x, int y, int z) {
        if (y < WORLD_DEPTH) return 0;
        ClientWorld world = chunk.getWorld();
        this.tmp3i.set(chunk.getPos().x(), 0, chunk.getPos().z()).mul(16).add(x, y, z);
        ClientChunk chunkAt = world.getChunkAt(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z);
        if (chunkAt != null)
            return chunkAt.getBlockLight(World.toLocalBlockPos(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return 0;
    }

    private int sunlight(ClientChunk chunk, int x, int y, int z) {
        if (y < WORLD_DEPTH) return 0;
        ClientWorld world = chunk.getWorld();
        this.tmp3i.set(chunk.getPos().x(), 0, chunk.getPos().z()).mul(16).add(x, y, z);
        ClientChunk chunkAt = world.getChunkAt(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z);
        if (chunkAt != null)
            return chunkAt.getSunlight(World.toLocalBlockPos(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return 0;
    }
}