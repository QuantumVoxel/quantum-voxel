package dev.ultreon.quantum.entity;

import dev.ultreon.quantum.entity.util.EntitySize;
import dev.ultreon.quantum.registry.Registries;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.world.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public abstract class EntityType<T extends Entity> {
    private final EntitySize size;
    private float eyeHeight = 0.5f;

    private EntityType(Builder<T> properties) {
        this.size = new EntitySize(properties.width, properties.height);
        this.eyeHeight = properties.eyeHeight;
    }

    public abstract T create(World world);

    public T spawn(World world) {
        T t = this.create(world);
        world.spawn(t, new MapType());
        return t;
    }

    public T spawn(World world, MapType spawnData) {
        T t = this.create(world);
        world.spawn(t, spawnData);
        return t;
    }

    public EntitySize getSize() {
        return this.size;
    }

    public @Nullable NamespaceID getId() {
        return Registries.ENTITY_TYPE.getId(this);
    }

    public float getEyeHeight() {
        return eyeHeight;
    }

    public static class Builder<T extends Entity> {
        private float eyeHeight = 0.5f;
        private float width = 0.8f;
        private float height = 1.9f;
        @Nullable
        private EntityFactory<T> factory;

        public Builder() {

        }

        @Contract("_,_->this")
        public Builder<T> size(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        @Contract("_->this")
        public Builder<T> eyeHeight(float eyeHeight) {
            this.eyeHeight = eyeHeight;
            return this;
        }

        @Contract("_->this")
        public Builder<T> factory(EntityFactory<T> factory) {
            this.factory = factory;
            return this;
        }

        public EntityType<T> build() {
            return new EntityType<>(this) {
                @Override
                public T create(World world) {
                    return Builder.this.factory.create(this, world);
                }
            };
        }
    }

    @FunctionalInterface
    public interface EntityFactory<T extends Entity> {
        T create(EntityType<T> type, World world);
    }
}
