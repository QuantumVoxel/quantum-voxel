package dev.ultreon.quantum.client;

import com.badlogic.gdx.utils.Disposable;

public interface DisposableContainer extends Disposable {
    <T extends Disposable> T deferDispose(T disposable);
}
