package dev.ultreon.quantum.api.events.tick;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.PlayerEvent;
import dev.ultreon.quantum.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface PlayerTickEvent extends TickEvent, PlayerEvent {
    class Pre implements PlayerTickEvent, Cancelable {
        private final @Nullable Player entity;
        private boolean canceled;

        public Pre(@Nullable Player entity) {
            this.entity = entity;
        }

        @Override
        public @Nullable Player getEntity() {
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

    class Post implements PlayerTickEvent {
        private final @Nullable Player entity;

        public Post(@Nullable Player entity) {
            this.entity = entity;
        }

        @Override
        public @Nullable Player getEntity() {
            return entity;
        }
    }
}
