package dev.ultreon.quantum.client.render.context;

import org.jetbrains.annotations.Nullable;

public interface MaterialType {
    @Nullable ColorSource getDiffuse();
    @Nullable ColorSource getSpecular();
    @Nullable ColorSource getEmission();
    @Nullable ColorSource getReflectiveness();
    @Nullable ColorSource getAmbient();
    @Nullable ColorSource getShininess();
    @Nullable ColorSource getTransparency();
}
