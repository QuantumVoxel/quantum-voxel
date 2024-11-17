package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.world.StandaloneRenderer;
import dev.ultreon.quantum.util.Animator;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.InstanceRenderer;
import dev.ultreon.quantum.util.RendererComponent;
import org.jetbrains.annotations.ApiStatus;

public class SceneCategory extends GameObject implements RenderableProvider {
    private final Matrix4 matrixTemp = new Matrix4();
    private final QuantumClient client = QuantumClient.get();

    @ApiStatus.Internal
    public SceneCategory() {

    }

    public static SceneCategory getBackground() {
        return QuantumClient.get().backgroundCat;
    }

    public static SceneCategory getWorld() {
        return QuantumClient.get().worldCat;
    }

    @Deprecated
    public GameObject create(Model model, Matrix4 transform) {
        GameObject gameObject = new UnknownGameObject();
        gameObject.set(StandaloneRenderer.class, new StandaloneRenderer(model, Shaders.MODEL_VIEW.get()));
        this.add(gameObject);

        gameObject.transform.set(transform);

        return gameObject;
    }

    @Deprecated
    public GameObject create(Model model, float x, float y, float z) {
        return create(model, this.matrixTemp.setToTranslation(x, y, z));
    }

    @Deprecated
    public GameObject create(Model model, Vector3 position) {
        return create(model, this.matrixTemp.setToTranslation(position));
    }

    @Deprecated
    public void add(ModelInstance model) {
        GameObject gameObject = new UnknownGameObject();
        gameObject.set(InstanceRenderer.class, new InstanceRenderer(model, Shaders.MODEL_VIEW.get()));
        this.add(gameObject);
    }

    @Deprecated
    public void add(AnimationController controller) {
        GameObject gameObject = new UnknownGameObject();
        gameObject.set(Animator.class, new Animator(controller));
        this.add(gameObject);
    }

    @Deprecated
    public void add(QVModel model) {
        this.add(model.getInstance());
        this.add(model.getAnimationController());
    }

    @Deprecated
    public GameObject create(Model model) {
        return this.create(model, 0, 0, 0);
    }

    @Deprecated
    @CanIgnoreReturnValue
    public boolean destroy(ModelInstance instance) {
        boolean destroyed = false;
        for (GameObject child : getChildren().select(child -> child.has(UnknownGameObject.class))) {
            RendererComponent renderer = child.getRenderer();
            if (renderer != null && renderer.getInstance() == instance) {
                child.dispose();
                destroyed = true;
            }
        }

        return destroyed;
    }

    @Deprecated
    public boolean destroy(AnimationController controller) {
        boolean destroyed = false;
        for (GameObject child : getChildren().select(child -> child.has(AnimationController.class))) {
            child.dispose();
            destroyed = true;
        }

        return destroyed;
    }

    @Deprecated
    public boolean destroy(QVModel model) {
        boolean destroy = destroy(model.getAnimationController());
        return destroy(model.getInstance()) || destroy;
    }

    @Deprecated
    public void activate(ModelInstance instance) {
        for (GameObject child : getChildren().select(child -> child.has(ModelInstance.class))) {
            child.setVisible(true);
        }
    }

    @Deprecated
    public void deactivate(ModelInstance instance) {
        for (GameObject child : getChildren().select(child -> child.has(ModelInstance.class))) {
            child.setVisible(false);
        }
    }

    public void update(float delta) {
        for (GameObject child : getChildren().select(child -> child.has(AnimationController.class))) {
            child.update(delta);
        }
    }
}
