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
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.model.BakedModel;
import dev.ultreon.quantum.client.model.model.Json5Model;
import dev.ultreon.quantum.client.render.ModelManager;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.render.meshing.FaceCull;
import dev.ultreon.quantum.client.render.meshing.Light;
import dev.ultreon.quantum.client.world.AOUtils;
import dev.ultreon.quantum.util.LazyValue;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import dev.ultreon.quantum.world.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class BakedCubeModel extends BakedModel implements BlockModel {
    private static final LazyValue<BlockModel> DEFAULT = new LazyValue<>();
    private static final Vector3 from = new Vector3(-8f, 8f, -8f);
    private static final Vector3 to = new Vector3(8f, 24f, 8f);
    public static final Vector3 w_from = new Vector3(0, 0, 0);
    public static final Vector3 w_to = new Vector3(16, 16, 16);
    private final TextureRegion top;
    private final TextureRegion bottom;
    private final TextureRegion left;
    private final TextureRegion right;
    private final TextureRegion front;
    private final TextureRegion back;
    public final ModelProperties properties;

    private final String renderPass;
    private RenderPass renderPassObj;

    private BakedCubeModel(NamespaceID resourceId, TextureRegion all, ModelProperties properties, Model model, String renderPass) {
        this(resourceId, all, all, all, all, all, all, properties, model, renderPass);
    }

    private BakedCubeModel(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                           TextureRegion left, TextureRegion right,
                           TextureRegion front, TextureRegion back, ModelProperties properties, Model model, String renderPass) {
        super(resourceId, model);
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;

        this.properties = properties;
        this.renderPass = renderPass;
    }

    public synchronized static BlockModel defaultModel() {
        if (DEFAULT.isInitialized()) {
            return DEFAULT.get();
        }

        ModelProperties properties1 = new ModelProperties();
        properties1.renderPass = "opaque";
        BlockModel bakedCubeModel = Json5Model.cubeOf(CubeModel.of(new NamespaceID("block/default"), QuantumClient.id("block/error"), properties1));
        DEFAULT.set(bakedCubeModel);
        QuantumClient.invokeAndWait(() -> bakedCubeModel.load(QuantumClient.get()));
        return bakedCubeModel;
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion all, String renderPass) {
        return new BakedCubeModel(resourceId, all, all, all, all, all, all, ModelProperties.builder().build(),
                createModel(resourceId, all, all, all, all, all, all), renderPass);
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                                    TextureRegion left, TextureRegion right,
                                    TextureRegion front, TextureRegion back, String renderPass) {
        return new BakedCubeModel(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build(),
                createModel(resourceId, top, bottom, left, right, front, back), renderPass);
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion all, ModelProperties properties) {
        return new BakedCubeModel(resourceId, all, properties, createModel(resourceId, all, all, all, all, all, all), properties.renderPass);
    }

    public static BakedCubeModel of(NamespaceID resourceId, TextureRegion top, TextureRegion bottom,
                                    TextureRegion left, TextureRegion right,
                                    TextureRegion front, TextureRegion back, ModelProperties properties) {
        return new BakedCubeModel(resourceId, top, bottom, left, right, front, back, properties,
                createModel(resourceId, top, bottom, left, right, front, back), properties.renderPass);
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

        for (Direction blockFace : Direction.values()) {
            TextureRegion entry;
            switch (blockFace) {
                case UP:
                    entry = top;
                    break;
                case DOWN:
                    entry = bottom;
                    break;
                case WEST:
                    entry = west;
                    break;
                case EAST:
                    entry = east;
                    break;
                case NORTH:
                    entry = north;
                    break;
                case SOUTH:
                    entry = south;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

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
    public boolean hasAO() {
        return true;
    }

    @Override
    public RenderPass getRenderPass() {
        if (renderPassObj != null) return renderPassObj;
        return renderPassObj = RenderPass.byName(renderPass);
    }

    @Override
    public Collection<NamespaceID> getAllTextures() {
        return List.of();
    }

    @Override
    public void bakeInto(MeshPartBuilder meshPartBuilder, int x, int y, int z, int cull, int[] ao, long light) {
        final var from = w_from;
        final var to = w_to;

        final var v00 = new VertexInfo();
        final var v01 = new VertexInfo();
        final var v10 = new VertexInfo();
        final var v11 = new VertexInfo();
        for (var direction : Direction.values()) {
            if (FaceCull.culls(direction, cull)) continue;

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

            var region = this.tex(direction);
            if (region == null) {
                region = QuantumClient.get().blocksTextureAtlas.get(NamespaceID.of("blocks/error"), TextureAtlas.TextureAtlasType.DIFFUSE);
            }

            if (region == null) {
                throw new IllegalArgumentException("Undefined error texture!");
            }

            v00.setUV(region.getU(), region.getV2());
            v01.setUV(region.getU(), region.getV());
            v10.setUV(region.getU2(), region.getV2());
            v11.setUV(region.getU2(), region.getV());

            switch (direction) {
                case UP:
                    v01.setPos(to.x, to.y, from.z);
                    v00.setPos(to.x, to.y, to.z);
                    v11.setPos(from.x, to.y, from.z);
                    v10.setPos(from.x, to.y, to.z);
                    break;
                case DOWN:
                    v10.setPos(to.x, from.y, from.z);
                    v11.setPos(to.x, from.y, to.z);
                    v00.setPos(from.x, from.y, from.z);
                    v01.setPos(from.x, from.y, to.z);
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
            v00.position.add(x, y, z);
            v01.position.add(x, y, z);
            v10.position.add(x, y, z);
            v11.position.add(x, y, z);

            meshPartBuilder.rect(v00, v10, v11, v01);
        }
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

    public TextureRegion tex(Direction direction) {
        switch (direction) {
            case UP:
                return top;
            case DOWN:
                return bottom;
            case WEST:
                return left;
            case EAST:
                return right;
            case NORTH:
                return front;
            case SOUTH:
                return back;
            default:
                throw new IllegalArgumentException();
        }
    }
}
