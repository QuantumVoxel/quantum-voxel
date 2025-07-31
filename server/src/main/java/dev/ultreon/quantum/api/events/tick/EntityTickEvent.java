package dev.ultreon.quantum.api.events.tick;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.entity.EntityEvent;
import dev.ultreon.quantum.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EntityTickEvent extends TickEvent, EntityEvent {
    class Pre implements EntityTickEvent, Cancelable {
        private final @Nullable Entity entity;
        private boolean canceled;

        public Pre(@Nullable Entity entity) {
            this.entity = entity;
        }

        @Override
        public @Nullable Entity getEntity() {
            return entity;
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

    class Post implements EntityTickEvent {
        private final @Nullable Entity entity;

        public Post(@Nullable Entity entity) {
            this.entity = entity;
        }

        @Override
        public @Nullable Entity getEntity() {
            return entity;
        }
    }
}
