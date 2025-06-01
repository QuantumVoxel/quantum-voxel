package dev.ultreon.quantum.client.render.meshing;

import dev.ultreon.quantum.block.BlockLike;
import dev.ultreon.quantum.client.model.block.BlockModel;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.world.ChunkModelBuilder;
import org.jetbrains.annotations.Nullable;

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
        boolean shouldUse(@Nullable BlockLike block, BlockModel model, RenderPass pass);
    }

}