package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.GdxRuntimeException;
import dev.ultreon.libs.commons.v0.Mth;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.block.BakedCubeModel;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.model.block.BlockModelRegistry;
import dev.ultreon.quantum.client.registry.BlockRenderPassRegistry;
import dev.ultreon.quantum.client.registry.BlockRendererRegistry;
import dev.ultreon.quantum.client.render.BlockRenderer;
import dev.ultreon.quantum.client.render.NormalBlockRenderer;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;
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

    @Override
    public boolean meshVoxels(UseCondition condition, ChunkModelBuilder modelBuilder) {
        List<Face> faces = this.getFaces(condition);
        if (faces.isEmpty()) {
            return false;
        }

        this.meshFaces(faces, modelBuilder);
        return true;
    }

    public List<Face> getFaces(UseCondition condition, OccludeCondition ocCond, MergeCondition shouldMerge) {
        List<Face> faces = new ArrayList<>();

        PerCornerLightData bright;
        if (this.perCornerLight) {
            bright = new PerCornerLightData();
            bright.l00 = 1;
            bright.l01 = 1;
            bright.l10 = 1;
            bright.l11 = 1;
        }

        int width = World.CS;
        int depth = World.CS;
        int height = World.CS;
        // Top, bottom
        for (int y = 0; y <= height; y++) {
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
                        BlockState curBlock = this.block(this.chunk, x, y, z);
                        if (curBlock == null) continue;
                        BlockModel blockModel = BlockModelRegistry.get().get(curBlock);
                        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
                            this.chunk.addModel(new BlockVec(x, y, z, BlockVecSpace.CHUNK), new ModelInstance(blockModel.getModel()));
                            continue;
                        }

                        if (!condition.shouldUse(curBlock)) continue;

                        int bottomNeighborY = y - 1;
                        int topNeighborY = y + 1;
                        @Nullable ClientChunkAccess bottomNeighborChunk = this.chunk;
                        @Nullable ClientChunkAccess topNeighborChunk = this.chunk;
                        if (bottomNeighborY < 0) {
                            bottomNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getVec().getIntX(), this.chunk.getVec().getIntY() - 1, this.chunk.getVec().getIntZ());
                            bottomNeighborY += World.CS;
                        }
                        if (topNeighborY >= World.CS) {
                            topNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getVec().getIntX(), this.chunk.getVec().getIntY() + 1, this.chunk.getVec().getIntZ());
                            topNeighborY -= World.CS;
                        }
                        if (topNeighborChunk != null) {
                            BlockState westNeighborBlk = topNeighborChunk.get(x, topNeighborY, z);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), westNeighborBlk.getBlock())) {
                                topMask[x][z] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(Direction.UP, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(Direction.UP, x, y, z + 1);
                                    lightData.l10 = this.calcPerCornerLight(Direction.UP, x + 1, y, z);
                                    lightData.l11 = this.calcPerCornerLight(Direction.UP, x + 1, y, z + 1);
                                    lightData.s00 = this.calcPerCornerSunlight(Direction.UP, x, y, z);
                                    lightData.s01 = this.calcPerCornerSunlight(Direction.UP, x, y, z + 1);
                                    lightData.s10 = this.calcPerCornerSunlight(Direction.UP, x + 1, y, z);
                                    lightData.s11 = this.calcPerCornerSunlight(Direction.UP, x + 1, y, z + 1);
                                    topPcld[x][z] = lightData;
                                }
                            }
                        }

                        if (bottomNeighborChunk != null) {
                            BlockState westNeighborBlk = bottomNeighborChunk.get(x, bottomNeighborY, z);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), westNeighborBlk.getBlock())) {
                                btmMask[x][z] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(Direction.DOWN, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(Direction.DOWN, x, y, z + 1);
                                    lightData.l10 = this.calcPerCornerLight(Direction.DOWN, x + 1, y, z);
                                    lightData.l11 = this.calcPerCornerLight(Direction.DOWN, x + 1, y, z + 1);
                                    lightData.s00 = this.calcPerCornerSunlight(Direction.DOWN, x, y, z);
                                    lightData.s01 = this.calcPerCornerSunlight(Direction.DOWN, x, y, z + 1);
                                    lightData.s10 = this.calcPerCornerSunlight(Direction.DOWN, x + 1, y, z);
                                    lightData.s11 = this.calcPerCornerSunlight(Direction.DOWN, x + 1, y, z + 1);
                                    btmPcld[x][z] = lightData;
                                }
                            }
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }
            this.greedy(faces, Direction.UP, shouldMerge, topMask, topPcld, y, GreedyMesher.OFF_X, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y);
            this.greedy(faces, Direction.DOWN, shouldMerge, btmMask, btmPcld, y, GreedyMesher.OFF_X, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y);
        }

        // East, west
        for (int x = 0; x < width; x++) {
            boolean[][] westMask = new boolean[depth][height];
            PerCornerLightData[][] westPcld = null;
            if (this.perCornerLight) {
                westPcld = new PerCornerLightData[depth][height];
            }
            boolean[][] eastMask = new boolean[depth][height];
            PerCornerLightData[][] eastPcld = null;
            if (this.perCornerLight) {
                eastPcld = new PerCornerLightData[depth][height];
            }
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    try {
                        BlockState curBlock = this.block(this.chunk, x, y, z);
                        if (curBlock == null) continue;
                        BlockModel blockModel = BlockModelRegistry.get().get(curBlock);
                        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
                            continue;
                        }
                        if (!condition.shouldUse(curBlock)) continue;

                        int westNeighborX = x - 1;
                        int eastNeighborX = x + 1;
                        @Nullable ClientChunkAccess westNeighborChunk = this.chunk;
                        @Nullable ClientChunkAccess eastNeighborChunk = this.chunk;
                        if (westNeighborX < 0) {
                            westNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getVec().getIntX() - 1, this.chunk.getVec().getIntY(), this.chunk.getVec().getIntZ());
                            westNeighborX += World.CS;
                        }
                        if (eastNeighborX >= World.CS) {
                            eastNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getVec().getIntX() + 1, this.chunk.getVec().getIntY(), this.chunk.getVec().getIntZ());
                            eastNeighborX -= World.CS;
                        }
                        if (westNeighborChunk != null) {
                            BlockState westNeighborBlk = westNeighborChunk.get(westNeighborX, y, z);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), westNeighborBlk.getBlock())) {
                                westMask[z][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(Direction.WEST, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(Direction.WEST, x, y, z + 1);
                                    lightData.l10 = this.calcPerCornerLight(Direction.WEST, x, y + 1, z);
                                    lightData.l11 = this.calcPerCornerLight(Direction.WEST, x, y + 1, z + 1);
                                    lightData.s00 = this.calcPerCornerSunlight(Direction.WEST, x, y, z);
                                    lightData.s01 = this.calcPerCornerSunlight(Direction.WEST, x, y, z + 1);
                                    lightData.s10 = this.calcPerCornerSunlight(Direction.WEST, x, y + 1, z);
                                    lightData.s11 = this.calcPerCornerSunlight(Direction.WEST, x, y + 1, z + 1);
                                    westPcld[z][y] = lightData;
                                }
                            }
                        }

                        if (eastNeighborChunk != null) {
                            BlockState eastNeighborBlk = eastNeighborChunk.get(eastNeighborX, y, z);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), eastNeighborBlk.getBlock())) {
                                eastMask[z][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(Direction.EAST, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(Direction.EAST, x, y, z + 1);
                                    lightData.l10 = this.calcPerCornerLight(Direction.EAST, x, y + 1, z);
                                    lightData.l11 = this.calcPerCornerLight(Direction.EAST, x, y + 1, z + 1);
                                    lightData.s00 = this.calcPerCornerSunlight(Direction.EAST, x, y, z);
                                    lightData.s01 = this.calcPerCornerSunlight(Direction.EAST, x, y, z + 1);
                                    lightData.s10 = this.calcPerCornerSunlight(Direction.EAST, x, y + 1, z);
                                    lightData.s11 = this.calcPerCornerSunlight(Direction.EAST, x, y + 1, z + 1);
                                    eastPcld[z][y] = lightData;
                                }
                            }
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }

            this.greedy(faces, Direction.EAST, shouldMerge, eastMask, eastPcld, x, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y, GreedyMesher.OFF_X);
            this.greedy(faces, Direction.WEST, shouldMerge, westMask, westPcld, x, GreedyMesher.OFF_Z, GreedyMesher.OFF_Y, GreedyMesher.OFF_X);
        }

        // North, south
        for (int z = 0; z < depth; z++) {
            boolean[][] northMask = new boolean[width][height];
            PerCornerLightData[][] northPcld = null;
            if (this.perCornerLight) {
                northPcld = new PerCornerLightData[width][height];
            }
            boolean[][] southMask = new boolean[width][height];
            PerCornerLightData[][] southPcld = null;
            if (this.perCornerLight) {
                southPcld = new PerCornerLightData[width][height];
            }
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    try {
                        BlockState curBlock = this.block(this.chunk, x, y, z);
                        if (curBlock == null) continue;
                        BlockModel blockModel = BlockModelRegistry.get().get(curBlock);
                        if (blockModel != null && !(blockModel instanceof BakedCubeModel)) {
                            continue;
                        }
                        if (!condition.shouldUse(curBlock)) continue;

                        int northNeighborZ = z + 1;
                        int southNeighborZ = z - 1;
                        @Nullable ClientChunkAccess northNeighborChunk = this.chunk;
                        @Nullable ClientChunkAccess southNeighborChunk = this.chunk;
                        if (northNeighborZ >= World.CS) {
                            northNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getVec().getIntX(), this.chunk.getVec().getIntY(), this.chunk.getVec().getIntZ() + 1);
                            northNeighborZ -= World.CS;
                        } else if (southNeighborZ < 0) {
                            southNeighborChunk = this.chunk.getWorld().getChunk(this.chunk.getVec().getIntX(), this.chunk.getVec().getIntY(), this.chunk.getVec().getIntZ() - 1);
                            southNeighborZ += World.CS;
                        }

                        if (northNeighborChunk != null) {
                            BlockState northNeighborBlock = northNeighborChunk.get(x, y, northNeighborZ);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), northNeighborBlock.getBlock())) {
                                northMask[x][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(Direction.NORTH, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(Direction.NORTH, x, y + 1, z);
                                    lightData.l10 = this.calcPerCornerLight(Direction.NORTH, x + 1, y, z);
                                    lightData.l11 = this.calcPerCornerLight(Direction.NORTH, x + 1, y + 1, z);
                                    lightData.s00 = this.calcPerCornerSunlight(Direction.NORTH, x, y, z);
                                    lightData.s01 = this.calcPerCornerSunlight(Direction.NORTH, x, y + 1, z);
                                    lightData.s10 = this.calcPerCornerSunlight(Direction.NORTH, x + 1, y, z);
                                    lightData.s11 = this.calcPerCornerSunlight(Direction.NORTH, x + 1, y + 1, z);
                                    northPcld[x][y] = lightData;
                                }
                            }
                        }

                        if (southNeighborChunk != null) {
                            BlockState southNeighborBlock = southNeighborChunk.get(x, y, southNeighborZ);
                            if (!ocCond.shouldOcclude(curBlock.getBlock(), southNeighborBlock.getBlock())) {
                                southMask[x][y] = true;

                                if (this.perCornerLight) {
                                    PerCornerLightData lightData = new PerCornerLightData();
                                    lightData.l00 = this.calcPerCornerLight(Direction.SOUTH, x, y, z);
                                    lightData.l01 = this.calcPerCornerLight(Direction.SOUTH, x, y + 1, z);
                                    lightData.l10 = this.calcPerCornerLight(Direction.SOUTH, x + 1, y, z);
                                    lightData.l11 = this.calcPerCornerLight(Direction.SOUTH, x + 1, y + 1, z);
                                    lightData.s00 = this.calcPerCornerSunlight(Direction.SOUTH, x, y, z);
                                    lightData.s01 = this.calcPerCornerSunlight(Direction.SOUTH, x, y + 1, z);
                                    lightData.s10 = this.calcPerCornerSunlight(Direction.SOUTH, x + 1, y, z);
                                    lightData.s11 = this.calcPerCornerSunlight(Direction.SOUTH, x + 1, y + 1, z);
                                    southPcld[x][y] = lightData;
                                }
                            }
                        }
                    } catch (PosOutOfBoundsException ex) {
                        QuantumClient.LOGGER.error("Greedy Meshing error:", ex);
                    }
                }
            }


            this.greedy(faces, Direction.NORTH, shouldMerge, northMask, northPcld, z, GreedyMesher.OFF_X, GreedyMesher.OFF_Y, GreedyMesher.OFF_Z);
            this.greedy(faces, Direction.SOUTH, shouldMerge, southMask, southPcld, z, GreedyMesher.OFF_X, GreedyMesher.OFF_Y, GreedyMesher.OFF_Z);
        }

        return faces;
    }

    private float calcLightLevel(Direction side, int x, int y, int z) throws PosOutOfBoundsException {
        LightLevelData result = this.calcLightLevels(side, x, y, z);
        if (result == null) return 1;

        return Math.min(1, result.sunBrightness() + result.blockBrightness());
    }

    @Nullable
    private LightLevelData calcLightLevels(Direction side, int x, int y, int z) {
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

        ClientWorldAccess world = Objects.requireNonNull(this.chunk).getWorld();
        int chunkSize = World.CS;
        @Nullable ClientChunkAccess sChunk = this.chunk;
        if (z < 0) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
            z += chunkSize;
        } else if (z > chunkSize - 1) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
            z -= chunkSize;
        } else if (y < 0) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
            y += chunkSize;
        } else if (y > chunkSize - 1) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
            y -= chunkSize;
        } else if (x < 0) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
            x += chunkSize;
        } else if (x > chunkSize - 1) {
            sChunk = world.getChunk(GreedyMesher.OFF_X + x, GreedyMesher.OFF_Y + y, GreedyMesher.OFF_Z + z);
            x -= chunkSize;
        }

        if (sChunk == null) return null;
        float sunBrightness = sChunk.getBrightness(this.sunlight(sChunk, x, y, z));
        float blockBrightness = sChunk.getBrightness(this.blockLight(sChunk, x, y, z));
        return new LightLevelData(sunBrightness, blockBrightness);
    }

    public List<Face> getFaces(UseCondition condition) {
        return this.getFaces(condition, this::shouldOcclude, this::shouldMerge);
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
    private void greedy(List<Face> outputList, Direction side, MergeCondition mergeCond, boolean[][] mask, PerCornerLightData[][] lightDatas, int z, int offsetX, int offsetY, int offsetZ) {
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

                    BlockState block = this.block(this.chunk, realX, realY, realZ);
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
                            BlockState newBlock = this.block(this.chunk, newRealX, newRealY, newRealZ);
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

                                    BlockState lblk = this.block(this.chunk, lRX, lRY, lRZ);
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

    public void meshFaces(List<Face> faces, ChunkModelBuilder builder) {
        for (Face f : faces) {
            f.render(builder);
        }
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
        // sx,sy,sz are the x, y, and z positions of the side block
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
                        fixedSz = World.CS + sz;
                        getChunk = true;
                    } else if (sz >= World.CS) {
                        fixedSz = sz - World.CS;
                        getChunk = true;
                    }
                    if (sy < 0) {
                        fixedSy = World.CS + sy;
                        getChunk = true;
                    } else if (sy >= World.CS) {
                        fixedSy = sy - World.CS;
                        getChunk = true;
                    }
                    if (sx < 0) {
                        fixedSx = World.CS + sx;
                        getChunk = true;
                    } else if (sx >= World.CS) {
                        fixedSx = sx - World.CS;
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
        // sx,sy,sz are the x, y, and z positions of the side block
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
                        fixedSz = World.CS + sz;
                        getChunk = true;
                    } else if (sz >= World.CS) {
                        fixedSz = sz - World.CS;
                        getChunk = true;
                    }
                    if (sy < 0) {
                        fixedSy = World.CS + sy;
                        getChunk = true;
                    } else if (sy >= World.CS) {
                        fixedSy = sy - World.CS;
                        getChunk = true;
                    }
                    if (sx < 0) {
                        fixedSx = World.CS + sx;
                        getChunk = true;
                    } else if (sx >= World.CS) {
                        fixedSx = sx - World.CS;
                        getChunk = true;
                    }
                    if (getChunk) {
                        sChunk = this.chunk.getWorld().getChunk(getChunkX, getChunkY, getChunkZ);
                    }
                    if (sChunk == null) continue;

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
        return !(shouldNotRenderNormally(blockToBlockFace) || blockToBlockFace.isTransparent() && bothOcclude || (BlockRenderPassRegistry.get(curBlock) != BlockRenderPassRegistry.get(blockToBlockFace) && !bothOcclude));
    }

    private boolean shouldMerge(BlockState id1, float light1, PerCornerLightData lightData1, BlockState id2, float light2, PerCornerLightData lightData2) {
        if (!id1.getBlock().shouldGreedyMerge()) return false;

        boolean sameBlock = Objects.equals(id1, id2);
        boolean sameLight = light1 == light2;

        if (this.perCornerLight) {
            sameLight = lightData1.equals(lightData2);
        }

        // Other block renderers may alter shape in an unpredictable way
        boolean considerAsSame = sameLight && !sameBlock
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

        public float sunBrightness() {
            return sunBrightness;
        }

        public float blockBrightness() {
            return blockBrightness;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (LightLevelData) obj;
            return Float.floatToIntBits(this.sunBrightness) == Float.floatToIntBits(that.sunBrightness) &&
                    Float.floatToIntBits(this.blockBrightness) == Float.floatToIntBits(that.blockBrightness);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sunBrightness, blockBrightness);
        }

        @Override
        public String toString() {
            return "LightLevelData[" +
                    "sunBrightness=" + sunBrightness + ", " +
                    "blockBrightness=" + blockBrightness + ']';
        }

    }

    public static class Face {

        private final Direction side;
        private final int x1, y1, x2, y2, z;
        private final float lightLevel;
        private final PerCornerLightData lightData;
        private final BlockRenderer renderer;
        private final BakedCubeModel bakedBlockModel;
        private final float sunlightLevel;
        private final int[] ao;
        private final RenderPass pass;

        /**
         * @param lightData     Per corner light data. Pass null if per corner lighting is disabled.
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
            this.lightData = lightData;
            this.ao = new int[]{16, 16, 16, 16, 16, 16};

            this.pass = BlockRenderPassRegistry.get(block.getBlock());

            QuantumClient client = QuantumClient.get();
            this.renderer = BlockRendererRegistry.get(block.getBlock());
            this.bakedBlockModel = (BakedCubeModel) client.getBlockModel(block);
        }

        public void render(ChunkModelBuilder builder) {
            LightLevelData lld = new LightLevelData(this.lightLevel, this.sunlightLevel);
            if (this.bakedBlockModel == null) return;
            switch (this.side) {
                case UP ->
                        this.renderer.renderTop(this.bakedBlockModel.top(), this.x1, this.y1, this.x2, this.y2, this.z + 1, lld, this.lightData, ao, builder.get(pass));
                case DOWN ->
                        this.renderer.renderBottom(this.bakedBlockModel.bottom(), this.x1, this.y1, this.x2, this.y2, this.z, lld, this.lightData, ao, builder.get(pass));
                case NORTH ->
                        this.renderer.renderNorth(this.bakedBlockModel.north(), this.x1, this.y1, this.x2, this.y2, this.z + 1, lld, this.lightData, ao, builder.get(pass));
                case SOUTH ->
                        this.renderer.renderSouth(this.bakedBlockModel.south(), this.x1, this.y1, this.x2, this.y2, this.z, lld, this.lightData, ao, builder.get(pass));
                case EAST ->
                        this.renderer.renderEast(this.bakedBlockModel.east(), this.x1, this.y1, this.x2, this.y2, this.z + 1, lld, this.lightData, ao, builder.get(pass));
                case WEST ->
                        this.renderer.renderWest(this.bakedBlockModel.west(), this.x1, this.y1, this.x2, this.y2, this.z, lld, this.lightData, ao, builder.get(pass));
            }
        }

    }

    // Find "real" x based on relative position in the greedy method
    private int realX(Direction side, int x, int y, int z) {
        return switch (side) {
            case UP, DOWN, NORTH, SOUTH -> x;
            case EAST, WEST -> z;
        };
    }

    // Find "real" y based on relative position in the greedy method
    private int realY(Direction side, int x, int y, int z) {
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

    public interface OccludeCondition {
        /**
         * @param curBlock    current block being checked
         * @param facingBlock block in the facing direction
         * @return {@code true} if the side of the curBlock should be occluded, {@code false} otherwise
         */
        boolean shouldOcclude(Block curBlock, Block facingBlock);
    }

    public interface MergeCondition {
        boolean shouldMerge(BlockState data1, float light1, PerCornerLightData lightData1, BlockState data2, float light2, PerCornerLightData lightData2);
    }

    private BlockState block(@NotNull ClientChunkAccess chunk, int x, int y, int z) {
        ClientWorldAccess world = chunk.getWorld();
        this.tmp3i.set(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ()).mul(16).add(x, y, z);
        @NotNull ClientChunkAccess chunkAt = world.getChunkAt(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z);
        if (chunkAt != null)
            return chunkAt.get(World.toLocalBlockVec(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return Blocks.AIR.getDefaultState();
    }

    private int blockLight(@NotNull ClientChunkAccess chunk, int x, int y, int z) {
        ClientWorldAccess world = chunk.getWorld();
        this.tmp3i.set(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ()).mul(16).add(x, y, z);
        @NotNull ClientChunkAccess chunkAt = world.getChunkAt(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z);
        if (chunkAt != null)
            return chunkAt.getBlockLight(World.toLocalBlockVec(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return 0;
    }

    private int sunlight(@NotNull ClientChunkAccess chunk, int x, int y, int z) {
        ClientWorldAccess world = chunk.getWorld();
        this.tmp3i.set(chunk.getVec().getIntX(), chunk.getVec().getIntY(), chunk.getVec().getIntZ()).mul(16).add(x, y, z);
        @NotNull ClientChunkAccess chunkAt = world.getChunkAt(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z);
        if (chunkAt != null)
            return chunkAt.getSunlight(World.toLocalBlockVec(this.tmp3i.x, this.tmp3i.y, this.tmp3i.z, this.tmp3i));
        return 0;
    }
}