package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.quantum.block.Block;

import java.util.List;

/**
 * Turns an array of voxels into OpenGL vertices
 */
public interface Mesher {

    /**
     * Meshes the specified voxels.
     *
     * @param condition Condition to check if the block should be used in the mesh
     * @return
     */
    List<GreedyMesher.Face> prepare(UseCondition condition);

    void meshFaces(List<GreedyMesher.Face> faces, MeshPartBuilder builder);

    interface UseCondition {
        /**
         * @param block Block to check
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(Block block);
    }

}