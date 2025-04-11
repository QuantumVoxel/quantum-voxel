package dev.ultreon.quantum.client.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.render.RenderPass;
import dev.ultreon.quantum.client.render.RenderPassRenderComponent;
import dev.ultreon.quantum.util.GameObject;
import dev.ultreon.quantum.util.Vec3d;
import org.jetbrains.annotations.NotNull;

public abstract class Gizmo extends GameObject implements RenderableProvider {
    protected static final Material MATERIAL = new Material("gizmo_material");

    static {
        MATERIAL.set(ColorAttribute.createDiffuse(1.0F, 1.0F, 1.0F, 1.0F));
        MATERIAL.set(ColorAttribute.createEmissive(1.0F, 1.0F, 1.0F, 1.0F));
    }
    
    private final QuantumClient client = QuantumClient.get();
    public final String category;
    public ModelInstance instance;
    public final Vec3d position = new Vec3d();
    public final Vector3 size = new Vector3();
    public final Color color = new Color(1.0F, 1.0F, 1.0F, 1.0F);
    public boolean outline = false;
    private final Vector3 rawPos = new Vector3();

    protected Gizmo(String category) {
        this.category = category;
    }

    @Override
    public void getRenderables(@NotNull Array<Renderable> renderables, @NotNull Pool<Renderable> pool) {
        client.camera.relative(position.cpy(), rawPos);
        if (instance == null) {
            instance = createInstance();
            this.set(RenderPassRenderComponent.class, new RenderPassRenderComponent(instance, outline ? RenderPass.GIZMO_OUTLINE : RenderPass.GIZMO));
        }
        instance.userData = this;
        instance.transform.setToTranslationAndScaling(rawPos, size);

        instance.getRenderables(renderables, pool);
    }

    protected abstract ModelInstance createInstance();

    @Override
    public boolean isVisible() {
        return client.world.isGimzoCategoryEnabled(category);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
