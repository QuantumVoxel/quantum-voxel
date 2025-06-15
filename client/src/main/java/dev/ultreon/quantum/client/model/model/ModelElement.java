package dev.ultreon.quantum.client.model.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import de.damios.guacamole.Preconditions;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.render.meshing.FaceCull;
import dev.ultreon.quantum.client.render.meshing.Light;
import dev.ultreon.quantum.client.world.AOUtils;
import dev.ultreon.quantum.util.Axis;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public final class ModelElement {
    private static final Vector3 tmp = new Vector3();
    private static final Quaternion tmpQ = new Quaternion();
    private final Map<Direction, FaceElement> blockFaceFaceElementMap;
    private final boolean shade;
    private final ElementRotation rotation;
    private final Vector3 from;
    private final Vector3 to;
    private final Vector3 tmp2 = new Vector3();


    public ModelElement(Map<Direction, FaceElement> blockFaceFaceElementMap, boolean shade, ElementRotation rotation, Vector3 from, Vector3 to) {
        Preconditions.checkNotNull(blockFaceFaceElementMap);
        Preconditions.checkNotNull(rotation);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        this.blockFaceFaceElementMap = blockFaceFaceElementMap;
        this.shade = shade;
        this.rotation = rotation;
        this.from = from;
        this.to = to;
    }

    public void bakeInto(int idx, MeshPartBuilder meshBuilder, Map<String, NamespaceID> textureElements, int x, int y, int z, int cull, int[] ao, long light) {
        final var from = this.from();
        final var to = this.to();

        final var v00 = new MeshPartBuilder.VertexInfo();
        final var v01 = new MeshPartBuilder.VertexInfo();
        final var v10 = new MeshPartBuilder.VertexInfo();
        final var v11 = new MeshPartBuilder.VertexInfo();
        for (var $ : blockFaceFaceElementMap.entrySet()) {
            final var direction = $.getKey();
            final var faceElement = $.getValue();
            if (faceElement.cullface == direction && FaceCull.culls(faceElement.cullface, cull)) continue;
            final var texRef = faceElement.texture;
            final @Nullable NamespaceID texture = Objects.equals(texRef, "#missing") ? NamespaceID.of("blocks/error")
                    : texRef.startsWith("#") ? textureElements.get(texRef.substring(1))
                    : NamespaceID.parse(texRef).mapPath(path -> path);


            int sAo = AOUtils.aoForSide(ao, direction);
            byte sLight = Light.get(light, direction);
            v00.setCol(0f, AOUtils.hasAoCorner00(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);
            v01.setCol(0f, AOUtils.hasAoCorner01(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);
            v10.setCol(0f, AOUtils.hasAoCorner10(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);
            v11.setCol(0f, AOUtils.hasAoCorner11(sAo) ? .5f : 1f, (sLight >> 4 | 0xf) / 15f, (sLight | 0xf) / 15f);

            v00.setNor(direction.getNormal());
            v01.setNor(direction.getNormal());
            v10.setNor(direction.getNormal());
            v11.setNor(direction.getNormal());

            var region = QuantumClient.get().blocksTextureAtlas.get(Objects.requireNonNull(texture), TextureAtlas.TextureAtlasType.DIFFUSE);
            if (region == null) {
                region = QuantumClient.get().blocksTextureAtlas.get(NamespaceID.of("blocks/error"), TextureAtlas.TextureAtlasType.DIFFUSE);

                if (region == null) throw new IllegalArgumentException("Undefined error texture! " + texture);

                v00.setUV(region.getU(), region.getV2());
                v01.setUV(region.getU(), region.getV());
                v10.setUV(region.getU2(), region.getV2());
                v11.setUV(region.getU2(), region.getV());
            } else {
                float u0 = region.getU() + (faceElement.uvs.x1) * (region.getU2() - region.getU());
                float v0 = region.getV() + (faceElement.uvs.y2) * (region.getV2() - region.getV());

                float u1 = region.getU() + (faceElement.uvs.x1) * (region.getU2() - region.getU());
                float v1 = region.getV() + (faceElement.uvs.y1) * (region.getV2() - region.getV());

                float u2 = region.getU() + (faceElement.uvs.x2) * (region.getU2() - region.getU());
                float v2 = region.getV() + (faceElement.uvs.y2) * (region.getV2() - region.getV());

                float u3 = region.getU() + (faceElement.uvs.x2) * (region.getU2() - region.getU());
                float v3 = region.getV() + (faceElement.uvs.y1) * (region.getV2() - region.getV());

                v00.setUV(u0, v0);
                v01.setUV(u1, v1);
                v10.setUV(u2, v2);
                v11.setUV(u3, v3);
            }

            switch (direction) {
                case UP:
                    v01.setPos(from.x / 16, to.y / 16, from.z / 16);
                    v00.setPos(from.x / 16, to.y / 16, to.z / 16);
                    v11.setPos(to.x / 16, to.y / 16, from.z / 16);
                    v10.setPos(to.x / 16, to.y / 16, to.z / 16);
                    break;
                case DOWN:
                    v00.setPos(from.x / 16, from.y / 16, from.z / 16);
                    v01.setPos(from.x / 16, from.y / 16, to.z / 16);
                    v10.setPos(to.x / 16, from.y / 16, from.z / 16);
                    v11.setPos(to.x / 16, from.y / 16, to.z / 16);
                    break;
                case WEST:
                    v00.setPos(from.x / 16, from.y / 16, from.z / 16);
                    v01.setPos(from.x / 16, to.y / 16, from.z / 16);
                    v10.setPos(from.x / 16, from.y / 16, to.z / 16);
                    v11.setPos(from.x / 16, to.y / 16, to.z / 16);
                    break;
                case EAST:
                    v10.setPos(to.x / 16, from.y / 16, from.z / 16);
                    v11.setPos(to.x / 16, to.y / 16, from.z / 16);
                    v00.setPos(to.x / 16, from.y / 16, to.z / 16);
                    v01.setPos(to.x / 16, to.y / 16, to.z / 16);
                    break;
                case NORTH:
                    v00.setPos(to.x / 16, from.y / 16, from.z / 16);
                    v01.setPos(to.x / 16, to.y / 16, from.z / 16);
                    v10.setPos(from.x / 16, from.y / 16, from.z / 16);
                    v11.setPos(from.x / 16, to.y / 16, from.z / 16);
                    break;
                case SOUTH:
                    v10.setPos(to.x / 16, from.y / 16, to.z / 16);
                    v11.setPos(to.x / 16, to.y / 16, to.z / 16);
                    v00.setPos(from.x / 16, from.y / 16, to.z / 16);
                    v01.setPos(from.x / 16, to.y / 16, to.z / 16);
                    break;
            }

            rotate(v00, v01, v10, v11, rotation);

            v00.position.add(x, y, z);
            v01.position.add(x, y, z);
            v10.position.add(x, y, z);
            v11.position.add(x, y, z);

            meshBuilder.rect(v00, v10, v11, v01);

            final var material = new Material();
            material.set(TextureAttribute.createDiffuse(QuantumClient.get().blocksTextureAtlas.getTexture()));
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            material.set(new FloatAttribute(FloatAttribute.AlphaTest));
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL));
        }
    }

    public void bake(int idx, ModelBuilder modelBuilder, Map<String, NamespaceID> textureElements) {
        Vector3 from = this.from();
        Vector3 to = this.to();

        ModelBuilder nodeBuilder = new ModelBuilder();
        nodeBuilder.begin();

        MeshBuilder meshBuilder = new MeshBuilder();
        MeshPartBuilder.VertexInfo v00 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v01 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v10 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v11 = new MeshPartBuilder.VertexInfo();
        for (Map.Entry<Direction, FaceElement> entry : blockFaceFaceElementMap.entrySet()) {
            Direction direction = entry.getKey();
            FaceElement faceElement = entry.getValue();

            final var texRef = faceElement.texture;
            final @Nullable NamespaceID texture = (Objects.equals(texRef, "#missing") ? NamespaceID.of("blocks/error")
                    : texRef.startsWith("#") ? textureElements.get(texRef.substring(1))
                    : NamespaceID.parse(texRef)).mapPath(path -> "textures/" + path + ".png");

            meshBuilder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorUnpacked(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)), GL20.GL_TRIANGLES);
            v00.setCol(Color.WHITE);
            v01.setCol(Color.WHITE);
            v10.setCol(Color.WHITE);
            v11.setCol(Color.WHITE);

            v00.setNor(direction.getNormal());
            v01.setNor(direction.getNormal());
            v10.setNor(direction.getNormal());
            v11.setNor(direction.getNormal());

            v00.setUV(faceElement.uvs.x1, faceElement.uvs.y2);
            v01.setUV(faceElement.uvs.x1, faceElement.uvs.y1);
            v10.setUV(faceElement.uvs.x2, faceElement.uvs.y2);
            v11.setUV(faceElement.uvs.x2, faceElement.uvs.y1);

            switch (direction) {
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

            meshBuilder.rect(v00, v10, v11, v01);

            Material material = new Material();
            material.set(TextureAttribute.createDiffuse(QuantumClient.get().getTextureManager().getTexture(texture)));
            material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            material.set(new FloatAttribute(FloatAttribute.AlphaTest));
            material.set(new DepthTestAttribute(GL20.GL_LEQUAL));
            nodeBuilder.part(idx + "." + direction.name(), meshBuilder.end(), GL20.GL_TRIANGLES, material);
        }

        Model end = nodeBuilder.end();
        Node node = modelBuilder.node("[" + idx + "]", end);

        Vector3 originVec = rotation.originVec;
        Axis axis = rotation.axis;
        float angle = rotation.angle;
        boolean rescale = rotation.rescale; // TODO: implement

        node.localTransform.translate(originVec.x, originVec.y, originVec.z);
        node.localTransform.rotate(axis.getVector(), angle);
        node.localTransform.translate(-originVec.x, -originVec.y, -originVec.z);
        node.scale.set(node.localTransform.getScale(tmp));
        node.translation.set(node.localTransform.getTranslation(tmp));
        node.rotation.set(node.localTransform.getRotation(tmpQ));
    }

    private void rotate(
            MeshPartBuilder.VertexInfo v00,
            MeshPartBuilder.VertexInfo v01,
            MeshPartBuilder.VertexInfo v10,
            MeshPartBuilder.VertexInfo v11,
            ElementRotation rotation
    ) {
        final var originVec = rotation.originVec;
        final var axis = rotation.axis;
        final var angle = rotation.angle;
        final var rescale = rotation.rescale; // TODO: implement

        // Rotate the vertices
        rotate(v00.position, originVec, axis, angle, v00.position);
        rotate(v01.position, originVec, axis, angle, v01.position);
        rotate(v10.position, originVec, axis, angle, v10.position);
        rotate(v11.position, originVec, axis, angle, v11.position);

    }

    public Vector3 rotate(Vector3 position, Vector3 originVec, Axis axis, float degrees, Vector3 out) {
        return rotateVector(position, originVec, degrees, axis.getVector(), out);
    }

    // Reusable instances
    private final Vector3 tmp1 = new Vector3();

    private final Matrix4 rotationMatrix = new Matrix4();

    private Vector3 rotateVector(
            Vector3 vector,
            Vector3 origin,
            float angleDegrees,
            Vector3 axis,
            Vector3 result
    ) {
        // Step 1: Translate the vector to the origin
        tmp1.set(vector).sub(tmp2.set(origin).scl(1 / 16f));

        // Step 2: Create a rotation matrix
        rotationMatrix.setToRotation(axis, angleDegrees);

        // Step 3: Apply the rotation matrix to the translated vector
        tmp1.mul(rotationMatrix);

        // Step 4: Translate the vector back
        result.set(tmp1).add(tmp2);

        return result;
    }

    public Map<Direction, FaceElement> blockFaceFaceElementMap() {
        return blockFaceFaceElementMap;
    }

    public boolean shade() {
        return shade;
    }

    public ElementRotation rotation() {
        return rotation;
    }

    public Vector3 from() {
        return from;
    }

    public Vector3 to() {
        return to;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModelElement) obj;
        return Objects.equals(this.blockFaceFaceElementMap, that.blockFaceFaceElementMap) &&
               this.shade == that.shade &&
               Objects.equals(this.rotation, that.rotation) &&
               Objects.equals(this.from, that.from) &&
               Objects.equals(this.to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockFaceFaceElementMap, shade, rotation, from, to);
    }

    @Override
    public String toString() {
        return "ModelElement[" +
               "blockFaceFaceElementMap=" + blockFaceFaceElementMap + ", " +
               "shade=" + shade + ", " +
               "rotation=" + rotation + ", " +
               "from=" + from + ", " +
               "to=" + to + ']';
    }

}
