package dev.ultreon.quantum.client;

import com.badlogic.gdx.utils.Disposable;

public sealed interface DesktopMain extends Disposable permits DataGeneratorClient, QuantumClient {
    void resize(int width, int height);

    void render();

    void pause();

    void resume();
}
