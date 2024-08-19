package dev.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import dev.ultreon.quantum.util.Vec3d;
import dev.ultreon.quantum.world.WorldAccess;


/**
 * Implementation of the WorldRenderContext interface.
 *
 * <p>This class is used to provide context for rendering entities in the world.
 * It contains information about the render layer, the holder of the entities being rendered,
 * the world being rendered, the world scale, and the camera position.</p>
 *
 * @param <T> The holder of the entities being rendered.
 */
public class WorldRenderContextImpl<T> implements WorldRenderContext<T> {

    /**
     * The render layer where the entities being rendered will be rendered.
     */
    private final ModelBatch modelBatch;

    /**
     * The holder of the entities being rendered.
     */
    private final T holder;

    /**
     * The world being rendered.
     */
    private final WorldAccess world;

    /**
     * The scale of the world being rendered.
     */
    private final float worldScale;

    /**
     * The position of the camera.
     */
    private final Vec3d cameraPos;

    /**
     * Constructs a new WorldRenderContextImpl.
     *
     * @param modelBatch The render layer where the entities being rendered will be rendered.
     * @param holder The holder of the entities being rendered.
     * @param world The world being rendered.
     * @param worldScale The scale of the world being rendered.
     * @param cameraPos The position of the camera.
     */
    public WorldRenderContextImpl(ModelBatch modelBatch, T holder, WorldAccess world, float worldScale, Vec3d cameraPos) {
        this.modelBatch = modelBatch;
        this.holder = holder;
        this.world = world;
        this.worldScale = worldScale;
        this.cameraPos = cameraPos;
    }

    /**
     * Returns the holder of the entities being rendered.
     *
     * @return The holder of the entities being rendered.
     */
    @Override
    public T getHolder() {
        return holder;
    }

    /**
     * Returns the relative translation of a given translation from the camera position.
     *
     * @param translation The translation to be converted to a relative translation.
     * @param tmp A temporary vector to store the result.
     * @return The relative translation of the given translation from the camera position.
     */
    public Vec3d relative(Vec3d translation, Vec3d tmp) {
        return tmp.set(0).add(translation).sub(cameraPos).mul(1.0 / worldScale);
    }

    /**
     * Returns the world being rendered.
     *
     * @return The world being rendered.
     */
    @Override
    public WorldAccess getWorld() {
        return world;
    }

    /**
     * Returns the render layer where the entities being rendered will be rendered.
     *
     * @return The render layer where the entities being rendered will be rendered.
     */
    public ModelBatch getModelBatch() {
        return modelBatch;
    }
}
