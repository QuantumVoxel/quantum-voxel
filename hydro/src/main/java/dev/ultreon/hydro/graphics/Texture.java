package dev.ultreon.hydro.graphics;

import dev.ultreon.hydro.core.Destroyable;

public interface Texture extends Destroyable {
    int getWidth();
    int getHeight();

    void bind();
}
