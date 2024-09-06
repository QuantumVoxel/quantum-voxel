package dev.ultreon.quantum.world.gen.carver;

import dev.ultreon.quantum.world.BuilderChunk;

/**
 * Carver is an interface used to define the methods necessary for carving terrain in a chunk.
 */
public interface Carver {
    /**
     * Carves the terrain within the given chunk at specified coordinates based on hilliness.
     *
     * @param chunk The chunk in which the terrain carving is to be performed.
     * @param x The x-coordinate within the chunk where the carving starts.
     * @param z The z-coordinate within the chunk where the carving starts.
     * @return The height of the terrain at the given coordinates after carving, returns -1 if undetermined.
     */
    int carve(BuilderChunk chunk, int x, int z);

    /**
     * Computes the height of the terrain surface noise at the specified coordinates.
     *
     * @param x The x-coordinate to compute the surface height noise.
     * @param z The z-coordinate to compute the surface height noise.
     * @return The height of the surface noise at the specified coordinates.
     */
    int getSurfaceHeightNoise(float x, float z);

    /**
     * Determines if the block at the specified coordinates (x, y, z) is air.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     * @return true if the block at the given coordinates is air, false otherwise.
     */
    boolean isAir(int x, int y, int z);
}
