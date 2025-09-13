package dev.ultreon.quantum.android;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.WebSocket;
import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.server.CloseCodes;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class JavaWebSocket implements WebSocket {
//    private java.net.http.WebSocket socket;
    private final Set<CloseListener> closeListeners = new HashSet<>();
    private final Set<ReceiveListener> receiveListeners = new HashSet<>();
    private final Set<OpenListener> openListeners = new HashSet<>();
//    private final ConnectedListener listener;
    private final ExecutorService webSocketPool = Executors.newCachedThreadPool();

    public JavaWebSocket(String location, Consumer<Throwable> onError, InitializeListener initializeListener, ConnectedListener listener) {
//        HttpClient client = HttpClient.newHttpClient();
//        initializeListener.handle(this);
//        this.listener = listener;
//
//        client.newWebSocketBuilder().header("Ultreon-QuantumVoxel-Client", "Yes").buildAsync(URI.create(location), this).exceptionally(throwable -> {
//            onError.accept(throwable);
//            return null;
//        });
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
//        socket.sendBinary(ByteBuffer.wrap(data), true).handle((socket, throwable) -> {
//            if (resultListener != null) {
//                if (throwable == null)
//                    resultListener.onSuccess();
//                else resultListener.onFailure();
//            }
//
//            return socket;
//        });
    }

    @Override
    public void disconnect(int statusCode, String reason) {
//        socket.sendClose(statusCode, reason).handle((socket, throwable) -> {
//            if (throwable instanceof TimeoutException) socket.abort();
//            return null;
//        });
    }

    @Override
    public void close() {
//        if (socket == null) return;
//        socket.abort();
//        socket = null;
        webSocketPool.shutdown();
    }

    @Override
    public boolean isAlive() {
//        return !socket.isInputClosed() && !socket.isOutputClosed();
        return false;
    }
}
