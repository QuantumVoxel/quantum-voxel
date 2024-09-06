package dev.ultreon.quantum.world.gen.layer;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.world.BlockSetter;
import dev.ultreon.quantum.world.ServerWorld;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.rng.RNG;
import org.jetbrains.annotations.ApiStatus;

/**
 * Abstract class representing a single layer in terrain generation.
 * This class is used to define how blocks are set within a specific layer
 * during the construction of a chunk in the world generation process.
 */
public abstract class TerrainLayer implements Disposable {
    /**
     * Handles the setting of blocks in a specific layer during the generation of a chunk in the world.
     *
     * @param world  The world where the chunk resides.
     * @param chunk  The chunk being generated.
     * @param rng    The random number generator used for terrain features.
     * @param x      The x-coordinate within the chunk.
     * @param y      The y-coordinate within the chunk.
     * @param z      The z-coordinate within the chunk.
     * @param height The height at which the layer is being generated.
     * @return True if the handling is successful, false otherwise.
     */
    @ApiStatus.OverrideOnly
    public abstract boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height);

    public void create(ServerWorld world) {

    }

    @Override
    public void dispose() {

    }
}
