package dev.ultreon.quantum.client.render.gizmo;

import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.render.context.ColorSource;
import dev.ultreon.quantum.client.render.context.MaterialType;

public class GizmoMaterial implements MaterialType {
    private final Gizmo gizmo;

    public GizmoMaterial(Gizmo gizmo) {
        this.gizmo = gizmo;
    }

    @Override
    public ColorSource getDiffuse() {
        return null;
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

    public boolean isOutline() {
        return gizmo.outline;
    }
}
