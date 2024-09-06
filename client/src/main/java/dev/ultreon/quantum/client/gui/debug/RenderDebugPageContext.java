package dev.ultreon.quantum.client.gui.debug;

import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.gui.Renderer;
import dev.ultreon.quantum.text.TextObject;

class RenderDebugPageContext implements DebugPageContext {
    private final DebugOverlay debugOverlay;
    private final Renderer renderer;

    public RenderDebugPageContext(DebugOverlay debugOverlay, Renderer renderer) {
        this.debugOverlay = debugOverlay;
        this.renderer = renderer;
    }

    @Override
    public DebugPageContext left() {
        debugOverlay.left();
        return this;
    }

    @Override
    public DebugPageContext left(String text) {
        debugOverlay.left(renderer, text);
        return this;
    }

    @Override
    public DebugPageContext left(String key, Object value) {
        debugOverlay.left(renderer, key, value);
        return this;
    }

    @Override
    public DebugPageContext right() {
        debugOverlay.right();
        return this;
    }

    @Override
    public DebugPageContext right(String text) {
        debugOverlay.right(renderer, text);
        return this;
    }

    @Override
    public DebugPageContext right(String key, Object value) {
        debugOverlay.right(renderer, key, value);
        return this;
    }

    @Override
    public DebugPageContext entryLine(int idx, String name, long nanos) {
        debugOverlay.entryLine(renderer, idx, name, nanos);
        return this;
    }

    @Override
    public DebugPageContext entryLine(String name, String value) {
        debugOverlay.entryLine(renderer, name, value);
        return this;
    }

    @Override
    public DebugPageContext entryLine(int idx, String name) {
        debugOverlay.entryLine(renderer, idx, name);
        return this;
    }

    @Override
    @Deprecated
    public DebugPageContext entryLine(TextObject text) {
        debugOverlay.entryLine(renderer, text);
        return this;
    }

    @Override
    @Deprecated
    public DebugPageContext entryLine(String text) {
        debugOverlay.entryLine(renderer, text);
        return this;
    }

    @Override
    public DebugPageContext entryLine() {
        debugOverlay.entryLine(renderer);
        return this;
    }

    @Override
    public QuantumClient client() {
        return debugOverlay.client;
    }
}
