package dev.ultreon.quantum.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * ClientModInit is an interface for client-side mod initialization.
 * It provides a way for mods to perform initialization tasks that are
 * specific to the client-side environment.
 */
public interface ClientModInit extends ClientModInitializer {

    /**
     * The key for the client-side initialization entry point.
     * When the game platform invokes entry points, it uses this key to
     * identify the client-side initialization entry point.
     * <p>
     * This would be the key to use in the {@code fabric.mod.json} file.
     */
    String ENTRYPOINT_KEY = "client-init";

    /**
     * Called when initializing the client-side mod.
     * This method is called when the game platform is initializing the
     * client-side mod. It is the entry point for initializing the client-side
     * mod and should be used for tasks that are specific to the client-side
     * environment, such as registering event listeners or setting up UI
     * components.
     */
    void onInitializeClient();
}
