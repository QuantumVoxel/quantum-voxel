package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.math.Vector3;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.BakedModel;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.util.LazyValue;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.CubicDirection;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class BakedCubeModel extends BakedModel implements BlockModel {
    private static final LazyValue<BakedCubeModel> DEFAULT = new LazyValue<>();
    private static final Vector3 from = new Vector3(-8f, 8f, -8f);
    private static final Vector3 to = new Vector3(8f, 24f, 8f);
    private final TextureRegion top;
    private final TextureRegion bottom;
    private final TextureRegion left;
    private final TextureRegion right;
    private final TextureRegion front;
    private final TextureRegion back;
    public final ModelProperties properties;

    private static final VertexInfo V_00 = new VertexInfo();
    private static final VertexInfo V_01 = new VertexInfo();
    private static final VertexInfo V_10 = new VertexInfo();
    private static final VertexInfo V_11 = new VertexInfo();

    private BakedCubeModel(NamespaceID resourceId, TextureRegion all, Model model) {
        this(resourceId, all, all, all, all, all, all, model);
    }

    private BakedCubeModel(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                           TextureRegion left, TextureRegion right,
                           TextureRegion front, TextureRegion back, Model model) {
        this(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build(), model);
    }

    private BakedCubeModel(NamespaceID resourceId, TextureRegion all, ModelProperties properties, Model model) {
        this(resourceId, all, all, all, all, all, all, properties, model);
    }

    private BakedCubeModel(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                           TextureRegion left, TextureRegion right,
                           TextureRegion front, TextureRegion back, ModelProperties properties, Model model) {
        super(resourceId, model);
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;

        this.properties = properties;
    }

    public synchronized static BakedCubeModel defaultModel() {
        if (DEFAULT.isInitialized()) {
            return DEFAULT.get();
        }

        BakedCubeModel bakedCubeModel = BakedCubeModel.of(new NamespaceID("block/default"), TextureManager.DEFAULT_TEX_REG);
        DEFAULT.set(bakedCubeModel);
        return bakedCubeModel;
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion all) {
        return new BakedCubeModel(resourceId, all, all, all, all, all, all, ModelProperties.builder().build(),
                createModel(resourceId, all, all, all, all, all, all));
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                                    TextureRegion left, TextureRegion right,
                                    TextureRegion front, TextureRegion back) {
        return new BakedCubeModel(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build(),
                createModel(resourceId, top, bottom, left, right, front, back));
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion all, ModelProperties properties) {
        return new BakedCubeModel(resourceId, all, properties, createModel(resourceId, all, all, all, all, all, all));
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                                    TextureRegion left, TextureRegion right,
                                    TextureRegion front, TextureRegion back, ModelProperties properties) {
        return new BakedCubeModel(resourceId, top, bottom, left, right, front, back, properties,
                createModel(resourceId, top, bottom, left, right, front, back));
    }

    public static Model createModel(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                                    TextureRegion left, TextureRegion right,
                                    TextureRegion front, TextureRegion back) {
        return ModelManager.INSTANCE.generateModel(resourceId, modelBuilder -> {
            Material material = new Material();
            material.set(new TextureAttribute(TextureAttribute.Diffuse, QuantumClient.get().blocksTextureAtlas.getTexture()));
            material.set(new TextureAttribute(TextureAttribute.Emissive, QuantumClient.get().blocksTextureAtlas.getEmissiveTexture()));

            modelBuilder.part("cube", createMesh(resourceId, top, bottom, left, right, front, back), GL20.GL_TRIANGLES, material);
        });
    }

    public TextureRegion top() {
        return this.top;
    }

    public TextureRegion bottom() {
        return this.bottom;
    }

    public TextureRegion west() {
        return this.left;
    }

    public TextureRegion east() {
        return this.right;
    }

    public TextureRegion north() {
        return this.front;
    }

    public TextureRegion south() {
        return this.back;
    }

    private static Mesh createMesh(NamespaceID resourceId, TextureRegion top, TextureRegion bottom, TextureRegion west, TextureRegion east, TextureRegion north, TextureRegion south) {
        MeshBuilder builder = new MeshBuilder();
        builder.begin(new VertexAttributes(VertexAttribute.Position(), VertexAttribute.ColorPacked(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0)), GL20.GL_TRIANGLES);
        builder.setColor(Color.WHITE);

        MeshPartBuilder.VertexInfo v00 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v01 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v10 = new MeshPartBuilder.VertexInfo();
        MeshPartBuilder.VertexInfo v11 = new MeshPartBuilder.VertexInfo();

        int faces = 0;

        for (CubicDirection blockFace : CubicDirection.values()) {
            TextureRegion entry = switch (blockFace) {
                case UP -> top;
                case DOWN -> bottom;
                case WEST -> west;
                case EAST -> east;
                case NORTH -> north;
                case SOUTH -> south;
            };

            if (entry == null) continue;

            faces++;
            
            v00.setCol(RgbColor.WHITE.toGdx());
            v01.setCol(RgbColor.WHITE.toGdx());
            v10.setCol(RgbColor.WHITE.toGdx());
            v11.setCol(RgbColor.WHITE.toGdx());

            v00.setUV(entry.getU(), entry.getV2());
            v01.setUV(entry.getU(), entry.getV());
            v10.setUV(entry.getU2(), entry.getV2());
            v11.setUV(entry.getU2(), entry.getV());

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

            builder.rect(v00, v10, v11, v01);
        }

        if (faces == 0)
            CommonConstants.LOGGER.warn("Model {} has no faces", resourceId);

        return builder.end();
    }

    private static void createTop(TextureRegion region, MeshPartBuilder builder) {
        if (region == null) return;

        V_00.setPos(-1, 1, 0);
        V_01.setPos(-1 + 1, 1, 0);
        V_10.setPos(-1 + 1, 1, 1);
        V_11.setPos(-1, 1, 1);

        setNor(0, 1, 0);
        finishRect(region, builder);
    }

    private static void createBottom(TextureRegion region, MeshPartBuilder builder) {
        if (region == null) return;

        V_00.setPos(-1, 0, 0);
        V_01.setPos(-1, 0, 1);
        V_10.setPos(-1 + 1, 0, 1);
        V_11.setPos(-1 + 1, 0, 0);

        setNor(0, -1, 0);
        finishRect(region, builder);
    }

    private static void createLeft(TextureRegion region, MeshPartBuilder builder) {
        if (region == null) return;

        V_00.setPos(-1, 0, 0);
        V_01.setPos(-1, 1, 0);
        V_10.setPos(-1, 1, 1);
        V_11.setPos(-1, 0, 1);

        setNor(-1, 0, 0);
        finishRect(region, builder);
    }

    private static void createRight(TextureRegion region, MeshPartBuilder builder) {
        if (region == null) return;

        V_00.setPos(-1 + 1, 0, 0);
        V_01.setPos(-1 + 1, 0, 1);
        V_10.setPos(-1 + 1, 1, 1);
        V_11.setPos(-1 + 1, 1, 0);

        setNor(1, 0, 0);
        finishRect(region, builder);
    }

    private static void createFront(TextureRegion region, MeshPartBuilder builder) {
        if (region == null) return;

        V_00.setPos(-1, 0, 0);
        V_01.setPos(-1 + 1, 0, 0);
        V_10.setPos(-1 + 1, 1, 0);
        V_11.setPos(-1, 1, 0);

        setNor(0, 0, 1);
        finishRect(region, builder);
    }

    private static void createBack(TextureRegion region, MeshPartBuilder builder) {
        if (region == null) return;

        V_00.setPos(-1, 0, 1);
        V_01.setPos(-1, 1, 1);
        V_10.setPos(-1 + 1, 1, 1);
        V_11.setPos(-1 + 1, 0, 1);

        setNor(0, 0, -1);
        finishRect(region, builder);
    }

    private static void setNor(int x, int y, int z) {
        V_00.setNor(x, y, z);
        V_01.setNor(x, y, z);
        V_10.setNor(x, y, z);
        V_11.setNor(x, y, z);
    }

    private static void finishRect(TextureRegion region, MeshPartBuilder builder) {
        V_00.setUV(region.getU2(), region.getV2());
        V_01.setUV(region.getU2(), region.getV());
        V_01.setUV(region.getU(), region.getV());
        V_11.setUV(region.getU(), region.getV2());

        builder.rect(V_00, V_01, V_10, V_11);
    }

    @Override
    public void load(QuantumClient client) {
        // Do nothing
    }

    public boolean isCustom() {
        return false;
    }

    @Override
    public @Nullable TextureRegion getBuriedTexture() {
        return front;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        BakedCubeModel that = (BakedCubeModel) obj;
        return Objects.equals(this.top, that.top) &&
                Objects.equals(this.bottom, that.bottom) &&
                Objects.equals(this.left, that.left) &&
                Objects.equals(this.right, that.right) &&
                Objects.equals(this.front, that.front) &&
                Objects.equals(this.back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.top, this.bottom, this.left, this.right, this.front, this.back);
    }

    @Override
    public String toString() {
        return "BakedCubeModel[" +
                "top=" + this.top + ", " +
                "bottom=" + this.bottom + ", " +
                "left=" + this.left + ", " +
                "right=" + this.right + ", " +
                "front=" + this.front + ", " +
                "back=" + this.back + ']';
    }
}
