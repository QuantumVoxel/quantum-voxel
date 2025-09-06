package dev.ultreon.quantum.client.render.material;

import com.badlogic.gdx.graphics.Color;
import dev.ultreon.quantum.client.render.context.ColorSource;
import dev.ultreon.quantum.client.render.context.ColorSrc;
import dev.ultreon.quantum.client.render.context.MaterialType;
import org.jetbrains.annotations.Nullable;

public class EntityMaterial implements MaterialType {
    @Override
    public @Nullable ColorSource getDiffuse() {
        return new ColorSrc(Color.WHITE, "diffuse");
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
