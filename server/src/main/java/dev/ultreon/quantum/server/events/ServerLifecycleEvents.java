package dev.ultreon.quantum.server.events;

import dev.ultreon.quantum.events.api.Event;
import dev.ultreon.quantum.server.QuantumServer;

public class ServerLifecycleEvents {
    public static final Event<ServerStarting> SERVER_STARTING = Event.create();
    public static final Event<ServerStarted> SERVER_STARTED = Event.create();
    public static final Event<ServerStopping> SERVER_STOPPING = Event.create();
    public static final Event<ServerStopped> SERVER_STOPPED = Event.create();

    @FunctionalInterface
    public interface ServerStarting {
        void onServerStarting(QuantumServer server);
    }

    @FunctionalInterface
    public interface ServerStarted {
        void onServerStarted(QuantumServer server);
    }
    
    @FunctionalInterface
    public interface ServerStopping {
        void onServerStopping(QuantumServer server);
    }

    @FunctionalInterface
    public interface ServerStopped {
        void onServerStopped(QuantumServer server);
    }
}
