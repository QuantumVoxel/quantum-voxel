package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.block.state.BlockData;

/**
 * Turns an array of voxels into OpenGL vertices
 */
public interface Mesher {

    /**
     * Meshes the specified voxels.
     *
     * @param meshBuilder   MeshBuilder to build the mesh onto
     * @param condition Condition to check if the block should be used in the mesh
     */
    void meshVoxels(MeshPartBuilder meshBuilder, UseCondition condition);

    interface UseCondition {
        /**
         * @param block Block to check
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(BlockData block);
    }

}