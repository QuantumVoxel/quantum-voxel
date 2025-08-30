package dev.ultreon.quantum.desktop.bridge;

import com.badlogic.gdx.utils.Disposable;

public interface LanguageBridge extends Disposable {
    void init();

    boolean fireEvent(String event, Object... args);

    void execFile(String name);

    void dispose();
}
