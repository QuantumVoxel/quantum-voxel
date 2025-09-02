package dev.ultreon.quantum.client.world;

import dev.ultreon.quantum.client.debug.Gizmo;
import dev.ultreon.quantum.client.util.RenderObject;
import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class RenderEntity extends RenderObject {
    private final Entity entity;
    public @Nullable Gizmo boundsGizmo;

    public RenderEntity(Entity entity) {
        this.entity = entity;
        this.name = entity.toString();
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RenderEntity) obj;
        return Objects.equals(this.entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity);
    }

    @Override
    public String toString() {
        return "RenderEntity[" +
               "entity=" + entity + ']';
    }
}
