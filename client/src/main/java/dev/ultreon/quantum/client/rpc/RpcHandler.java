package dev.ultreon.quantum.client.rpc;

import java.util.ServiceLoader;

public interface RpcHandler {
    Iterable<RpcHandler> HANDLERS = ServiceLoader.load(RpcHandler.class);

    void start();

    void setActivity(GameActivity newActivity);

    static void enable() {
        for (RpcHandler handler : HANDLERS) {
            handler.start();
        }
    }

    static void newActivity(GameActivity newActivity) {
        for (RpcHandler handler : HANDLERS) {
            handler.setActivity(newActivity);
        }
    }
}
