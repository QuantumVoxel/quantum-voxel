package com.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;

public interface RenderContext<T> {
    T getHolder();

    void render(RenderableProvider renderableProvider);
}
