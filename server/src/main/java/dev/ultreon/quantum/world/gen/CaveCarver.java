package dev.ultreon.quantum.world.gen;

import com.badlogic.gdx.math.MathUtils;
import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.pipeline.JNoise;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.ServerWorld.Region;

public class CaveCarver {
    private JNoise noiseGenerator;
    private Region region;

    public CaveCarver(ServerWorld.Region region) {
        this.region = region;
        this.noiseGenerator = JNoise.newBuilder().perlin(region.pos().seed(), Interpolation.LINEAR, FadeFunction.NONE).build();
    }

    public void generateCaves() {
        int wormCount = 100;
        int wormLength = 100;
        double stepSize = 1.0f;
        double radius = 2.0f;

        for (int i = 0; i < wormCount; i++) {
            Vec3d position = getRandomStartingPosition();
            Vec3d direction = getRandomDirection();

            for (int j = 0; j < wormLength; j++) {
                carveOutSphere(position, radius);
                adjustDirectionWithPerlinNoise(position, direction);
                position.add(direction.scl(stepSize));

                // Break the loop if the worm goes out of the chunk bounds
                if (!isWithinChunkBounds(position)) {
                    break;
                }
            }
        }
    }

    private void carveOutSphere(Vec3d position, double radius) {
        int startX = (int) Math.floor(position.x - radius);
        int startY = (int) Math.floor(position.y - radius);
        int startZ = (int) Math.floor(position.z - radius);
        int endX = (int) Math.ceil(position.x + radius);
        int endY = (int) Math.ceil(position.y + radius);
        int endZ = (int) Math.ceil(position.z + radius);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    Vec3d currentPos = new Vec3d(x, y, z);
                    if (currentPos.dst(position) <= radius) {
                        setCave(x, y, z); // Carve out by setting the voxel to AIR
                    }
                }
            }
        }
    }

    private void adjustDirectionWithPerlinNoise(Vec3d position, Vec3d direction) {
        double noiseScale = 0.1f;
        double angleX = noiseGenerator.evaluateNoise(position.x, position.y, position.z) * noiseScale;
        double angleY = noiseGenerator.evaluateNoise(position.y, position.z, position.x) * noiseScale;
        double angleZ = noiseGenerator.evaluateNoise(position.z, position.x, position.y) * noiseScale;

        direction.add(angleX, angleY, angleZ).nor();
    }

    private Vec3d getRandomStartingPosition() {
        double x = MathUtils.random(region.getStartX(), region.getEndX());
        double y = MathUtils.random(region.getStartY(), region.getEndY());
        double z = MathUtils.random(region.getStartZ(), region.getEndZ());
        return new Vec3d(x, y, z);
    }

    private Vec3d getRandomDirection() {
        double angleX = MathUtils.random(-1f, 1f);
        double angleY = MathUtils.random(-1f, 1f);
        double angleZ = MathUtils.random(-1f, 1f);
        return new Vec3d(angleX, angleY, angleZ).nor();
    }

    private boolean isWithinChunkBounds(Vec3d position) {
        return position.x >= region.getStartX() && position.x <= region.getEndX() &&
               position.y >= region.getStartY() && position.y <= region.getEndY() &&
               position.z >= region.getStartZ() && position.z <= region.getEndZ();
    }

    private void setCave(int x, int y, int z) {
        if (region.isWithinBounds(x, y, z)) {
            region.setCave(x, y, z);
        }
    }
}
