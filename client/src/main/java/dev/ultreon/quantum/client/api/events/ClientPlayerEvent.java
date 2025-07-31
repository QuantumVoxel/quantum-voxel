package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.PlayerEvent;
import dev.ultreon.quantum.client.player.ClientPlayer;
import dev.ultreon.quantum.client.player.LocalPlayer;

public interface ClientPlayerEvent extends ClientEvent, PlayerEvent {
    @Override
    ClientPlayer getEntity();

    class Joined implements ClientPlayerEvent {
        private final LocalPlayer entity;

        public Joined(LocalPlayer entity) {
            this.entity = entity;
        }

        @Override
        public LocalPlayer getEntity() {
            return entity;
        }
    }

    class Left implements ClientPlayerEvent {
        private final LocalPlayer entity;
        private final String message;

        public Left(LocalPlayer player, String message) {
            this.entity = player;
            this.message = message;
        }

        public LocalPlayer getEntity() {
            return entity;
        }

        public String getMessage() {
            return message;
        }
    }

    class Jump implements ClientPlayerEvent {
        private final LocalPlayer entity;

        public Jump(LocalPlayer entity) {
            this.entity = entity;
        }

        @Override
        public LocalPlayer getEntity() {
            return entity;
        }
    }
}
