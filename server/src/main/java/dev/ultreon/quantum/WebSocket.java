package dev.ultreon.quantum;

import dev.ultreon.quantum.network.PacketListener;
import org.jetbrains.annotations.Nullable;

public interface WebSocket {
    void addCloseListener(CloseListener listener);

    void removeCloseListener(CloseListener listener);

    void addOpenListener(OpenListener listener);

    void removeOpenListener(OpenListener listener);

    void addReceiveListener(ReceiveListener listener);

    void removeReceiveListener(ReceiveListener listener);

    void send(byte[] data, @Nullable PacketListener resultListener);

    default void onInternalError(Throwable throwable) {
        disconnect(500, "Internal error: " + throwable.toString());
    }

    void disconnect(int statusCode, String reason);

    void close();

    boolean isAlive();

    @FunctionalInterface
    interface CloseListener {
        void handle(int statusCode, String message);
    }

    @FunctionalInterface
    interface ReceiveListener {
        boolean handle(byte[] data);
    }

    @FunctionalInterface
    interface ConnectedListener {
        void handle(WebSocket socket);
    }

    @FunctionalInterface
    interface InitializeListener {
        void handle(WebSocket socket);
    }

    @FunctionalInterface
    public interface OpenListener {
        void handle(WebSocket socket);
    }
}
