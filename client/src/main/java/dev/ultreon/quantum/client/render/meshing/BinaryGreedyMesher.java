package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;
import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.world.Direction;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;

public class BinaryGreedyMesher implements Mesher {
    private final ClientChunk chunk;

    public BinaryGreedyMesher(ClientChunk chunk) {
        this.chunk = chunk;
    }

    private static final int CS = World.CS - 2;
    private static final int CS_2 = CS * CS;
    private static final int CS_P = CS + 2;
    private static final int CS_P2 = CS_P * CS_P;
    private static final int CS_P3 = CS_P2 * CS_P;

    private static final long P_MASK = ~(1L << 63 | 1);

    @SuppressWarnings("deprecation")
    public static BlockVec getAxisIndex(int axis, int a, int b, int c) {
        if (axis == 0) return new BlockVec(a, b, c);
        else if (axis == 1) return new BlockVec(c, a, b);
        else return new BlockVec(b, c, a);
    }
    @SuppressWarnings("t")
    @Override
    public boolean buildMesh(UseCondition condition, ChunkModelBuilder builder) {
        int vertexI = 0;
        long[] faceMasks = new long[CS_2 * 6];
        int[] opaqueMask = this.chunk.getOpaqueMask();
        byte[] forwardMerged = new byte[CS_P2];
        byte[] rightMerged = new byte[CS_P];

        int[] faceVertexBegin = new int[6];
        int[] faceVertexLength = new int[6];

        // Face mask generation logic here (left unchanged)
        for (int a = 1; a < CS_P - 1; a++) {
            final int aCS_P = a * CS_P;

            for (int b = 1; b < CS_P - 1; b++) {
                final long columnBits = (opaqueMask[chunk.index(a, b)] & P_MASK);
                final int baIndex = (b - 1) + (a - 1) * CS;
                final int abIndex = (a - 1) + (b - 1) * CS;

                faceMasks[baIndex] = (columnBits & ~(long) opaqueMask[aCS_P + CS_P + b]) >> 1;
                faceMasks[baIndex + CS_2] = (columnBits & ~(long) opaqueMask[aCS_P - CS_P + b]) >> 1;

                faceMasks[abIndex + 2 * CS_2] = (columnBits & ~(long) opaqueMask[aCS_P + (b + 1)]) >> 1;
                faceMasks[abIndex + 3 * CS_2] = (columnBits & ~(long) opaqueMask[aCS_P + (b - 1)]) >> 1;

                faceMasks[baIndex + 4 * CS_2] = columnBits & ~((long) opaqueMask[aCS_P + b] >> 1);
                faceMasks[baIndex + 5 * CS_2] = columnBits & ~((long) opaqueMask[aCS_P + b] << 1);
            }
        }

        // Greedy meshing faces 0-3
        for (int face = 0; face < 4; face++) {
            int axis = face / 2;
            faceVertexBegin[face] = vertexI;

            for (int layer = 0; layer < CS; layer++) {
                int bitsLocation = layer * CS + face * CS_2;

                for (int forward = 0; forward < CS; forward++) {
                    long bitsHere = faceMasks[forward + bitsLocation];
                    if (bitsHere == 0) continue;

                    long bitsNext = (forward + 1 < CS) ? faceMasks[(forward + 1) + bitsLocation] : 0;

                    int rightMergedCount = 1;
                    while (bitsHere != 0) {
                        int bitPos = Long.numberOfTrailingZeros(bitsHere);
                        long mask = 1L << bitPos;

                        @NotNull BlockState type = chunk.get(getAxisIndex(axis, forward + 1, bitPos + 1, layer + 1));
                        byte forwardMergedRef = forwardMerged[bitPos];

                        if (((bitsNext >> bitPos) & 1) != 0 &&
                            type == chunk.get(getAxisIndex(axis, forward + 2, bitPos + 1, layer + 1))) {
                            forwardMerged[bitPos]++;
                            bitsHere &= ~mask;
                            continue;
                        }

                        for (int right = bitPos + 1; right < CS; right++) {
                            if (canMerge(forwardMerged[right], bitsHere, rightMergedCount, type, axis, right, forward)) break;

                            forwardMerged[right] = 0;
                            rightMergedCount++;
                        }

                        bitsHere &= -(1L << (bitPos + rightMergedCount));

                        int meshFront = forward - forwardMergedRef;
                        int meshUp = layer + ((~face) & 1);

                        int meshWidth = rightMergedCount;
                        int meshLength = forwardMergedRef + 1;

                        forwardMerged[bitPos] = 0;
                        rightMergedCount = 1;

                        GreedyMesher.Face f = new GreedyMesher.Face(
                                switch (face) {
                                    case 0 -> Direction.EAST;
                                    case 1 -> Direction.WEST;
                                    case 2 -> Direction.SOUTH;
                                    case 3 -> Direction.NORTH;
                                    default -> throw new IllegalStateException("Unexpected value: " + face);
                                },
                                type,
                                new int[]{16, 16, 16, 16, 16, 16},
                                1f,
                                null,
                                bitPos,
                                meshFront,
                                bitPos + meshWidth,
                                meshFront + meshLength,
                                meshUp,
                                1f
                        );

                        f.bake(builder);
                    }
                }
            }

            faceVertexLength[face] = vertexI - faceVertexBegin[face];
        }

        // Greedy meshing faces 4-5
        for (int face = 4; face < 6; face++) {
            int axis = face / 2;
            faceVertexBegin[face] = vertexI;

            for (int forward = 0; forward < CS; forward++) {
                int bitsLocation = forward * CS + face * CS_2;
                int bitsForwardLocation = (forward + 1) * CS + face * CS_2;

                for (int right = 0; right < CS; right++) {
                    long bitsHere = faceMasks[right + bitsLocation];
                    if (bitsHere == 0) continue;

                    long bitsForward = forward < CS - 1 ? faceMasks[right + bitsForwardLocation] : 0;
                    long bitsRight = right < CS - 1 ? faceMasks[right + 1 + bitsLocation] : 0;
                    int rightCS = right * CS;

                    while (bitsHere != 0) {
                        int bitPos = Long.numberOfTrailingZeros(bitsHere);
                        bitsHere &= ~(1L << bitPos);

                        @NotNull BlockState type = chunk.get(getAxisIndex(axis, right + 1, forward + 1, bitPos));
                        int forwardMergedIndex = rightCS + (bitPos - 1);
                        int rightMergedIndex = bitPos - 1;

                        if (canMerge(forwardMerged[forwardMergedIndex], bitsRight, rightMergedIndex, type, axis, right, forward)) {
                            forwardMerged[forwardMergedIndex]++;
                            continue;
                        }

                        if (((bitsRight >>> bitPos) & 1) != 0 &&
                            forwardMerged[forwardMergedIndex] == forwardMerged[rightCS + CS + (bitPos - 1)] &&
                            type == chunk.get(getAxisIndex(axis, right + 2, forward + 1, bitPos))) {
                            forwardMerged[forwardMergedIndex] = 0;
                            rightMerged[rightMergedIndex]++;
                            continue;
                        }

                        int meshLeft = right - rightMerged[rightMergedIndex];
                        int meshFront = forward - forwardMerged[forwardMergedIndex];
                        int meshUp = bitPos - 1 + (~face & 1);

                        int meshWidth = 1 + rightMerged[rightMergedIndex];
                        int meshLength = 1 + forwardMerged[forwardMergedIndex];

                        forwardMerged[forwardMergedIndex] = 0;
                        rightMerged[rightMergedIndex] = 0;

                        GreedyMesher.Face f = new GreedyMesher.Face(
                                switch (face) {
                                    case 4 -> Direction.UP;
                                    case 5 -> Direction.DOWN;
                                    default -> throw new IllegalStateException("Unexpected value: " + face);
                                },
                                type,
                                new int[]{16, 16, 16, 16, 16, 16},
                                1f,
                                null,
                                bitPos,
                                meshFront,
                                bitPos + meshWidth,
                                meshFront + meshLength,
                                meshUp,
                                1f
                        );

                        f.bake(builder);
                    }
                }
            }

            faceVertexLength[face] = vertexI - faceVertexBegin[face];
        }

        chunk.meshVertices = vertexI + 1;
        return true;
    }

    private boolean canMerge(byte rightMergedRef, long bitsForward, int bitPos, BlockState type, int axis, int right, int forward) {
        return rightMergedRef == 0 &&
               ((bitsForward >> bitPos) & 1) != 0 &&
               type == chunk.get(getAxisIndex(axis, right + 1, forward + 2, bitPos));
    }
}
