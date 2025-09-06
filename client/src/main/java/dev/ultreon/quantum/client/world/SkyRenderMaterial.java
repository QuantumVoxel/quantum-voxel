package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.client.render.context.ColorSource;
import dev.ultreon.quantum.client.render.context.MaterialType;
import org.jetbrains.annotations.Nullable;

public class SkyRenderMaterial implements MaterialType {
    @Override
    public @Nullable ColorSource getDiffuse() {
        return null;
    }

    @Override
    public @Nullable ColorSource getSpecular() {
        return null;
    }

    @Override
    public @Nullable ColorSource getEmission() {
        return null;
    }

    @Override
    public @Nullable ColorSource getReflectiveness() {
        return null;
    }

    @Override
    public @Nullable ColorSource getAmbient() {
        return null;
    }

    @Override
    public @Nullable ColorSource getShininess() {
        return null;
    }

    @Override
    public @Nullable ColorSource getTransparency() {
        return null;
    }
}
