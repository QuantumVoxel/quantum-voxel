package dev.ultreon.quantum;

/**
 * This interface represents a modification initializer.
 */
public interface ModInit {
    /**
     * Key for the entry point.
     */
    String ENTRYPOINT_KEY = "init";

    /**
     * Method called during initialization.
     */
    void onInitialize();
}
