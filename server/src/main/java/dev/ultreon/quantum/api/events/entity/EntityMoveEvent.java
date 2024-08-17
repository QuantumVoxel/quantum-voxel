package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.util.Vec;
import org.jetbrains.annotations.NotNull;

public class EntityMoveEvent extends EntityEvent implements Cancelable {
    private final @NotNull Vec delta;
    private boolean canceled;

    public EntityMoveEvent(@NotNull Entity entity, @NotNull Vec delta) {
        super(entity);
        this.delta = delta;
    }

    public @NotNull Vec getDelta() {
        return delta;
    }

    public void setDelta(@NotNull Vec delta) {
        this.delta.set(delta);
    }

    public void setDelta(double x, double y, double z) {
        this.delta.set(x, y, z);
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
