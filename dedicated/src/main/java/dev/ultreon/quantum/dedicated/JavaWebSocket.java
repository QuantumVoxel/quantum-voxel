package dev.ultreon.quantum.dedicated;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.WebSocket;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.server.CloseCodes;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class JavaWebSocket implements WebSocket, java.net.http.WebSocket.Listener {
    private final HttpClient client;
    private boolean connecting = true;
    private boolean connected = false;
    private java.net.http.WebSocket socket;
    private final Set<WebSocket.CloseListener> closeListeners = new HashSet<>();
    private final Set<WebSocket.ReceiveListener> receiveListeners = new HashSet<>();
    private final Set<WebSocket.OpenListener> openListeners = new HashSet<>();
    private final ConnectedListener listener;

    public JavaWebSocket(String location, Consumer<Throwable> onError, InitializeListener initializeListener, ConnectedListener listener) {
        this.client = HttpClient.newHttpClient();
        initializeListener.handle(this);
        this.listener = listener;

        this.client.newWebSocketBuilder().header("Ultreon-QuantumVoxel-Client", "Yes").buildAsync(URI.create(location), this).exceptionally(throwable -> {
            onError.accept(throwable);
            return null;
        });
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        this.closeListeners.add(listener);
    }

    @Override
    public void removeCloseListener(CloseListener listener) {
        this.closeListeners.remove(listener);
    }

    @Override
    public void addOpenListener(OpenListener listener) {
        this.openListeners.add(listener);
    }

    @Override
    public void removeOpenListener(OpenListener listener) {
        this.openListeners.remove(listener);
    }

    @Override
    public void addReceiveListener(ReceiveListener listener) {
        this.receiveListeners.add(listener);
    }

    @Override
    public void removeReceiveListener(ReceiveListener listener) {
        this.receiveListeners.remove(listener);
    }

    @Override
    public void send(byte[] data, @Nullable PacketListener resultListener) {
        socket.sendBinary(ByteBuffer.wrap(data), true).handle((socket, throwable) -> {
            if (resultListener != null) {
                if (throwable == null)
                    resultListener.onSuccess();
                else resultListener.onFailure();
            }

            return socket;
        });
    }

    @Override
    public void disconnect(int statusCode, String reason) {
        socket.sendClose(statusCode, reason).handle((socket, throwable) -> {
            if (throwable instanceof TimeoutException) socket.abort();
            return null;
        });
    }

    @Override
    public void close() {
        if (socket == null) return;
        socket.abort();
    }

    @Override
    public boolean isAlive() {
        return !socket.isInputClosed() && !socket.isOutputClosed();
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        this.socket = webSocket;
        this.connected = true;
        this.connecting = false;
        listener.handle(this);
        java.net.http.WebSocket.Listener.super.onOpen(webSocket);
        this.openListeners.forEach(openListener -> openListener.handle(this));
    }

    @Override
    public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onBinary(java.net.http.WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            for (ReceiveListener receiveListener : receiveListeners) {
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                if (receiveListener.handle(bytes)) {
                    webSocket.request(1);
                    return CompletableFuture.completedFuture(null);
                }
            }
            CommonConstants.LOGGER.error("Didn't handle packet! (This shouldn't happen)");
            disconnect(CloseCodes.UNEXPECTED_CONDITION.getCode(), "We didn't handle the packet!");
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to handle data:", e);
            disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "We didn't handle the packet!");
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        closeListeners.forEach(listener -> listener.handle(statusCode, reason));
        return CompletableFuture.runAsync(webSocket::abort);
    }

    @Override
    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
        disconnect(500, "Internal connection error!\n" + error.getLocalizedMessage());
        java.net.http.WebSocket.Listener.super.onError(webSocket, error);
    }
}
