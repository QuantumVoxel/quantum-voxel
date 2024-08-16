package dev.ultreon.quantum.client.model.blockbench;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.util.Vec2f;
import dev.ultreon.quantum.util.Vec3f;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.CubicDirection;

import java.io.IOException;
import java.util.*;

public final class BBCubeModelElement extends BBModelElement {
    private final String name;
    private final boolean boxUv;
    private final boolean rescale;
    private final boolean locked;
    private final String renderOrder;
    private final boolean allowMirrorModeling;
    private final Vec3f from;
    private final Vec3f to;
    private final float autouv;
    private final RgbColor color;
    private final Vec3f origin;
    private final List<BBModelFace> faces;
    private final UUID uuid;
    private final Vec3f rotation;
    private Matrix4 rotationMatrix;

    public BBCubeModelElement(String name, boolean boxUv, boolean rescale, boolean locked, String renderOrder,
                              boolean allowMirrorModeling, Vec3f from, Vec3f to, float autouv, RgbColor color,
                              Vec3f origin, List<BBModelFace> faces, UUID uuid) {
        this(name, boxUv, rescale, locked, renderOrder, allowMirrorModeling, from, to, autouv, color, origin, faces, uuid, new Vec3f());
    }

    public BBCubeModelElement(String name, boolean boxUv, boolean rescale, boolean locked, String renderOrder,
                              boolean allowMirrorModeling, Vec3f from, Vec3f to, float autouv, RgbColor color,
                              Vec3f origin, List<BBModelFace> faces, UUID uuid, Vec3f rotation) {
        this.name = name;
        this.boxUv = boxUv;
        this.rescale = rescale;
        this.locked = locked;
        this.renderOrder = renderOrder;
        this.allowMirrorModeling = allowMirrorModeling;
        this.from = from;
        this.to = to;
        this.autouv = autouv;
        this.color = color;
        this.origin = origin;
        this.faces = faces;
        this.uuid = uuid;
        this.rotation = rotation;
    }

    @Override
    public String name() {
        return name;
    }

    public boolean boxUv() {
        return boxUv;
    }

