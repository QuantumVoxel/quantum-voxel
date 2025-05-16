package dev.ultreon.quantum.client.gui.debug;

public interface DebugPage {
    DebugPage EMPTY = context -> {
        // Empty debug page
    };

    void render(DebugPageContext context);

    default void update(boolean selected) {

    }
}
