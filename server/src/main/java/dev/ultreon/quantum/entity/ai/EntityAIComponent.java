package dev.ultreon.quantum.entity.ai;

import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.component.GameComponent;
import dev.ultreon.quantum.util.Tickable;
import dev.ultreon.quantum.util.Updatable;

public abstract class EntityAIComponent<T> extends GameComponent implements Tickable, Disposable {
    public abstract T getTarget();
    public abstract void setTarget(T target);
    public abstract boolean hasTarget();
    public abstract void reset();

    @Override
    public void dispose() {

    }
}
