package dev.ultreon.quantum.client.model;

import dev.ultreon.quantum.client.render.RenderContext;
import dev.ultreon.quantum.world.World;

public interface WorldRenderContext<T> extends RenderContext<T> {
    World getWorld();
}
