package dev.ultreon.quantum.switchgdx;

import dev.ultreon.quantum.WebSocket;
import dev.ultreon.quantum.network.PacketListener;
import org.jetbrains.annotations.Nullable;

public class NullWebSocket implements WebSocket {
    @Override
    public void addCloseListener(CloseListener listener) {

    }

    @Override
    public void removeCloseListener(CloseListener listener) {

    }

    @Override
    public void addOpenListener(OpenListener listener) {

    }

    @Override
    public void removeOpenListener(OpenListener listener) {

    }

    @Override
    public void addReceiveListener(ReceiveListener listener) {

    }

    @Override
    public void removeReceiveListener(ReceiveListener listener) {

    }

    @Override
    public void send(byte[] data, @Nullable PacketListener resultListener) {

    }

    @Override
    public void disconnect(int statusCode, String reason) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isAlive() {
        return false;
    }
}
