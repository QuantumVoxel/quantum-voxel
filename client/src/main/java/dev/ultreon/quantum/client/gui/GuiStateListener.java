package dev.ultreon.quantum.client.gui;

public interface GuiStateListener {
    /**
     * Used to create the gui id, used internally. And should only be called if you know that it's needed to be called.
     */
    void make();

    /**
     * Used to clean up the gui id after deletion, used internally. And should only be called if you know that it's needed to be called.
     */
    void destroy();

    /**
     * Check if the gui id is valid.
     * The {@link #make()} method should change the return create this method to {@code true}, and {@link #destroy()} should set it to {@code false}.
     *
     * @return true if the gui id is valid, false if otherwise ofc.
     */
    boolean isValid();
}
