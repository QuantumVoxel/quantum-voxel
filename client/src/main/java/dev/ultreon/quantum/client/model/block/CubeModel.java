package dev.ultreon.quantum.client.model.block;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.crash.ApplicationCrash;
import dev.ultreon.quantum.crash.CrashCategory;
import dev.ultreon.quantum.crash.CrashLog;
import dev.ultreon.quantum.util.Identifier;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

import static dev.ultreon.quantum.client.QuantumClient.isOnRenderThread;

public final class CubeModel {
    private final Identifier top;
    private final Identifier bottom;
    private final Identifier left;
    private final Identifier right;
    private final Identifier front;
    private final Identifier back;
    private final ModelProperties properties;
    private Identifier resourceId;

    private CubeModel(Identifier resourceId, Identifier top, Identifier bottom,
                      Identifier left, Identifier right,
                      Identifier front, Identifier back, ModelProperties properties) {
        this.resourceId = resourceId;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.properties = properties;
    }

    public static CubeModel of(Identifier resourceId, Identifier all) {
        return CubeModel.of(resourceId, all, all, all);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side) {
        return CubeModel.of(resourceId, top, bottom, side, side, side, side);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, side);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, back);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back) {
        return new CubeModel(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public static CubeModel of(Identifier resourceId, Identifier all, ModelProperties properties) {
        return CubeModel.of(resourceId, all, all, all, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, side, side, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, side, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, back, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back, ModelProperties properties) {
        return new CubeModel(resourceId, top, bottom, left, right, front, back, properties);
    }

    public BakedCubeModel bake(Identifier resourceId, TextureAtlas texture) {
        if (!isOnRenderThread()) return QuantumClient.invokeAndWait(() -> this.bake(resourceId, texture));
        try {
            TextureRegion topTex = texture.get(this.top);
            TextureRegion bottomTex = texture.get(this.bottom);
            TextureRegion leftTex = texture.get(this.left);
            TextureRegion rightTex = texture.get(this.right);
            TextureRegion frontTex = texture.get(this.front);
            TextureRegion backTex = texture.get(this.back);

            switch (this.properties.rotation) {
                case NORTH:
                    break;
                case EAST:
                    frontTex = texture.get(this.left);
                    backTex = texture.get(this.right);
                    leftTex = texture.get(this.back);
                    rightTex = texture.get(this.front);

                    // Rotate top and bottom tex
                    topTex = rotate(topTex, 1);
                    bottomTex = rotate(bottomTex, 1);
                    break;
                case SOUTH:
                    frontTex = texture.get(this.back);
                    backTex = texture.get(this.front);
                    leftTex = texture.get(this.right);
                    rightTex = texture.get(this.left);

                    // Rotate top and bottom tex
                    topTex = rotate(topTex, 2);
                    bottomTex = rotate(bottomTex, 2);
                    break;
                case WEST:
                    frontTex = texture.get(this.right);
                    backTex = texture.get(this.left);
                    leftTex = texture.get(this.front);
                    rightTex = texture.get(this.back);

                    // Rotate top and bottom tex
                    topTex = rotate(topTex, 3);
                    bottomTex = rotate(bottomTex, 3);
                    break;
                case UP:
                    frontTex = texture.get(this.top);
                    backTex = texture.get(this.bottom);
                    leftTex = texture.get(this.left);
                    rightTex = texture.get(this.right);

                    leftTex = rotate(leftTex, 1);
                    rightTex = rotate(rightTex, 1);
                    break;
                case DOWN:
                    frontTex = texture.get(this.bottom);
                    backTex = texture.get(this.top);
                    leftTex = texture.get(this.left);
                    rightTex = texture.get(this.right);

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
    private CrashLog createCrash(Identifier resourceId, RuntimeException e) {
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

    public Identifier top() {
        return top;
    }

    public Identifier bottom() {
        return bottom;
    }

    public Identifier left() {
        return left;
    }

    public Identifier right() {
        return right;
    }

    public Identifier front() {
        return front;
    }

    public Identifier back() {
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

    public Set<Identifier> all() {
        return new ReferenceArraySet<>(new Object[]{top, bottom, left, right, front, back});
    }

    public Identifier resourceId() {
        return resourceId;
    }
}
