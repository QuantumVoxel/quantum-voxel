package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec2i;
import dev.ultreon.quantum.world.Chunk;
import dev.ultreon.quantum.world.Double2BooleanFunction;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import dev.ultreon.quantum.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static dev.ultreon.quantum.world.World.CS;

public class TreeGenerator {
    private NoiseInstance noise;

    @NotNull
    private final NoiseConfig treeNoiseConfig;

    @NotNull
    private final DomainWarping domainWrapping;

    public TreeGenerator(@NotNull NoiseConfig treeNoiseConfig, @NotNull DomainWarping domainWrapping) {
        this.treeNoiseConfig = treeNoiseConfig;
        this.domainWrapping = domainWrapping;
    }

    public void create(long seed) {
        this.noise = this.treeNoiseConfig.create(seed);
    }

    public static List<Vec2i> findLocalMaxima(double[][] dataMatrix, int x, int z) {
        ArrayList<Vec2i> maxima = new ArrayList<Vec2i>();
        for (int matrixX = 0; matrixX < dataMatrix.length; matrixX++) {
            for (int matrixZ = 0; matrixZ < dataMatrix[matrixX].length; matrixZ++) {
                double noiseVal = dataMatrix[matrixX][matrixZ];
                if (TreeGenerator.checkNeighbours(dataMatrix, matrixX, matrixZ, (neighbourNoise) -> neighbourNoise < noiseVal)) {
                    maxima.add(new Vec2i(x + matrixX, z + matrixZ));
                }

            }
        }
        return maxima;
    }

    private static boolean checkNeighbours(double[][] matrix, int x, int y, Double2BooleanFunction successCondition) {
        for (Neighbour8Direction direction : Neighbour8Direction.values()) {
            Vec2i dir = direction.vec();
            Vec2f newPost = new Vec2f(x + dir.x, y + dir.y);

            if (newPost.x < 0 || newPost.x >= matrix.length || newPost.y < 0 || newPost.y >= matrix[0].length) {
                continue;
            }

            if (successCondition.apply(matrix[x + dir.x][y + dir.y])) {
                return false;
            }
        }
        return true;
    }

    public TreeData generateTreeData(Chunk chunkData) {
        NoiseInstance noise = this.noise;
        TreeData treeData = new TreeData();

        return treeData;
    }

    private double[][] generateMatrix(Chunk chunkData, NoiseInstance noise) {
        double[][] noiseMax = new double[CS][CS];
        int xMax = chunkData.getOffset().x + CS;
        int xMin = chunkData.getOffset().x;
        int zMax = chunkData.getOffset().z + CS;
        int zMin = chunkData.getOffset().z;
        int xIndex = 0, zIndex = 0;

        for (int x = xMin; x < xMax; x++) {
            for (int z = zMin; z < zMax; z++) {
                noiseMax[xIndex][zIndex] = this.domainWrapping.generateDomainNoise(x, z, noise);
                zIndex++;
            }

            xIndex++;
            zIndex = 0;
        }

        return noiseMax;
    }

}
