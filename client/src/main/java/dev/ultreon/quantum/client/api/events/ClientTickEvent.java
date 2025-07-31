package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.tick.TickEvent;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.player.ClientPlayer;
import dev.ultreon.quantum.client.world.ClientWorld;

public interface ClientTickEvent extends ClientEvent, TickEvent {
    class WorldTickPre implements ClientTickEvent, ClientWorldEvent, Cancelable {
        private final ClientWorld world;
        private boolean canceled;

        public WorldTickPre(ClientWorld world) {
            this.world = world;
        }

        @Override
        public ClientWorld getWorld() {
            return world;
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

    class WorldTickPost implements ClientTickEvent, ClientWorldEvent {
        private final ClientWorld world;

        public WorldTickPost(ClientWorld world) {
            this.world = world;
        }

        @Override
        public ClientWorld getWorld() {
            return world;
        }
    }

    class PlayerTickPre implements ClientTickEvent, ClientPlayerEvent, Cancelable {
        private final ClientPlayer entity;
        private boolean canceled;

        public PlayerTickPre(ClientPlayer entity) {
            this.entity = entity;
        }

        @Override
        public ClientPlayer getEntity() {
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

    class PlayerTickPost implements ClientTickEvent, ClientPlayerEvent {
        private final ClientPlayer entity;

        public PlayerTickPost(ClientPlayer entity) {
            this.entity = entity;
        }

        @Override
        public ClientPlayer getEntity() {
            return entity;
        }
    }

    class GameTickPre implements ClientTickEvent, Cancelable {
        private final QuantumClient client;
        private boolean canceled;

        public GameTickPre(QuantumClient client) {
            this.client = client;
        }

        @Override
        public QuantumClient getClient() {
            return client;
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

    class GameTickPost implements ClientTickEvent {
        private final QuantumClient client;

        public GameTickPost(QuantumClient client) {
            this.client = client;
        }

        @Override
        public QuantumClient getClient() {
            return client;
        }
    }
}
