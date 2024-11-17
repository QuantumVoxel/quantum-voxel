package dev.ultreon.quantum.util;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import dev.ultreon.quantum.component.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class GameObject implements RenderableProvider, Disposable {
    private static final Matrix4 IDENTITY_MATRIX = new Matrix4();
    private final @NotNull ClassToInstanceMap<Component<?>> components = MutableClassToInstanceMap.create();
    private final @NotNull Array<GameObject> children = new Array<>();

    public final @NotNull Matrix4 transform = new Matrix4();

    protected @Nullable Object userData = null;

    private boolean isVisible = true;
    private boolean updateInvisible = false;
    private @Nullable GameObject parent;
    private int activeCount;
    private final Matrix4 finalTransform = new Matrix4();
    private @Nullable RendererComponent renderer = null;

    private String name = null;

    @Deprecated
    public void add(@NotNull GameObject child) {
        add(null, child);
    }

    public void add(String name, @NotNull GameObject child) {
        synchronized (children) {
            child.name = name;
            children.add(child);
            child.create();
            child.parent = this;
        }
    }

    public void add(@NotNull GameObject child, @NotNull GameObject... children) {
        add(null, child, children);
    }

    public void add(String name, @NotNull GameObject child, @NotNull GameObject... children) {
        synchronized (this.children) {
            child.name = name;
            this.children.add(child);
            this.children.addAll(children);
            child.create();
            child.parent = this;
        }
    }

    private void create() {
        onCreate();
    }

    protected void onCreate() {

    }

    public void remove(@NotNull GameObject child) {
        synchronized (children) {
            if (child.parent != this) return;
            children.removeValue(child, true);
            child.dispose();
            child.parent = null;
        }
    }

    public GameObject pop(@NotNull GameObject child) {
        synchronized (children) {
            children.removeValue(child, true);
            return child;
        }
    }

    public @NotNull Array<GameObject> getChildren() {
        return children;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void dispose() {
        this.parent = null;

        synchronized (children) {
            this.onDispose();
            for (GameObject child : children) {
                child.dispose();
            }
        }

        synchronized (components) {
            components.forEach((type, value) -> value.onRemoved(this));
            components.clear();
        }
    }

    protected void onDispose() {

    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        setVisible(visible, false);
    }

    public void setVisible(boolean visible, boolean force) {
        if (isVisible == visible) return;
        isVisible = visible;

        if (isVisible) {
            this.onEnable();
            if (parent != null && force) parent.setVisible(true, true);
            if (parent != null) parent.activeCount++;
        } else {
            this.onDisable();
            if (parent != null) parent.activeCount--;
        }
    }

    public boolean isUpdateInvisible() {
        return updateInvisible;
    }

    public void setUpdateInvisible(boolean updateInvisible) {
        this.updateInvisible = updateInvisible;
    }

    protected void onEnable() {

    }

    protected void onDisable() {

    }

    @Override
    public void getRenderables(@NotNull Array<Renderable> renderables, @NotNull Pool<Renderable> pool) {
        synchronized (children) {
            for (GameObject child : children) {
                if (!child.isVisible) continue;
                child.finalTransform.set(child.transform).mul(parent == null ? IDENTITY_MATRIX : parent.finalTransform);
                child.getRenderables(renderables, pool);

                if (child.renderer != null) {
                    child.renderer.getRenderables(renderables, pool, child);
                }
            }
        }
    }

    public void clear() {
        synchronized (children) {
            for (GameObject child : children) child.dispose();
            children.clear();
        }

        synchronized (components) {
            components.clear();
        }

        synchronized (this) {
            userData = null;
        }
    }

    public <T extends Component<?>> T set(@NotNull Class<T> type, @Nullable T value) {
        synchronized (components) {
            if (value instanceof RendererComponent rendererComponent) {
                this.renderer = rendererComponent;
            }
            if (value == null) {
                Component<?> removed = components.remove(type);
                removed.onRemoved(this);
                return null;
            }
            if (!components.containsKey(type)) {
                components.putInstance(type, value);
            } else {
                Object old = components.replace(type, value);
                if (old instanceof Component<?> component)
                    component.onRemoved(this);
            }

            value.onAdded(this);
            return value;
        }
    }

    public <T extends Component<?>> T get(@NotNull Class<T> modelInstanceClass) {
        synchronized (components) {
            return this.components.getInstance(modelInstanceClass);
        }
    }

    public <T> @Nullable T getUserData(@NotNull Class<T> type) {
        synchronized (this) {
            if (userData == null) return null;
            if (!type.isInstance(userData)) return null;
            return type.cast(userData);
        }
    }

    public void setUserData(@Nullable Object userData) {
        synchronized (this) {
            this.userData = userData;
        }
    }

    public <T> boolean has(@NotNull Class<T> type) {
        synchronized (components) {
            return components.containsKey(type);
        }
    }

    public void update(float delta) {
        synchronized (children) {
            for (GameObject child : children) {
                if (!child.isVisible && !child.updateInvisible) continue;
                child.update(delta);
            }
        }
    }

    public String toString() {
        if (name != null) return name;
        return this.getClass().getSimpleName();
    }

    public void clearComponents() {
        synchronized (components) {
            components.forEach((type, value) -> value.onRemoved(this));
            components.clear();
        }
    }

    public int getActiveCount() {
        return activeCount;
    }

    public ShaderProvider getShaderProvider() {
        if (renderer == null) return null;
        return renderer.getShaderProvider();
    }

    public void setShaderProvider(@Nullable ShaderProvider provider) {
        this.renderer.setShaderProvider(provider);
    }

    @Nullable
    public RendererComponent getRenderer() {
        return renderer;
    }

    public @Nullable String getDescription() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Component<?>> getComponents() {
        synchronized (components) {
            return components.values();
        }
    }
}
