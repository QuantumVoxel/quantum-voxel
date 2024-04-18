package com.ultreon.quantum.client.gui.debug;

public interface DebugPage {
    DebugPage EMPTY = new DebugPage() {
        @Override
        public void render(DebugPageContext context) {
            // Empty debug page
        }
    };

    void render(DebugPageContext context);
}
