package dev.ultreon.quantum.api.events;

import dev.ultreon.quantum.api.event.Event;
import dev.ultreon.quantum.server.QuantumServer;
import org.jetbrains.annotations.NotNull;

public interface ServerEvent extends Event {
    QuantumServer getServer();

    class Started implements ServerEvent {
        private final QuantumServer server;

        public Started(QuantumServer server) {
            this.server = server;
        }

        @Override
        public @NotNull QuantumServer getServer() {
            return server;
        }
    }

    class Starting implements ServerEvent {
        private final QuantumServer server;

        public Starting(QuantumServer server) {
            this.server = server;
        }

        @Override
        public @NotNull QuantumServer getServer() {
            return server;
        }
    }

    class Stopped implements ServerEvent {
        private final QuantumServer server;

        public Stopped(QuantumServer server) {
            this.server = server;
        }

        @Override
        public @NotNull QuantumServer getServer() {
            return server;
        }
    }

    class Stopping implements ServerEvent {
        private final QuantumServer server;

        public Stopping(QuantumServer server) {
            this.server = server;
        }

        @Override
        public @NotNull QuantumServer getServer() {
            return server;
        }
    }
}
