package dev.ultreon.quantum.client.render.world;

import dev.ultreon.quantum.client.render.context.ColorSource;
import dev.ultreon.quantum.client.render.context.MaterialType;
import dev.ultreon.quantum.client.render.context.TextureSrc;

public class MoonMaterial implements MaterialType {
    private final TextureSrc sunTexture;

    public MoonMaterial(TextureSrc sunTexture) {
        this.sunTexture = sunTexture;
    }

    @Override
    public ColorSource getDiffuse() {
        return sunTexture;
    }

    @Override
    public ColorSource getSpecular() {
        return null;
    }

    @Override
    public ColorSource getEmission() {
        return null;
    }

    @Override
    public ColorSource getReflectiveness() {
        return null;
    }

    @Override
    public ColorSource getAmbient() {
        return null;
    }

    @Override
    public ColorSource getShininess() {
        return null;
    }

    @Override
    public ColorSource getTransparency() {
        return null;
    }
}
