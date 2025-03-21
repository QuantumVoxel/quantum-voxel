package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;

/**
 * Turns an array of voxels into OpenGL vertices
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface Mesher {
    /**
     * Builds a mesh based on the given condition and MeshPartBuilder.
     *
     * @param condition The condition to determine which blocks should be used for the mesh.
     * @param builder   The MeshPartBuilder to construct the mesh.
     * @return
     */
    boolean buildMesh(UseCondition condition, ChunkModelBuilder builder);

    /**
     * Determines whether a block should be used in the mesh.
     */
    interface UseCondition {
        /**
         * @param block Block to check
         * @param model
         * @param pass
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(Block block, BlockModel model, RenderPass pass);
    }

}