package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.world.Location;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.Nullable;

public abstract class EntityEvent extends Event {
    private final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public <T extends Entity> @Nullable T getEntity(Class<T> clazz) {
        if (clazz.isInstance(this.entity)) {
            return clazz.cast(this.entity);
        }
        return null;
    }

    public boolean isEntity(Class<? extends Entity> clazz) {
        return clazz.isInstance(this.entity);
    }

    public WorldAccess getWorld() {
        return this.entity.getWorld();
    }

    public BlockVec getBlockVec() {
        return this.entity.getBlockVec();
    }

    public Location getLocation() {
        return this.entity.getLocation();
    }
}
