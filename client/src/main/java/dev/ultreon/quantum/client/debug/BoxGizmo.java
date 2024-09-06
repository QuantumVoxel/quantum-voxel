package dev.ultreon.quantum.client.debug;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import kotlin.Lazy;
import kotlin.LazyKt;

public class BoxGizmo extends Gizmo {
    private static final Lazy<Model> model = LazyKt.lazy(BoxGizmo::createModel);
    private static final Lazy<Model> outlineModel = LazyKt.lazy(BoxGizmo::createOutlineModel);

    public BoxGizmo(String category) {
        super(category);
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
        return new ModelInstance(outline ? outlineModel.getValue() : model.getValue(), new Matrix4());
    }
}
