package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.utils.Disposable;

public interface DeferredDisposable extends Disposable {
    <T extends Disposable> T deferDispose(T disposable);
}
