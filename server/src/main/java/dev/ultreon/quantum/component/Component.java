package dev.ultreon.quantum.component;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.util.GameObject;

public abstract class Component<T> implements Disposable {
    private final Class<? extends T> holder;
    private final Array<GameObject> objects = new Array<>();

    protected Component(Class<? extends T> holder) {
        this.holder = holder;
    }

    public Class<? extends T> getHolder() {
        return this.holder;
    }

    public void onAdded(GameObject object) {
        this.objects.add(object);
    }

    public void onRemoved(GameObject object) {
        this.objects.removeValue(object, true);

        if (this.objects.isEmpty()) {
            this.dispose();
        }
    }

    public void dispose() {

    }
}
