package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import dev.ultreon.quantum.client.render.meshing.GreedyMesher;
import dev.ultreon.quantum.client.render.meshing.PerCornerLightData;
import dev.ultreon.quantum.client.render.pipeline.WorldRenderNode;

/**
 * The BlockRenderer interface provides methods for rendering the various faces of a block
 * in a 3D environment. Implementations of this interface are responsible for generating
 * the mesh data for each face of the block based on the given texture region and light data.
 * 
 * <p>
 * This is a part of the render pipeline. It is used to render the block faces. 
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @see WorldRenderNode
 */
@Deprecated
public interface BlockRenderer {

    /**
     * Renders the north face of a block using the specified texture region and mesh data.
     * Accepts a texture region, and the coordinates of the face. And a mesh builder to build the mesh.
     * This is used to render the north face(s) of a block.
     *
     * @param region     the texture region to use for this face of the block
     * @param x1         the x-coordinate of the first vertex of the face
     * @param y1         the y-coordinate of the first vertex of the face
     * @param x2         the x-coordinate of the second vertex of the face
     * @param y2         the y-coordinate of the second vertex of the face
     * @param z          the z-coordinate for both vertices of the face
     * @param lightLevel the light level data to be used for shading calculations
     * @param lightData  the per-corner light data to be used for shading calculations
     * @param ao
     * @param builder    the mesh part builder used to create the mesh for the face
     */
    void renderNorth(TextureRegion region, float x1, float y1, float x2, float y2, float z, GreedyMesher.LightLevelData lightLevel, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder);

    /**
     * Renders the south face of a block in a 3D environment using the given texture region and light data.
     * Accepts a texture region, and the coordinates of the face. And a mesh builder to build the mesh.
     * This is used to render the south face(s) of a block.
     *
     * @param region     the texture region to use for the face
     * @param x1         the x-coordinate of the first corner of the face
     * @param y1         the y-coordinate of the first corner of the face
     * @param x2         the x-coordinate of the second corner of the face
     * @param y2         the y-coordinate of the second corner of the face
     * @param z          the z-coordinate of the face
     * @param lightLevel the light level data for the face
     * @param lightData  the per-corner light data
     * @param ao
     * @param builder    the mesh part builder to store the generated mesh data
     */
    void renderSouth(TextureRegion region, float x1, float y1, float x2, float y2, float z, GreedyMesher.LightLevelData lightLevel, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder);

    /**
     * Renders the west face of a block using the specified texture region, coordinates, light data,
     * and mesh builder.
     * Accepts a texture region, and the coordinates of the face. And a mesh builder to build the mesh.
     * This is used to render the west face(s) of a block.
     *
     * @param region     the texture region to use for the face
     * @param z1         the starting z-coordinate of the face
     * @param y1         the starting y-coordinate of the face
     * @param z2         the ending z-coordinate of the face
     * @param y2         the ending y-coordinate of the face
     * @param x          the constant x-coordinate where the face is rendered
     * @param lightLevel the light level data for general light conditions
     * @param lightData  the light data for each corner of the face
     * @param ao
     * @param builder    the mesh part builder used to construct the mesh for the face
     */
    void renderWest(TextureRegion region, float z1, float y1, float z2, float y2, float x, GreedyMesher.LightLevelData lightLevel, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder);

    /**
     * Renders the east face of a block using the provided texture region, coordinates, light data, and mesh builder.
     * Accepts a texture region, and the coordinates of the face. And a mesh builder to build the mesh.
     * This is used to render the east face(s) of a block.
     *
     * @param region     The texture region to use for rendering the block face.
     * @param z1         The starting z-coordinate of the block face.
     * @param y1         The starting y-coordinate of the block face.
     * @param z2         The ending z-coordinate of the block face.
     * @param y2         The ending y-coordinate of the block face.
     * @param x          The x-coordinate of the block face.
     * @param lightLevel The light level data to use for rendering.
     * @param lightData  The per-corner light data to use for rendering.
     * @param ao
     * @param builder    The mesh part builder to construct the mesh data.
     */
    void renderEast(TextureRegion region, float z1, float y1, float z2, float y2, float x, GreedyMesher.LightLevelData lightLevel, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder);

    /**
     * Renders the top face of a block.
     * Accepts a texture region, and the coordinates of the face. And a mesh builder to build the mesh.
     * This is used to render the top face(s) of a block.
     *
     * @param region     The texture region to apply for this face.
     * @param x1         The starting x-coordinate for this face.
     * @param z1         The starting z-coordinate for this face.
     * @param x2         The ending x-coordinate for this face.
     * @param z2         The ending z-coordinate for this face.
     * @param y          The y-coordinate level for this face.
     * @param lightLevel The light level data to be used for rendering.
     * @param lightData  The per-corner light data for this face.
     * @param ao
     * @param builder    The mesh part builder where the face geometry is constructed.
     */
    void renderTop(TextureRegion region, float x1, float z1, float x2, float z2, float y, GreedyMesher.LightLevelData lightLevel, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder);

    /**
     * Renders the bottom face of a block in a 3D environment based on the given texture region
     * and light data.
     * Accepts a texture region, and the coordinates of the face. And a mesh builder to build the mesh.
     * This is used to render the bottom face(s) of a block.
     *
     * @param region     The texture region to use for the bottom face of the block.
     * @param x1         The starting x-coordinate of the bottom face.
     * @param z1         The starting z-coordinate of the bottom face.
     * @param x2         The ending x-coordinate of the bottom face.
     * @param z2         The ending z-coordinate of the bottom face.
     * @param y          The y-coordinate level at which to render the bottom face.
     * @param lightLevel Lighting information for the entire face.
     * @param lightData  Per-corner lighting information.
     * @param ao
     * @param builder    The mesh part builder used to construct the mesh for the bottom face.
     */
    void renderBottom(TextureRegion region, float x1, float z1, float x2, float z2, float y, GreedyMesher.LightLevelData lightLevel, PerCornerLightData lightData, int[] ao, MeshPartBuilder builder);
}