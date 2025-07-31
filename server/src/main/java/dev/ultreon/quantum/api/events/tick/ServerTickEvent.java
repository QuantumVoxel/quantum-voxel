package dev.ultreon.quantum.api.events.tick;

import dev.ultreon.quantum.api.events.Cancelable;
import dev.ultreon.quantum.api.events.ServerEvent;
import dev.ultreon.quantum.server.QuantumServer;

public abstract class ServerTickEvent implements TickEvent, ServerEvent {
    public static class Pre extends ServerTickEvent implements Cancelable {
        private final QuantumServer server;
        private boolean canceled;

        public Pre(QuantumServer server) {
            this.server = server;
        }

        @Override
        public QuantumServer getServer() {
            return server;
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
    
    public static class Post extends ServerTickEvent {
        private final QuantumServer server;
        
        public Post(QuantumServer server) {
            this.server = server;
        }
        
        @Override
        public QuantumServer getServer() {
            return server;
        }
    }
}
