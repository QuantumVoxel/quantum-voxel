package dev.ultreon.quantum.client.rpc;

import java.util.ServiceLoader;

/**
 * This interface represents a handler for RPC (Rich Presence) operations.
 * Implementations of this interface should define the methods for starting,
 * stopping, and setting the activity for the RPC.
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface RpcHandler {
    Iterable<RpcHandler> HANDLERS = ServiceLoader.load(RpcHandler.class);

    /**
     * Starts the RPC handler, initializing any necessary resources or connections.
     * 
     * @see #close()
     */
    void start();

    /**
     * Closes the RPC handler, releasing any allocated resources.
     * This method should be called before the application shuts down to ensure
     * that resources are properly released and any necessary cleanup is performed.
     * 
     * @see #start()
     */
    void close();

    /**
     * Set the current activity for the game.
     *
     * @param newActivity the new activity to be set
     */
    void setActivity(GameActivity newActivity);

    /**
     * Enables all registered RPC handlers by calling their start method.
     * This will initialize any necessary resources or connections required
     * for the handlers.
     */
    static void enable() {
        for (RpcHandler handler : HANDLERS) {
            handler.start();
        }
    }

    /**
     * Disables all registered RPC handlers by calling their close method.
     * This method ensures that all handlers release any allocated resources
     * and perform necessary cleanup operations.
     */
    static void disable() {
        for (RpcHandler handler : HANDLERS) {
            handler.close();
        }
    }

    /**
     * Updates the activity for all registered RpcHandlers to the specified new activity.
     *
     * @param newActivity the new GameActivity to be set for all handlers
     */
    static void newActivity(GameActivity newActivity) {
        for (RpcHandler handler : HANDLERS) {
            handler.setActivity(newActivity);
        }
    }
}
