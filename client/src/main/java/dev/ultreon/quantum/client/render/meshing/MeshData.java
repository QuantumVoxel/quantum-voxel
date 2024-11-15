package dev.ultreon.quantum.client.render.meshing;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import dev.ultreon.quantum.world.Direction;

import java.util.EnumMap;
import java.util.Map;

public class MeshData {
    private MeshPartBuilder builder;

    // Cached vertex positions for each direction
    private final Map<Direction, Vector3[]> cachedVertices = new EnumMap<>(Direction.class);

    // Reused VertexInfo objects to avoid recreating them
    private final VertexInfo[] cachedVertexInfo = new VertexInfo[4];

    public MeshData(MeshPartBuilder builder) {
        this.builder = builder;

        // Initialize cache for VertexInfo objects
        for (int i = 0; i < cachedVertexInfo.length; i++) {
            cachedVertexInfo[i] = new VertexInfo();
        }
    }

    // Adds a quad to the MeshPartBuilder for the specified direction
    public void addQuad(int x, int y, int z, int width, int height, Direction direction, TextureRegion uv) {
        // Get or calculate the vertices for the direction
        Vector3[] vertices = getCachedQuadVertices(x, y, z, width, height, direction);

        // Set the cached VertexInfo objects to the corresponding positions and UVs
        cachedVertexInfo[0].setPos(vertices[0]).setUV(uv.getU(), uv.getV());
        cachedVertexInfo[1].setPos(vertices[1]).setUV(uv.getU2(), uv.getV());
        cachedVertexInfo[2].setPos(vertices[2]).setUV(uv.getU2(), uv.getV2());
        cachedVertexInfo[3].setPos(vertices[3]).setUV(uv.getU(), uv.getV2());

        // Add the quad (two triangles) to the MeshPartBuilder
        builder.rect(cachedVertexInfo[0], cachedVertexInfo[1], cachedVertexInfo[2], cachedVertexInfo[3]);
    }

    // Retrieves cached vertices or calculates and caches them if needed
    private Vector3[] getCachedQuadVertices(int x, int y, int z, int width, int height, Direction direction) {
        // Check if vertices for this direction have already been cached
        if (!cachedVertices.containsKey(direction)) {
            cachedVertices.put(direction, new Vector3[4]);
        }
        
        Vector3[] vertices = cachedVertices.get(direction);
        if (vertices[0] == null) {
            // Only calculate the vertices if they haven't been cached yet
            vertices[0] = new Vector3();
            vertices[1] = new Vector3();
            vertices[2] = new Vector3();
            vertices[3] = new Vector3();

            switch (direction) {
                case NORTH:
                    vertices[0].set(x, y, z + height);  // Top-left
                    vertices[1].set(x + width, y, z + height);  // Top-right
                    vertices[2].set(x + width, y, z);  // Bottom-right
                    vertices[3].set(x, y, z);  // Bottom-left
                    break;
                case SOUTH:
                    vertices[0].set(x, y, z);  // Top-left
                    vertices[1].set(x + width, y, z);  // Top-right
                    vertices[2].set(x + width, y, z - height);  // Bottom-right
                    vertices[3].set(x, y, z - height);  // Bottom-left
                    break;
                case EAST:
                    vertices[0].set(x + width, y, z);  // Top-left
                    vertices[1].set(x + width, y + height, z);  // Top-right
                    vertices[2].set(x + width, y + height, z - height);  // Bottom-right
                    vertices[3].set(x + width, y, z - height);  // Bottom-left
                    break;
                case WEST:
                    vertices[0].set(x, y, z);  // Top-left
                    vertices[1].set(x, y + height, z);  // Top-right
                    vertices[2].set(x, y + height, z - height);  // Bottom-right
                    vertices[3].set(x, y, z - height);  // Bottom-left
                    break;
                case UP:
                    vertices[0].set(x, y + height, z);  // Top-left
                    vertices[1].set(x + width, y + height, z);  // Top-right
                    vertices[2].set(x + width, y + height, z - height);  // Bottom-right
                    vertices[3].set(x, y + height, z - height);  // Bottom-left
                    break;
                case DOWN:
                    vertices[0].set(x, y, z);  // Top-left
                    vertices[1].set(x + width, y, z);  // Top-right
                    vertices[2].set(x + width, y, z - height);  // Bottom-right
                    vertices[3].set(x, y, z - height);  // Bottom-left
                    break;
            }
        }

        return vertices;
    }
}