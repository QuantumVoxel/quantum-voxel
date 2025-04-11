package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.block.state.BlockState;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;

/**
 * Turns an array of voxels into OpenGL vertices
 */
public interface Mesher {

    /**
     * Meshes the specified voxels.
     *
     * @param builder   MeshBuilder to build the mesh onto
     * @param condition Condition to check if the block should be used in the mesh
     * @return
     */
    boolean meshVoxels(UseCondition condition, ChunkModelBuilder builder);

    interface UseCondition {
        /**
         * @param block Block to check
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(BlockState block);
    }

}