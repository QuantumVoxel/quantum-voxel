package com.ultreon.quantum.client.model;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.ultreon.quantum.client.render.RenderContext;
import com.ultreon.quantum.entity.Entity;

public class EntityDrawBuffer implements RenderContext<Entity> {
    private Entity entity;

    public EntityDrawBuffer(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Entity getHolder() {
        return entity;
    }

    @Override
    public void render(RenderableProvider renderableProvider) {

    }
}
