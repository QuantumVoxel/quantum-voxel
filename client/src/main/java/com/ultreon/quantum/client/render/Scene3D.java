package com.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public class Scene3D {
    public static final Scene3D BACKGROUND = new Scene3D();
    public static final Scene3D WORLD = new Scene3D();

    private final Array<ModelInstance> inactiveObjects = new Array<>();
    private final Array<ModelInstance> activeObjects = new Array<>();
    private final Matrix4 matrixTemp = new Matrix4();

    private Scene3D() { }

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
        return create(model, this.matrixTemp.translate(x, y, z));
    }

    public ModelInstance create(Model model, Vector3 position) {
        return create(model, this.matrixTemp.translate(position));
    }

    public void add(ModelInstance model) {
        if (this.inactiveObjects.contains(model, true))
            this.inactiveObjects.removeValue(model, true);
        if (!this.activeObjects.contains(model, true))
            this.activeObjects.add(model);
    }

    public ModelInstance create(Model model) {
        return this.create(model, 0, 0, 0);
    }

    @CanIgnoreReturnValue
    public boolean destroy(ModelInstance instance) {
        boolean flag = this.inactiveObjects.removeValue(instance, true);
        return this.activeObjects.removeValue(instance, true) || flag;
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
}
