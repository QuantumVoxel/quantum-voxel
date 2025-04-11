package dev.ultreon.quantum.world.gen.chunk;

import dev.ultreon.quantum.block.Blocks;
import dev.ultreon.quantum.registry.ServerRegistry;
import dev.ultreon.quantum.tags.NamedTag;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.*;
import dev.ultreon.quantum.world.gen.biome.BiomeGenerator;
import dev.ultreon.quantum.world.gen.carver.Carver;
import dev.ultreon.quantum.world.gen.noise.DomainWarping;
import dev.ultreon.quantum.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static dev.ultreon.quantum.world.World.CS;

/**
 * The OverworldGenerator is responsible for generating the terrain of the Overworld.
 * It uses various noise configurations and biome data to create diverse and immersive biomes.
 * It extends the SimpleChunkGenerator, inheriting its basic functionalities and providing further customization.
 */
public class DebugGenerator implements ChunkGenerator {

    private NoiseConfig noiseConfig;
    private final NamedTag<Biome> biomeTag;

    private BiomeGenerator voidBiome;

    public DebugGenerator(ServerRegistry<Biome> biomeRegistry) {
        super();

        this.biomeTag = biomeRegistry.getTag(new NamespaceID("overworld_biomes")).orElseThrow();
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        this.voidBiome = world.getServer().getBiomes().void_.create(world, world.getSeed());
    }

    @Override
    public void generate(@NotNull ServerWorld world, BuilderChunk chunk, Collection<ServerWorld.@NotNull RecordedChange> changes) {
        fillBiome(chunk);

        if (chunk.vec.equals(0, 0, 0)) {
            sphere(chunk);
        } else if (chunk.vec.equals(2, 0, 0)) {
            cube(chunk);
        } else if (chunk.vec.equals(0, 0, 2)) {
            pyramid(chunk);
        } else if (chunk.vec.equals(2, 0, 2)) {
            cone(chunk);
        } else if (chunk.vec.equals(0, 2, 0)) {
            cylinder(chunk);
        } else if (chunk.vec.equals(2, 2, 0)) {
            checkerboard(chunk);
        }

        if (chunk.vec.y <= -1) {
            for (int x = 0; x < CS; x++) {
                for (int y = 0; y < CS; y++) {
                    for (int z = 0; z < CS; z++) {
                        chunk.set(x, y, z, Blocks.VOIDGUARD.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public DomainWarping getLayerDomain() {
        return null;
    }

    @Override
    public Carver getCarver() {
        return new VoidCarver();
    }

    private void random(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    if (Math.random() < 0.1) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private void checkerboard(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    if (x % 2 == 0 && y % 2 == 0 && z % 2 == 0) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void sphere(@NotNull BuilderChunk chunk) {
        int centerX = CS / 2;
        int centerZ = CS / 2;
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    double distance = Math.sqrt((x - centerX) * (x - centerX) + (y - CS / 2) * (y - CS / 2) + (z - centerZ) * (z - centerZ));
                    if (distance <= CS / 2 - 2) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void cube(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    if (x == 0 || x == CS - 1 || y == 0 || y == CS - 1 || z == 0 || z == CS - 1) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void pyramid(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    if (x == 0 || x == CS - 1 || y == 0 || y == CS - 1 || z == 0) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void cone(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    int distance = Math.max(Math.abs(x - CS / 2), Math.abs(z - CS / 2));
                    if (distance <= CS - y - 2) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void cylinder(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int y = 0; y < CS; y++) {
                for (int z = 0; z < CS; z++) {
                    int distance = Math.max(Math.abs(x - CS / 2), Math.abs(z - CS / 2));
                    if (distance <= CS / 2 - 2) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private void fillBiome(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < CS; x++) {
            for (int z = 0; z < CS; z++) {
                chunk.setBiomeGenerator(x, z, this.voidBiome);
            }
        }
    }

    @Override
    public double getTemperature(int x, int z) {
        return 1.0;
    }

    @Override
    public void dispose() {

    }
}
