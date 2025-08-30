package dev.ultreon.quantum.client.debug;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import dev.ultreon.quantum.client.world.LazyInitializer;
import dev.ultreon.quantum.util.GameObject;

public class BoxGizmo extends Gizmo {
    private static final LazyInitializer<Model> model = new LazyInitializer<>(BoxGizmo::createModel);
    private static final LazyInitializer<Model> outlineModel = new LazyInitializer<>(BoxGizmo::createOutlineModel);

    public BoxGizmo(GameObject attach, String name, String category) {
        super(category);

        attach.add("Gizmo '" + name + "'", this);
    }

    private static Model createModel() {
        ModelBuilder builder = new ModelBuilder();
        return builder.createBox(1, 1, 1, GL20.GL_TRIANGLES, MATERIAL, VertexAttributes.Usage.Position);
    }

    private static Model createOutlineModel() {
        ModelBuilder builder = new ModelBuilder();
        return builder.createBox(1, 1, 1, GL20.GL_LINES, MATERIAL, VertexAttributes.Usage.Position);
    }

    @Override
    protected ModelInstance createInstance() {
        return new ModelInstance(outline ? outlineModel.get() : model.get(), new Matrix4());
    }
}
