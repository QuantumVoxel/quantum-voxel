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
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.QuantumClient;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class MeshManager {
    public static final MeshManager INSTANCE = new MeshManager();
    private final MeshBuilder builder = new MeshBuilder();
    private final List<Mesh> meshes = new CopyOnWriteArrayList<>();

    private MeshManager() {

    }

    public Mesh createBox(float width, float height, float depth) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        BoxShapeBuilder.build(builder, width, height, depth);
        return builder.end();
    }

    public Mesh createCube(float size) {
        return createBox(size, size, size);
    }

    public Mesh createCylinder(float width, float height, float depth, int divisions) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        CylinderShapeBuilder.build(builder, width, height, depth, divisions);
        return builder.end();
    }

    public Mesh createCone(float width, float height, float depth, int divisions) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        ConeShapeBuilder.build(builder, width, height, depth, divisions);
        return builder.end();
    }

    public Mesh createSphere(float width, float height, float depth, int divisionsU, int divisionsV) {
        builder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        SphereShapeBuilder.build(builder, width, height, depth, divisionsU, divisionsV);
        return builder.end();
    }

    public Mesh generateMesh(VertexAttributes attributes, Consumer<MeshPartBuilder> builder) {
        this.builder.begin(attributes, GL20.GL_TRIANGLES);
        builder.accept(this.builder);
        Mesh mesh = this.builder.end();

        this.meshes.add(mesh);

        return mesh;
    }

    public Mesh generateMesh(long attributes, Consumer<MeshBuilder> builder) {
        this.builder.begin(attributes, GL20.GL_TRIANGLES);
        builder.accept(this.builder);
        Mesh mesh = this.builder.end();

        this.meshes.add(mesh);

        return mesh;
    }

    public Mesh generateMesh(Function<MeshBuilder, Mesh> builder) {
        Mesh mesh = builder.apply(this.builder);

        this.meshes.add(mesh);

        return mesh;
    }

    @CanIgnoreReturnValue
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

    public void add(Mesh mesh) {
        this.meshes.add(mesh);
    }

    public void reload() {
        // TODO
    }

    public void dispose() {
        for (Mesh mesh : this.meshes) {
            unloadMesh(mesh);
        }
    }
}
