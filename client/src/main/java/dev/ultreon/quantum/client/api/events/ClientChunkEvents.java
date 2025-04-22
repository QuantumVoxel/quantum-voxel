package dev.ultreon.quantum.client.api.events;

import dev.ultreon.quantum.client.world.ClientChunk;
import dev.ultreon.quantum.events.api.Event;

public class ClientChunkEvents {
    public static final Event<Received> RECEIVED = Event.create(listeners -> chunk -> {
        for (Received listener : listeners) {
            listener.onClientChunkReceived(chunk);
        }
    });
    public static final Event<Rebuilt> REBUILT = Event.create(listeners -> chunk -> {
        for (Rebuilt listener : listeners) {
            listener.onClientChunkRebuilt(chunk);
        }
    });
    public static final Event<Rebuilt> BUILT = Event.create(listeners -> chunk -> {
        for (Rebuilt listener : listeners) {
            listener.onClientChunkRebuilt(chunk);
        }
    });

    @FunctionalInterface
    public interface Received {
        void onClientChunkReceived(ClientChunk chunk);
    }

    @FunctionalInterface
    public interface Rebuilt {
        void onClientChunkRebuilt(ClientChunk chunk);
    }

    @FunctionalInterface
    public interface Built {
        void onClientChunkBuilt(ClientChunk chunk);
    }
}
