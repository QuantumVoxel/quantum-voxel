package dev.ultreon.quantum.client.model;

import dev.ultreon.quantum.client.render.RenderContext;
import dev.ultreon.quantum.entity.Entity;

public class EntityDrawBuffer implements RenderContext<Entity> {
    private final Entity entity;

    public EntityDrawBuffer(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Entity getHolder() {
        return entity;
    }
}
