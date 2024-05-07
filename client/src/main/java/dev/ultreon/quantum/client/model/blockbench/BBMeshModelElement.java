package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3f;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.Color;

import java.util.*;

public final class BBMeshModelElement extends BBModelElement {
    private final String name;
    private final Color color;
    private final Vec3f origin;
    private final Vec3f rotation;
    private final boolean export;
    private final boolean visibility;
    private final boolean locked;
    private final String renderOrder;
    private final boolean allowMirrorModeling;
    private final List<BBModelMeshFace> faces;
    private final UUID uuid;

    public BBMeshModelElement(String name, Color color, Vec3f origin, Vec3f rotation, boolean export,
                              boolean visibility, boolean locked, String renderOrder, boolean allowMirrorModeling,
                              List<BBModelMeshFace> faces, UUID uuid) {
        this.name = name;
        this.color = color;
        this.origin = origin;
        this.rotation = rotation;
        this.export = export;
        this.visibility = visibility;
        this.locked = locked;
        this.renderOrder = renderOrder;
        this.allowMirrorModeling = allowMirrorModeling;
        this.faces = faces;
        this.uuid = uuid;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Color color() {
        return color;
    }

    @Override
    public Vec3f origin() {
        return origin;
    }

    public Vec3f rotation() {
        return rotation;
    }

    public boolean export() {
        return export;
    }

    public boolean visibility() {
        return visibility;
    }

    @Override
    public boolean locked() {
        return locked;
    }

    @Override
    public String renderOrder() {
        return renderOrder;
    }

    @Override
    public boolean allowMirrorModeling() {
        return allowMirrorModeling;
    }

    public List<BBModelMeshFace> faces() {
        return faces;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBMeshModelElement) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.origin, that.origin) &&
                Objects.equals(this.rotation, that.rotation) &&
                this.export == that.export &&
                this.visibility == that.visibility &&
                this.locked == that.locked &&
                Objects.equals(this.renderOrder, that.renderOrder) &&
                this.allowMirrorModeling == that.allowMirrorModeling &&
                Objects.equals(this.faces, that.faces);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color, origin, rotation, export, visibility, locked, renderOrder, allowMirrorModeling, faces);
    }

    @Override
    public String toString() {
        return "BBMeshModelElement[" +
                "name=" + name + ", " +
                "color=" + color + ", " +
                "origin=" + origin + ", " +
                "rotation=" + rotation + ", " +
                "export=" + export + ", " +
                "visibility=" + visibility + ", " +
                "locked=" + locked + ", " +
                "renderOrder=" + renderOrder + ", " +
                "allowMirrorModeling=" + allowMirrorModeling + ", " +
                "faces=" + faces + ']';
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Node write(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes, Map<Integer, BBTexture> texture2texture, BBModelLoader modelData, Vec2f resolution) {
        Map<BBTexture, MeshPartBuilder> meshes = new HashMap<>();
        ModelBuilder nodeBuilder = new ModelBuilder();
        nodeBuilder.begin();
        for (BBModelMeshFace face : faces) {
            face.write(nodeBuilder, texture2texture, meshes, resolution);
        }

        Model nodeModel = QuantumClient.invokeAndWait(nodeBuilder::end);
        Node node = groupBuilder.node(name, nodeModel);

        node.rotation.setEulerAngles(rotation.x, rotation.y, rotation.z);
        node.translation.set(origin.x / 16f, origin.y / 16f, origin.z / 16f);

        return node;
    }
}
