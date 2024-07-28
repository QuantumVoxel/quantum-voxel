package dev.ultreon.quantum.client.model;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.client.render.RenderLayer;
import dev.ultreon.quantum.world.WorldAccess;

public class WorldRenderContextImpl<T> implements WorldRenderContext<T> {
    final RenderLayer renderLayer;
    final T holder;
    private final WorldAccess world;
    private final float worldScale;
    private final Vec3d cameraPos;

    public WorldRenderContextImpl(RenderLayer renderLayer, T holder, WorldAccess world, float worldScale, Vec3d cameraPos) {
        this.renderLayer = renderLayer;
        this.holder = holder;
        this.world = world;
        this.worldScale = worldScale;
        this.cameraPos = cameraPos;
    }

    @Override
    public T getHolder() {
        return holder;
    }

    public Vec3d relative(Vec3d translation, Vec3d tmp) {
        return tmp.set(0).add(translation).sub(cameraPos).mul(1.0 / worldScale);
    }

    @Override
    public WorldAccess getWorld() {
        return world;
    }
}
