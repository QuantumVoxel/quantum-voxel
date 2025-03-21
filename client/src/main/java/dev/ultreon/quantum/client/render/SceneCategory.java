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

/**
 * The SceneCategory class represents a category of scenes in the game.
 * It extends the GameObject class and implements the RenderableProvider interface.
 * <p>
 * This class is used to manage the scenes in the game.
 * </p>
 * 
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public class SceneCategory extends GameObject implements RenderableProvider {
    private final Matrix4 matrixTemp = new Matrix4();
    private final QuantumClient client = QuantumClient.get();

    /**
     * Constructs a new SceneCategory object.
     */
    @ApiStatus.Internal
    public SceneCategory() {

    }

    /**
     * Gets the background scene category.
     * 
     * @return The background scene category.
     */
    public static SceneCategory getBackground() {
        return QuantumClient.get().backgroundCat;
    }

    /**
     * Gets the world scene category.
     * 
     * @return The world scene category.
     */
    public static SceneCategory getWorld() {
        return QuantumClient.get().worldCat;
    }

    /**
     * Creates a new game object with a standalone renderer.
     * 
     * @param model The model to render.
     * @param transform The transform of the game object.
     * @return The created game object.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public GameObject create(Model model, Matrix4 transform) {
        GameObject gameObject = new UnknownGameObject();
        gameObject.set(StandaloneRenderer.class, new StandaloneRenderer(model, Shaders.MODEL_VIEW.get()));
        this.add(gameObject);

        gameObject.transform.set(transform);

        return gameObject;
    }

    /**
     * Creates a new game object with a standalone renderer.
     * 
     * @param model The model to render.
     * @return The created game object.
     */
    @Deprecated
    public GameObject create(Model model, Vector3 position) {
        return create(model, this.matrixTemp.setToTranslation(position));
    }

    /**
     * Adds a model instance to the scene category.
     * 
     * @param model The model instance to add.
     */
    @Deprecated
    public void add(ModelInstance model) {
        GameObject gameObject = new UnknownGameObject();
        gameObject.set(InstanceRenderer.class, new InstanceRenderer(model, Shaders.MODEL_VIEW.get()));
        this.add(gameObject);
    }

    /**
     * Adds an animation controller to the scene category.
     * 
     * @param controller The animation controller to add.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public void add(AnimationController controller) {
        GameObject gameObject = new UnknownGameObject();
        gameObject.set(Animator.class, new Animator(controller));
        this.add(gameObject);
    }

    /**
     * Adds a QVModel to the scene category.
     * 
     * @param model The QVModel to add.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public void add(QVModel model) {
        this.add(model.getInstance());
        this.add(model.getAnimationController());
    }

    /**
     * Creates a new game object with a standalone renderer.
     * 
     * @param model The model to render.
     * @return The created game object.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public GameObject create(Model model) {
        return this.create(model, new Matrix4());
    }

    /**
     * Destroys a model instance.
     * 
     * @param instance The model instance to destroy.
     * @return True if the model instance was destroyed, false otherwise.
     * @deprecated This method is deprecated.
     */
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

    /**
     * Destroys an animation controller.
     * 
     * @param controller The animation controller to destroy.
     * @return True if the animation controller was destroyed, false otherwise.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public boolean destroy(AnimationController controller) {
        boolean destroyed = false;
        for (GameObject child : getChildren().select(child -> child.has(AnimationController.class))) {
            child.dispose();
            destroyed = true;
        }

        return destroyed;
    }

    /**
     * Destroys a QVModel.
     * 
     * @param model The QVModel to destroy.
     * @return True if the QVModel was destroyed, false otherwise.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public boolean destroy(QVModel model) {
        boolean destroy = destroy(model.getAnimationController());
        return destroy(model.getInstance()) || destroy;
    }

    /**
     * Activates a model instance.
     * 
     * @param instance The model instance to activate.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public void activate(ModelInstance instance) {
        for (GameObject child : getChildren().select(child -> child.has(ModelInstance.class))) {
            child.setVisible(true);
        }
    }

    /**
     * Deactivates a model instance.
     * 
     * @param instance The model instance to deactivate.
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public void deactivate(ModelInstance instance) {
        for (GameObject child : getChildren().select(child -> child.has(ModelInstance.class))) {
            child.setVisible(false);
        }
    }

    /**
     * Updates the scene category.
     * 
     * @param delta The delta time.
     */
    public void update(float delta) {
        for (GameObject child : getChildren().select(child -> child.has(AnimationController.class))) {
            child.update(delta);
        }
    }
}
