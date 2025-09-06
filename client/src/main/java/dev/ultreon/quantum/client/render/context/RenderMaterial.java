package dev.ultreon.quantum.client.render.context;

import org.jetbrains.annotations.Nullable;

public final class RenderMaterial {
    private final @Nullable MaterialType materialType;
    private final ObjectType objectType;
    private final String name;

    public RenderMaterial(@Nullable MaterialType materialType, ObjectType objectType, String name) {
        this.materialType = materialType;
        this.objectType = objectType;
        this.name = name;
    }

    public @Nullable MaterialType getMaterialType() {
        return materialType;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getName() {
        return name;
    }
}
