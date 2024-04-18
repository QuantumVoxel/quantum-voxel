package com.ultreon.quantum.client.model;

import com.ultreon.quantum.client.render.RenderContext;
import com.ultreon.quantum.world.World;

public interface WorldRenderContext<T> extends RenderContext<T> {
    World getWorld();
}
