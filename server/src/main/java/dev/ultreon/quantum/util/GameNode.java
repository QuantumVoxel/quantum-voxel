package dev.ultreon.quantum.util;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.component.Component;
import dev.ultreon.quantum.component.GameComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GameNode implements Disposable {
    private static final Array<GameNode> MANAGED = new Array<>();
    private static int allActiveCount;

    final @NotNull Map<Class<?>, Component<?>> components = new ConcurrentHashMap<>();
    final @NotNull Array<GameNode> children = new Array<>();

    public boolean enabled = true;
    public String name;
    public String description;

    @HiddenNode
    public GameNode parent;
    protected int activeCount;
    protected boolean updateAnyways;

    public GameNode() {
        if (getClass().isAnnotationPresent(NodeDescription.class))
            description = getClass().getAnnotation(NodeDescription.class).value();

        MANAGED.add(this);
    }

    public final void attach(GameNode parent) {
        this.parent = parent;
        activeCount++;

        this.onAttach();
    }

    protected void onAttach() {

    }

    public <T> boolean has(@NotNull Class<T> type) {
        synchronized (components) {
            return components.containsKey(type);
        }
    }

    public void update(float delta) {
        synchronized (components) {
            for (Component<?> component : components.values()) {
                if (component instanceof Updatable) {
                    Updatable updatable = (Updatable) component;
                    updatable.update(delta);
                }
            }
        }

        synchronized (children) {
            for (GameNode child : children) {
                if (!child.enabled && !child.updateAnyways) continue;

                if (child instanceof GameObject) {
                    GameObject gameObject = (GameObject) child;
                    gameObject.combined.set(gameObject.transform)
                            .translate(gameObject.translation)
                            .rotate(Vector3.X, gameObject.rotation.x)
                            .rotate(Vector3.Y, gameObject.rotation.y)
                            .rotate(Vector3.Z, gameObject.rotation.z)
                            .scale(gameObject.scale.x, gameObject.scale.y, gameObject.scale.z);
                }

                child.update(delta);
            }
        }
    }

    public void tick() {
        synchronized (components) {
            for (Component<?> component : components.values()) {
                if (component instanceof Tickable) {
                    Tickable tickable = (Tickable) component;
                    tickable.tick();
                }
            }
        }

        synchronized (children) {
            for (GameNode child : children) {
                child.tick();
            }
        }
    }

    public void dispose() {
        enabled = false;

        onDisable();

        synchronized (components) {
            for (Component<?> component : components.values()) {
                if (component instanceof Disposable) {
                    Disposable disposable = (Disposable) component;
                    disposable.dispose();
                }
            }
        }

        synchronized (children) {
            for (GameNode child : children) {
                child.dispose();
            }
        }

        MANAGED.removeValue(this, true);
        if (enabled) {
            if (parent != null) parent.activeCount--;
            allActiveCount--;
        }

        onDisable();
    }

    public boolean isVisible() {
        return enabled;
    }

    public void setVisible(boolean visible) {
        setVisible(visible, false);
    }

    public void setVisible(boolean visible, boolean force) {
        if (enabled == visible) return;
        enabled = visible;

        if (enabled) {
            this.onEnable();
            if (parent != null && force) parent.setVisible(true, true);
            if (parent != null) parent.activeCount++;
            allActiveCount++;
        } else {
            this.onDisable();
            if (parent != null) parent.activeCount--;
            allActiveCount--;
        }
    }

    private void onDisable() {

    }

    private void onEnable() {

    }

    public void add(String name, GameNode child) {
        synchronized (children) {
            child.name = name;
            children.add(child);
            child.create();
            child.parent = this;
        }
    }

    private void create() {
        onCreate();
    }

    protected void onCreate() {

    }

    protected void onDispose() {

    }

    public boolean isUpdateInvisible() {
        return updateAnyways;
    }

    public void setUpdateInvisible(boolean value) {
        updateAnyways = value;
    }

    public void remove(GameNode node) {
        synchronized (children) {
            children.removeValue(node, true);
        }
    }

    public Array<GameNode> getChildren() {
        synchronized (children) {
            return children;
        }
    }

    public int getChildCount() {
        return children.size;
    }

    public int getActiveChildCount() {
        return activeCount;
    }

    public Collection<? extends GameNode> byName(String name) {
        synchronized (children) {
            List<GameNode> list = new ArrayList<>();
            for (GameNode child : children) {
                if (child.name.equals(name)) list.add(child);
            }
            return list;
        }
    }

    public <T> Collection<T> byType(Class<T> type) {
        synchronized (children) {
            List<T> list = new ArrayList<>();
            for (GameNode child : children) {
                if (type.isInstance(child)) list.add(type.cast(child));
            }
            return list;
        }
    }

    public GameNode find(String name) {
        synchronized (children) {
            for (GameNode child : children) {
                if (child.name.equals(name)) return child;
            }
            return null;
        }
    }

    public GameNode find(Class<? extends GameNode> type) {
        synchronized (children) {
            for (GameNode child : children) {
                if (type.isInstance(child)) return type.cast(child);
            }
            return null;
        }
    }

    public void clearChildren() {
        synchronized (children) {
            children.forEach(GameNode::dispose);
            children.clear();
        }
    }

    public Iterable<? extends Component<?>> getComponents() {
        synchronized (components) {
            return components.values();
        }
    }

    public Iterable<? extends GameComponent> getGameComponents() {
        synchronized (components) {
            List<GameComponent> list = new ArrayList<>();
            for (Component<?> component : components.values()) {
                if (component instanceof GameComponent) {
                    GameComponent gameComponent = (GameComponent) component;
                    list.add(gameComponent);
                }
            }
            return list;
        }
    }

    public void clearComponents() {
        synchronized (components) {
            components.forEach((type, value) -> value.onRemoved(this));
            components.clear();
        }
    }

    public <T extends Component<?>> @Nullable T get(@NotNull Class<T> modelInstanceClass) {
        synchronized (components) {
            return (T) this.components.get(modelInstanceClass);
        }
    }

    public <T extends Component<?>> @Nullable T set(@NotNull Class<T> type, @Nullable T value) {
        synchronized (components) {
            if (value == null) {
                Component<?> removed = components.remove(type);
                removed.onRemoved(this);
                return null;
            }
            if (!components.containsKey(type)) {
                components.put(type, value);
            } else {
                Object old = components.replace(type, value);
                if (old instanceof Component<?>) {
                    Component<?> component = (Component<?>) old;
                    component.onRemoved(this);
                }
            }

            value.onAdded(this);
            return value;
        }
    }

    public <T extends Component<?>> T removeComponent(Class<T> type) {
        synchronized (components) {
            T component = type.cast(components.get(type));
            components.remove(type, component);
            return component;
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static int getManagedCount() {
        return MANAGED.size;
    }

    public static void disposeAll() {
        for (GameNode gameNode : MANAGED.toArray(GameNode.class)) {
            gameNode.dispose();
        }
    }
}
