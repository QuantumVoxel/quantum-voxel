package com.ultreon.quantum.client.model.entity;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.ultreon.quantum.client.render.EntityTextures;
import com.ultreon.quantum.entity.LivingEntity;

public abstract class LivingEntityModel<T extends LivingEntity> extends EntityModel<T> {
    @Override
    protected abstract void build(ModelBuilder builder, EntityTextures textures);
}