    public boolean rescale() {
        return rescale;
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

    public Vec3f from() {
        return from;
    }

    public Vec3f to() {
        return to;
    }

    public float autouv() {
        return autouv;
    }

    @Override
    public RgbColor color() {
        return color;
    }

    @Override
    public Vec3f origin() {
        return origin;
    }

    public List<BBModelFace> faces() {
        return faces;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Vec3f rotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BBCubeModelElement) obj;
        return Objects.equals(this.name, that.name) &&
                this.boxUv == that.boxUv &&
                this.rescale == that.rescale &&
                this.locked == that.locked &&
                Objects.equals(this.renderOrder, that.renderOrder) &&
                this.allowMirrorModeling == that.allowMirrorModeling &&
                Objects.equals(this.from, that.from) &&
                Objects.equals(this.to, that.to) &&
                Float.floatToIntBits(this.autouv) == Float.floatToIntBits(that.autouv) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.origin, that.origin) &&
                Objects.equals(this.faces, that.faces) &&
                Objects.equals(this.uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, boxUv, rescale, locked, renderOrder, allowMirrorModeling, from, to, autouv, color, origin, faces, uuid);
    }

    @Override
    public String toString() {
        return "BBModelElement[" +
                "name=" + name + ", " +
                "boxUv=" + boxUv + ", " +
                "rescale=" + rescale + ", " +
                "locked=" + locked + ", " +
                "renderOrder=" + renderOrder + ", " +
                "allowMirrorModeling=" + allowMirrorModeling + ", " +
                "from=" + from + ", " +
                "vec3f=" + to + ", " +
                "autouv=" + autouv + ", " +
                "color=" + color + ", " +
                "origin=" + origin + ", " +
                "faces=" + faces + ", " +
                "uuid=" + uuid + ']';
    }

    @Override
    public Node write(ModelBuilder groupBuilder, Map<UUID, ModelBuilder> subNodes, Map<Integer, BBTexture> texture2texture, BBModelLoader modelData, Vec2f resolution) {
        return this.bake(groupBuilder, texture2texture, resolution);
    }


    public Node bake(ModelBuilder groupBuilder, Map<Integer, BBTexture> texture2builder0, Vec2f resolution) {
        Vec3f from = this.from().cpy().div(16);
        Vec3f to = this.to().cpy().div(16);

//        Vec3f rotation = this.rotation().cpy();
//        Vector3 from = new Vector3(fromVec.x, fromVec.y, fromVec.z);
//        Vector3 to = new Vector3(toVec.x, toVec.y, toVec.z);
//        Matrix4 transform = new Matrix4();
//        transform.translate(from.x, from.y, from.z);
//        transform.rotate(1, 0, 0, rotation.x);
//        transform.rotate(0, 1, 0, rotation.y);
//        transform.rotate(0, 0, 1, rotation.z);
//        transform.translate(-from.x, -from.y, -from.z);
//
//        from.mul(transform);
//        to.mul(transform);

        MeshPartBuilder.VertexInfo v00 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v01 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v10 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v11 = new MeshPartBuilder.VertexInfo();

        ModelBuilder elementBuilder = new ModelBuilder();
        elementBuilder.begin();
        Map<Integer, MeshBuilder> texture2builder = new HashMap<>();
        for (BBModelFace entry : faces) {
            CubicDirection blockFace = entry.blockFace();
            int texRef = entry.texture();
            if (texRef == -1) continue;
            MeshBuilder parent = texture2builder.computeIfAbsent(texRef, integer -> {
                MeshBuilder meshBuilder = new MeshBuilder();
                meshBuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked | VertexAttributes.Usage.TextureCoordinates, GL20.GL_TRIANGLES);
                return meshBuilder;
            });

            v00.setCol(RgbColor.WHITE.toGdx());
            v01.setCol(RgbColor.WHITE.toGdx());
            v10.setCol(RgbColor.WHITE.toGdx());
            v11.setCol(RgbColor.WHITE.toGdx());

            v00.setUV(entry.uv().x / resolution.x, entry.uv().w / resolution.y);
            v01.setUV(entry.uv().x / resolution.x, entry.uv().y / resolution.y);
            v10.setUV(entry.uv().z / resolution.x, entry.uv().w / resolution.y);
            v11.setUV(entry.uv().z / resolution.x, entry.uv().y / resolution.y);

            switch (blockFace) {
                case UP:
                    v00.setPos(to.x, to.y, from.z);
                    v01.setPos(to.x, to.y, to.z);
                    v10.setPos(from.x, to.y, from.z);
                    v11.setPos(from.x, to.y, to.z);
                    break;
                case DOWN:
                    v00.setPos(from.x, from.y, from.z);
                    v01.setPos(from.x, from.y, to.z);
                    v10.setPos(to.x, from.y, from.z);
                    v11.setPos(to.x, from.y, to.z);
                    break;
                case WEST:
                    v00.setPos(from.x, from.y, from.z);
                    v01.setPos(from.x, to.y, from.z);
                    v10.setPos(from.x, from.y, to.z);
                    v11.setPos(from.x, to.y, to.z);
                    break;
                case EAST:
                    v00.setPos(to.x, from.y, to.z);
                    v01.setPos(to.x, to.y, to.z);
                    v10.setPos(to.x, from.y, from.z);
                    v11.setPos(to.x, to.y, from.z);
                    break;
                case NORTH:
                    v00.setPos(to.x, from.y, from.z);
                    v01.setPos(to.x, to.y, from.z);
                    v10.setPos(from.x, from.y, from.z);
                    v11.setPos(from.x, to.y, from.z);
                    break;
                case SOUTH:
                    v00.setPos(from.x, from.y, to.z);
                    v01.setPos(from.x, to.y, to.z);
                    v10.setPos(to.x, from.y, to.z);
                    v11.setPos(to.x, to.y, to.z);
                    break;
            }

            parent.rect(v00, v10, v11, v01);
        }

        texture2builder.forEach((texId, meshBuilder) -> {
            Material material = new Material();
            QuantumClient.invokeAndWait(() -> {
                try {
                    material.set(TextureAttribute.createDiffuse(texture2builder0.get(texId).loadOrGetTexture()));
                    material.set(new BlendingAttribute());
                    elementBuilder.part(name + " @" + texId, meshBuilder.end(), GL20.GL_TRIANGLES, material);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        return groupBuilder.node(name + "::PivotWrapped", elementBuilder.end());
    }

    public Matrix4 rotationMatrix() {
        if (this.rotationMatrix == null) {
            this.rotationMatrix = new Matrix4();
            this.rotationMatrix.rotate(Vector3.X, this.rotation.x);
            this.rotationMatrix.rotate(Vector3.Y, this.rotation.y);
            this.rotationMatrix.rotate(Vector3.Z, this.rotation.z);
        }
        return this.rotationMatrix;
    }
}
