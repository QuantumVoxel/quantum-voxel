package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.ultreon.quantum.client.model.QVModel;

public class DrawLayer {
    public static final DrawLayer BACKGROUND = new DrawLayer();
    public static final DrawLayer WORLD = new DrawLayer();

    private final Array<ModelInstance> inactiveObjects = new Array<>();
    private final Array<ModelInstance> activeObjects = new Array<>();
    private final Array<AnimationController> animationControllers = new Array<>();
    private final Matrix4 matrixTemp = new Matrix4();

    private DrawLayer() { }

    @Deprecated(forRemoval = true)
    public MeshBuilder meshBuilder() {
        return new MeshBuilder();
    }

    public ModelInstance create(Model model, Matrix4 transform) {
        ModelInstance modelInstance = new ModelInstance(model);
        modelInstance.transform.set(transform);

        this.activeObjects.add(modelInstance);
        return modelInstance;
    }

    public ModelInstance create(Model model, float x, float y, float z) {
        return create(model, this.matrixTemp.setToTranslation(x, y, z));
    }

    public ModelInstance create(Model model, Vector3 position) {
        return create(model, this.matrixTemp.setToTranslation(position));
    }

    public void add(ModelInstance model) {
        if (this.inactiveObjects.contains(model, true))
            this.inactiveObjects.removeValue(model, true);
        if (!this.activeObjects.contains(model, true))
            this.activeObjects.add(model);
    }

    public void add(AnimationController controller) {
        if (!this.animationControllers.contains(controller, true))
            this.animationControllers.add(controller);
    }

    public void add(QVModel model) {
        this.add(model.getInstance());
        this.add(model.getAnimationController());
    }

    public ModelInstance create(Model model) {
        return this.create(model, 0, 0, 0);
    }

    @CanIgnoreReturnValue
    public boolean destroy(ModelInstance instance) {
        boolean flag = this.inactiveObjects.removeValue(instance, true);
        return this.activeObjects.removeValue(instance, true) || flag;
    }

    public boolean destroy(AnimationController controller) {
        return this.animationControllers.removeValue(controller, true);
    }

    public boolean destroy(QVModel model) {
        boolean destroy = destroy(model.getAnimationController());
        return destroy(model.getInstance()) || destroy;
    }

    public void activate(ModelInstance instance) {
        if (this.inactiveObjects.contains(instance, true)) {
            this.inactiveObjects.removeValue(instance, true);
            this.activeObjects.add(instance);
        }
    }

    public void deactivate(ModelInstance instance) {
        if (this.activeObjects.contains(instance, true)) {
            this.activeObjects.removeValue(instance, true);
            this.inactiveObjects.add(instance);
        }
    }

    public void finish(Array<Renderable> output, Pool<Renderable> pool) {
        for (ModelInstance modelInstance : this.activeObjects) {
            modelInstance.getRenderables(output, pool);
        }
    }

    public void update(float delta) {
        for (AnimationController controller : this.animationControllers) {
            controller.update(delta);
        }
    }

    public void clear() {
        for (var obj : this.activeObjects) destroy(obj);
        for (var obj : this.inactiveObjects) destroy(obj);
    }

    public int getActiveCount() {
        return this.activeObjects.size;
    }

    public int getInactiveCount() {
        return this.inactiveObjects.size;
    }
}
