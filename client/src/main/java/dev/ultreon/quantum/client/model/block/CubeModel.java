package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.util.NamespaceID;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

import static dev.ultreon.quantum.client.QuantumClient.isOnRenderThread;

public final class CubeModel {
    private final NamespaceID top;
    private final NamespaceID bottom;
    private final NamespaceID left;
    private final NamespaceID right;
    private final NamespaceID front;
    private final NamespaceID back;
    private final ModelProperties properties;
    private NamespaceID resourceId;

    private CubeModel(NamespaceID resourceId, NamespaceID top, NamespaceID bottom,
                      NamespaceID left, NamespaceID right,
                      NamespaceID front, NamespaceID back, ModelProperties properties) {
        this.resourceId = resourceId;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.properties = properties;
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID all) {
        return CubeModel.of(resourceId, all, all, all);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID side) {
        return CubeModel.of(resourceId, top, bottom, side, side, side, side);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID side, NamespaceID front) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, side);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID side, NamespaceID front, NamespaceID back) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, back);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID left, NamespaceID right, NamespaceID front, NamespaceID back) {
        return new CubeModel(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID all, ModelProperties properties) {
        return CubeModel.of(resourceId, all, all, all, properties);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID side, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, side, side, properties);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID side, NamespaceID front, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, side, properties);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID side, NamespaceID front, NamespaceID back, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, back, properties);
    }

    public static CubeModel of(NamespaceID resourceId, NamespaceID top, NamespaceID bottom, NamespaceID left, NamespaceID right, NamespaceID front, NamespaceID back, ModelProperties properties) {
        return new CubeModel(resourceId, top, bottom, left, right, front, back, properties);
    }

    public BakedCubeModel bake(NamespaceID resourceId, TextureAtlas texture) {
        if (!isOnRenderThread()) return QuantumClient.invokeAndWait(() -> this.bake(resourceId, texture));
        try {
            TextureRegion topTex = texture.getDiffuse(this.top);
            TextureRegion bottomTex = texture.getDiffuse(this.bottom);
            TextureRegion leftTex = texture.getDiffuse(this.left);
            TextureRegion rightTex = texture.getDiffuse(this.right);
            TextureRegion frontTex = texture.getDiffuse(this.front);
            TextureRegion backTex = texture.getDiffuse(this.back);

            switch (this.properties.rotation) {
                case NORTH:
                    break;
                case EAST:
                    frontTex = texture.getDiffuse(this.left);
                    backTex = texture.getDiffuse(this.right);
                    leftTex = texture.getDiffuse(this.back);
                    rightTex = texture.getDiffuse(this.front);

                    // Rotate top and bottom tex
                    topTex = rotate(topTex, 1);
                    bottomTex = rotate(bottomTex, 1);
                    break;
                case SOUTH:
                    frontTex = texture.getDiffuse(this.back);
                    backTex = texture.getDiffuse(this.front);
                    leftTex = texture.getDiffuse(this.right);
                    rightTex = texture.getDiffuse(this.left);

                    // Rotate top and bottom tex
                    topTex = rotate(topTex, 2);
                    bottomTex = rotate(bottomTex, 2);
                    break;
                case WEST:
                    frontTex = texture.getDiffuse(this.right);
                    backTex = texture.getDiffuse(this.left);
                    leftTex = texture.getDiffuse(this.front);
                    rightTex = texture.getDiffuse(this.back);

                    // Rotate top and bottom tex
                    topTex = rotate(topTex, 3);
                    bottomTex = rotate(bottomTex, 3);
                    break;
                case UP:
                    frontTex = texture.getDiffuse(this.top);
                    backTex = texture.getDiffuse(this.bottom);
                    leftTex = texture.getDiffuse(this.left);
                    rightTex = texture.getDiffuse(this.right);

                    leftTex = rotate(leftTex, 1);
                    rightTex = rotate(rightTex, 1);
                    break;
                case DOWN:
                    frontTex = texture.getDiffuse(this.bottom);
                    backTex = texture.getDiffuse(this.top);
                    leftTex = texture.getDiffuse(this.left);
                    rightTex = texture.getDiffuse(this.right);

                    leftTex = rotate(leftTex, 3);
                    rightTex = rotate(rightTex, 3);
                    break;
            }

            return BakedCubeModel.of(
                    resourceId,
                    topTex, bottomTex,
                    leftTex, rightTex,
                    frontTex, backTex,
                    this.properties
            );
        } catch (RuntimeException e) {
            CrashLog crashLog = createCrash(resourceId, e);

            throw new ApplicationCrash(crashLog);
        }
    }

    private TextureRegion rotate(TextureRegion region, int ticks) {
        Texture texture = region.getTexture();
        return new TextureRegion(texture, region.getU(), region.getV(), region.getU2(), region.getV2());
    }

    @NotNull
    private CrashLog createCrash(NamespaceID resourceId, RuntimeException e) {
        CrashLog crashLog = new CrashLog("Failed to bake cube model", e);
        CrashCategory bakingModel = new CrashCategory("Baking Model");
        bakingModel.add("ID", resourceId);
        crashLog.addCategory(bakingModel);

        CrashCategory model = new CrashCategory("Model");
        model.add("Top", this.top);
        model.add("Bottom", this.bottom);
        model.add("Left", this.left);
        model.add("Right", this.right);
        model.add("Front", this.front);
        model.add("Back", this.back);
        crashLog.addCategory(model);
        return crashLog;
    }

    public NamespaceID top() {
        return top;
    }

    public NamespaceID bottom() {
        return bottom;
    }

    public NamespaceID left() {
        return left;
    }

    public NamespaceID right() {
        return right;
    }

    public NamespaceID front() {
        return front;
    }

    public NamespaceID back() {
        return back;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        CubeModel that = (CubeModel) obj;
        return Objects.equals(this.top, that.top) &&
                Objects.equals(this.bottom, that.bottom) &&
                Objects.equals(this.left, that.left) &&
                Objects.equals(this.right, that.right) &&
                Objects.equals(this.front, that.front) &&
                Objects.equals(this.back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, left, right, front, back);
    }

    @Override
    public String toString() {
        return "CubeModel[" +
                "top=" + top + ", " +
                "bottom=" + bottom + ", " +
                "left=" + left + ", " +
                "right=" + right + ", " +
                "front=" + front + ", " +
                "back=" + back + ']';
    }

    public Set<NamespaceID> all() {
        return new ReferenceArraySet<>(new Object[]{top, bottom, left, right, front, back});
    }

    public NamespaceID resourceId() {
        return resourceId;
    }
}
