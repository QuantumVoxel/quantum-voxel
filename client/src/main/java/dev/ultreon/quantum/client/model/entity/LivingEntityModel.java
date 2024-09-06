package dev.ultreon.quantum.client.model.entity;

import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import dev.ultreon.quantum.client.render.EntityTextures;
import dev.ultreon.quantum.entity.LivingEntity;

public abstract class LivingEntityModel<T extends LivingEntity> extends EntityModel<T> {
    @Override
    protected abstract void build(ModelBuilder builder, EntityTextures textures);
}
