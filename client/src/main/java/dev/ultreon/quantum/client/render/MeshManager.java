package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ConeShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.CylinderShapeBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.SphereShapeBuilder;
import dev.ultreon.quantum.client.QuantumClient;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * MeshManager is a singleton class responsible for managing and creating various mesh shapes for 3D rendering.
 * <p>
 * This is a part of the render pipeline. It is used to manage the meshes.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @deprecated Managing meshes is now done manually.
 */
@Deprecated
public class MeshManager {
    public static final MeshManager INSTANCE = new MeshManager();
    private final MeshBuilder builder = new MeshBuilder();
    private final List<Mesh> meshes = new CopyOnWriteArrayList<>();

    private MeshManager() {

    }

    /**
     * Creates a box mesh with the specified dimensions and initializes
     *
     * @param width the width of the box
     * @param height the height of the box
     * @param depth the depth of the box
     * @return the created box mesh
     */
    public Mesh createBox(float width, float height, float depth) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        BoxShapeBuilder.build(builder, width, height, depth);
        return builder.end();
    }

    /**
     * Creates a cube mesh with the specified size.
     *
     * @param size The size of the cube along each dimension.
     * @return A new {@link Mesh} instance representing the cube.
     */
    public Mesh createCube(float size) {
        return createBox(size, size, size);
    }

    /**
     * Creates a cylinder mesh with specified dimensions and divisions.
     *
     * @param width      the width (diameter) of the cylinder.
     * @param height     the height of the cylinder.
     * @param depth      the depth (radius) of the cylinder.
     * @param divisions  the number of divisions around the cylinder's circumference.
     * @return the created cylinder mesh.
     */
    public Mesh createCylinder(float width, float height, float depth, int divisions) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        CylinderShapeBuilder.build(builder, width, height, depth, divisions);
        return builder.end();
    }

    /**
     * Creates a cone-shaped mesh with specified dimensions and divisions.
     *
     * @param width The width of the base of the cone.
     * @param height The height of the cone.
     * @param depth The depth of the cone.
     * @param divisions The number of divisions along the circumference of the base.
     * @return A {@link Mesh} object representing the cone.
     */
    public Mesh createCone(float width, float height, float depth, int divisions) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        ConeShapeBuilder.build(builder, width, height, depth, divisions);
        return builder.end();
    }

    /**
     * Creates a spherical mesh.
     *
     * @param width The width of the sphere.
     * @param height The height of the sphere.
     * @param depth The depth of the sphere.
     * @param divisionsU Number of divisions along the horizontal axis.
     * @param divisionsV Number of divisions along the vertical axis.
     * @return The generated spherical mesh.
     */
    public Mesh createSphere(float width, float height, float depth, int divisionsU, int divisionsV) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        SphereShapeBuilder.build(builder, width, height, depth, divisionsU, divisionsV);
        return builder.end();
    }

    /**
     * Generates a mesh based on the provided vertex attributes and a builder function.
     *
     * @param attributes The vertex attributes that define the structure of the mesh's vertices.
     * @param builder A consumer function that accepts a MeshPartBuilder to define the mesh parts.
     * @return The generated mesh based on the provided builder function and vertex attributes.
     */
    public Mesh generateMesh(VertexAttributes attributes, Consumer<MeshPartBuilder> builder) {
        this.builder.begin(attributes, GL20.GL_TRIANGLES);
        builder.accept(this.builder);
        Mesh mesh = this.builder.end();

        this.meshes.add(mesh);

        return mesh;
    }

    /**
     * Generates a mesh using the specified attributes and a builder function.
     *
     * @param attributes the attributes to be used for the mesh.
     * @param builder a consumer that accepts the MeshBuilder to define the mesh structure.
     * @return the generated mesh.
     */
    public Mesh generateMesh(long attributes, Consumer<MeshBuilder> builder) {
        this.builder.begin(attributes, GL20.GL_TRIANGLES);
        builder.accept(this.builder);
        Mesh mesh = this.builder.end();

        this.meshes.add(mesh);

        return mesh;
    }

    /**
     * Generates a new {@link Mesh} using the provided builder function.
     *
     * @param builder A function that takes a {@link MeshBuilder} and returns a {@link Mesh}.
     * @return The generated {@link Mesh}.
     */
    public Mesh generateMesh(Function<MeshBuilder, Mesh> builder) {
        Mesh mesh = builder.apply(this.builder);

        this.meshes.add(mesh);

        return mesh;
    }

    /**
     * Unloads the specified mesh from the manager and disposes of its resources.
     *
     * @param mesh The mesh to be unloaded and disposed of. Can be null.
     *
     * @return true if the mesh was successfully unloaded and disposed of; false otherwise.
     */
    public boolean unloadMesh(Mesh mesh) {
        this.meshes.remove(mesh);
        if (mesh != null) {
            try {
                mesh.dispose();
            } catch (Exception e) {
                QuantumClient.LOGGER.debug("Error unloading mesh!", e);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Adds a mesh to the manager's collection of meshes.
     *
     * @param mesh The mesh to be added.
     */
    public void add(Mesh mesh) {
        this.meshes.add(mesh);
    }

    public void reload() {
        // TODO
    }

    /**
     * Disposes of all meshes managed by this instance.
     * <p>
     * This method iterates over the internal list of meshes and
     * calls the {@link #unloadMesh(Mesh)} method to unload
     * and dispose of each mesh, releasing associated resources.
     */
    public void dispose() {
        for (Mesh mesh : this.meshes) {
            unloadMesh(mesh);
        }
    }
}
