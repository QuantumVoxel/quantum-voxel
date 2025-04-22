package dev.ultreon.quantum.util;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.component.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GameObject extends GameNode implements RenderableProvider, Disposable {
    private static final Matrix4 IDENTITY_MATRIX = new Matrix4();

    public final @NotNull Matrix4 transform = new Matrix4();

    protected @Nullable Object userData = null;

    @HiddenNode
    public final Matrix4 combined = new Matrix4();
    private @Nullable RendererComponent renderer = null;

    public Vector3 translation = new Vector3();
    public Vector3 rotation = new Vector3();
    public Vector3 scale = new Vector3(1, 1, 1);

    public void add(String name, @NotNull GameObject child) {
        super.add(name, child);
    }

    public void remove(@NotNull GameObject child) {
        super.remove(child);
    }

    public void add(String name, GameNode node) {
        if (!(node instanceof GameObject)) {
            throw new IllegalArgumentException("Game objects can only have game objects as children");
        }
        GameObject gameObject = (GameObject) node;
        add(name, gameObject);
    }

    @Override
    public void remove(GameNode node) {
        if (!(node instanceof GameObject)) {
            throw new IllegalArgumentException("Game objects can only have game objects as children");
        }
        GameObject gameObject = (GameObject) node;
        remove(gameObject);
    }

    public GameObject pop(@NotNull GameObject child) {
        synchronized (children) {
            children.removeValue(child, true);
            return child;
        }
    }

    @Override
    public <T extends Component<?>> @Nullable T set(@NotNull Class<T> type, @Nullable T value) {
        if (value instanceof RendererComponent) {
            RendererComponent rendererComponent = (RendererComponent) value;
            this.renderer = rendererComponent;
        }

        return super.set(type, value);
    }

    @Override
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
                    gameObject.combined.set(combined)
                            .mul(gameObject.transform)
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

    protected void onDisable() {

    }

    @Override
    public void getRenderables(@NotNull Array<Renderable> renderables, @NotNull Pool<Renderable> pool) {
        synchronized (children) {
            combined.set(parent instanceof GameObject ? ((GameObject) parent).combined : IDENTITY_MATRIX)
                    .mul(transform)
                    .translate(translation)
                    .rotate(Vector3.X, rotation.x)
                    .rotate(Vector3.Y, rotation.y)
                    .rotate(Vector3.Z, rotation.z)
                    .scale(scale.x, scale.y, scale.z);

            if (renderer != null) {
                renderer.getRenderables(renderables, pool, this);
            }

            for (GameNode child : children) {
                if (!child.enabled || !(child instanceof GameObject)) continue;
                GameObject gameObject = (GameObject) child;
                gameObject.getRenderables(renderables, pool);
            }

        }
    }

    public void clear() {
        synchronized (children) {
            for (GameNode child : children) child.dispose();
            children.clear();
        }

        synchronized (components) {
            components.clear();
        }

        synchronized (this) {
            userData = null;
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

    public Matrix4 getTransform() {
        return combined;
    }

    public String toString() {
        if (name != null) return name;
        return this.getClass().getSimpleName();
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
}
