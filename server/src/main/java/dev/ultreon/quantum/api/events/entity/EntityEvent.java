package dev.ultreon.quantum.api.events.entity;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.entity.damagesource.DamageSource;
import dev.ultreon.quantum.ubo.types.MapType;
import dev.ultreon.quantum.util.Vec;
import dev.ultreon.quantum.world.Location;
import dev.ultreon.quantum.world.World;
import dev.ultreon.quantum.world.WorldAccess;
import dev.ultreon.quantum.world.vec.BlockVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityEvent extends Event {
    @Nullable Entity getEntity();
    default <T extends Entity> @Nullable T getEntity(Class<T> clazz) {
        if (clazz.isInstance(this.getEntity())) {
            return clazz.cast(this.getEntity());
        }
        return null;
    }

    default boolean isEntity(Class<? extends Entity> clazz) {
        return clazz.isInstance(this.getEntity());
    }

    default @Nullable World getWorld() {
        if (this.getEntity() == null) {
            return null;
        }
        return this.getEntity().getWorld();
    }

    default @Nullable BlockVec getBlockVec() {
        if (this.getEntity() == null) {
            return null;
        }
        return this.getEntity().getBlockVec();
    }

    default @Nullable Location getLocation() {
        if (this.getEntity() == null) {
            return null;
        }
        return this.getEntity().getLocation();
    }

    class Damage implements Cancelable, EntityEvent {
        private final Entity entity;
        private final DamageSource source;
        private float damage;
        private boolean canceled;
        private boolean isIgnoringLimits;
        private boolean isBypassingImmunity;

        public Damage(Entity entity, DamageSource source, float damage) {
            this.entity = entity;
            this.source = source;
            this.damage = damage;
        }

        public float getDamage() {
            return this.damage;
        }

        public boolean isIgnoringLimits() {
            return this.isIgnoringLimits;
        }

        public void setIgnoringLimits(boolean ignoringLimits) {
            this.isIgnoringLimits = ignoringLimits;
        }

        public boolean isBypassingImmunity() {
            return this.isBypassingImmunity;
        }

        public void setBypassingImmunity(boolean bypassingImmunity) {
            this.isBypassingImmunity = bypassingImmunity;
        }

        public void setDamage(float damage) {
            this.damage = damage;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        @Override
        public @Nullable Entity getEntity() {
            return entity;
        }

        public DamageSource getSource() {
            return source;
        }
    }

    class Move implements Cancelable, EntityEvent {
        @NotNull
        private final Entity entity;
        private final @NotNull Vec delta;
        private boolean canceled;

        public Move(@NotNull Entity entity, @NotNull Vec delta) {
            this.entity = entity;
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

        @Override
        public @NotNull Entity getEntity() {
            return entity;
        }
    }

    class Load implements EntityEvent {
        @NotNull
        private final Entity entity;
        private final MapType extra;

        public Load(@NotNull Entity entity, @NotNull MapType extra) {
            this.entity = entity;
            this.extra = extra;
        }

        @Override
        public @NotNull Entity getEntity() {
            return entity;
        }

        public @NotNull MapType getExtra() {
            return extra;
        }
    }

    class Removed implements EntityEvent {
        private final Entity entity;

        public Removed(Entity entity) {
            this.entity = entity;
        }

        @Override
        public @Nullable Entity getEntity() {
            return entity;
        }
    }
}
