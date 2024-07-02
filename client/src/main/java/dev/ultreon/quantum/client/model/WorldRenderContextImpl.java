package dev.ultreon.quantum.client.model;

import dev.ultreon.libs.commons.v0.vector.Vec3d;
import dev.ultreon.quantum.world.World;

public class WorldRenderContextImpl<T> implements WorldRenderContext<T> {
    final T holder;
    private final World world;
    private final float worldScale;
    private final Vec3d cameraPos;

    public WorldRenderContextImpl(T holder, World world, float worldScale, Vec3d cameraPos) {
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
    public World getWorld() {
        return world;
    }
}
