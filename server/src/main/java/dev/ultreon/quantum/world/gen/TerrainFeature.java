package dev.ultreon.quantum.world.gen;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.block.BlockState;
import dev.ultreon.quantum.world.Fork;
import dev.ultreon.quantum.world.ServerWorld;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The WorldGenFeature abstract class defines the structure for world generation features.
 * Classes that extend WorldGenFeature must implement the handle method to specify
 * how to generate features in a world.
 */
public abstract class TerrainFeature implements Disposable {
    /**
     * Determines whether the feature should be placed at the specified coordinates.
     *
     * @param x the x-coordinate in the world
     * @param y the y-coordinate in the world
     * @param z the z-coordinate in the world
     * @param origin the original block state at the specified coordinates
     * @return true if the feature should be placed, false otherwise
     */
    public boolean shouldPlace(int x, int y, int z, @NotNull BlockState origin) {
        return true;
    }

    /**
     * Handles the generation of a world feature at a specific location within a chunk.
     *
     * @param setter  the world in which the feature is being generated
     * @param seed
     * @param x      the x-coordinate of the location within the chunk
     * @param y      the y-coordinate of the location within the chunk
     * @param z      the z-coordinate of the location within the chunk
     * @return true if the feature was successfully generated, false otherwise
     */
    @ApiStatus.OverrideOnly
    public abstract boolean handle(@NotNull Fork setter, long seed, int x, int y, int z);

    /**
     * Create the world generator feature in the given world.
     * <p>NOTE: Always override {@link #dispose()} to avoid memory leaks.</p>
     *
     * @param world the world to create the feature in.
     */
    @ApiStatus.OverrideOnly
    public void create(ServerWorld world) {

    }

    /**
     * Dispose the feature when the world is being unloaded.
     */
    @Override
    @ApiStatus.OverrideOnly
    public void dispose() {

    }
}
