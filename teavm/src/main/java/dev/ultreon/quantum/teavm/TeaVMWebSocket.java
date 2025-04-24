package dev.ultreon.quantum.teavm;

import dev.ultreon.quantum.network.PacketListener;
import dev.ultreon.quantum.server.CloseCodes;
import org.jetbrains.annotations.Nullable;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.MessageEvent;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.websocket.WebSocket;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TeaVMWebSocket implements dev.ultreon.quantum.WebSocket {
    private final WebSocket socket;
    private final Set<CloseListener> closeListeners = new HashSet<>();
    private final Set<ReceiveListener> receiveListeners = new HashSet<>();
    private final Set<OpenListener> openListeners = new HashSet<>();
    private boolean alive;

    public TeaVMWebSocket(String url, Consumer<Throwable> onError, InitializeListener initializeListener, ConnectedListener connectedListener) {
        initializeListener.handle(this);
        socket = new WebSocket(url);
        socket.setBinaryType("arraybuffer"); // Required for binary data

        socket.addEventListener("open", evt -> {
            connectedListener.handle(this);

            for (OpenListener listener : openListeners) {
                listener.handle(this);
            }
        });

        socket.addEventListener("message", (evt) -> {
            JSObject data = ((MessageEvent) evt).getData();

            if (data instanceof ArrayBuffer) {
                byte[] byteData = toByteArray((ArrayBuffer) data);
                for (ReceiveListener listener : receiveListeners) {
                    if (listener.handle(byteData)) break;
                }
            } else {
                System.err.println("Received non-binary message, ignoring.");
            }
        });

        socket.addEventListener("close", evt -> {
            this.alive = false;

            int code = getCloseEventCode(evt);
            String reason = getCloseEventReason(evt);
            for (CloseListener listener : closeListeners) {
                listener.handle(code, reason);
            }
        });

        socket.addEventListener("error", evt -> {
            onError.accept(new Throwable("WebSocker error occurred!"));
        });
    }

    public void send(byte[] data, @Nullable PacketListener resultListener) {
        Uint8Array uint8Array = new Uint8Array(data.length);
        for (int i = 0; i < data.length; i++) {
            uint8Array.set(i, (short) (data[i] & 0xFF));
        }
        socket.send(uint8Array);
    }

    @Override
    public void disconnect(int statusCode, String reason) {
        socket.close(statusCode, reason);
    }

    @Override
    public void close() {
        alive = false;
        socket.close(CloseCodes.UNEXPECTED_CONDITION.getCode(), "Connection terminated");
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    public void addCloseListener(CloseListener listener) {
        closeListeners.add(listener);
    }

    public void removeCloseListener(CloseListener listener) {
        closeListeners.remove(listener);
    }

    public void addOpenListener(OpenListener listener) {
        openListeners.add(listener);
    }

    public void removeOpenListener(OpenListener listener) {
        openListeners.remove(listener);
    }

    public void addReceiveListener(ReceiveListener listener) {
        receiveListeners.add(listener);
    }

    public void removeReceiveListener(ReceiveListener listener) {
        receiveListeners.remove(listener);
    }

    private byte[] toByteArray(ArrayBuffer buffer) {
        Uint8Array array = new Uint8Array(buffer);
        byte[] result = new byte[array.getLength()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) array.get(i);
        }
        return result;
    }

    @JSBody(params = "evt", script = "return evt.code;")
    private static native int getCloseEventCode(JSObject evt);

    @JSBody(params = "evt", script = "return evt.reason;")
    private static native String getCloseEventReason(JSObject evt);
}