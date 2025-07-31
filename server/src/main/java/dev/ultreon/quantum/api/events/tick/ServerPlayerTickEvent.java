package dev.ultreon.quantum.api.events.tick;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.ServerPlayerEvent;
import dev.ultreon.quantum.server.player.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public abstract class ServerPlayerTickEvent implements TickEvent, ServerPlayerEvent {
    private final ServerPlayer entity;

    protected ServerPlayerTickEvent(ServerPlayer entity) {
        this.entity = entity;
    }

    @Override
    public @Nullable ServerPlayer getEntity() {
        return entity;
    }

    public static class Pre extends ServerPlayerTickEvent implements Cancelable {
        private boolean canceled;

        public Pre(ServerPlayer entity) {
            super(entity);
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

    public static class Post extends ServerPlayerTickEvent {
        public Post(ServerPlayer entity) {
            super(entity);
        }
    }
}
