package dev.ultreon.quantum.client.model.entity.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.model.EntityModelInstance;
import dev.ultreon.quantum.client.model.QVModel;
import dev.ultreon.quantum.client.model.WorldRenderContext;
import dev.ultreon.quantum.client.render.EntityTextures;
import dev.ultreon.quantum.client.render.RenderType;
import dev.ultreon.quantum.client.render.context.ObjectType;
import dev.ultreon.quantum.client.render.context.RenderMaterial;
import dev.ultreon.quantum.client.render.material.EntityMaterial;
import dev.ultreon.quantum.client.render.pass.RenderPass;
import dev.ultreon.quantum.client.render.world.RenderMaterials;
import dev.ultreon.quantum.client.shaders.ShaderProviders;
import dev.ultreon.quantum.entity.Entity;
import dev.ultreon.quantum.util.DVec3;
import dev.ultreon.quantum.util.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class EntityRenderer<E extends Entity> implements Disposable {
    protected static DVec3 tmp0 = new DVec3();
    protected static DVec3 tmp1 = new DVec3();
    protected static DVec3 tmp2 = new DVec3();
    protected static Vec3 tmp0f = new Vec3();
    protected static Vec3 tmp1f = new Vec3();
    protected static Vec3 tmp2f = new Vec3();

    protected QuantumClient client = QuantumClient.get();
    protected Matrix4 tmp = new Matrix4();

    protected EntityRenderer() {

    }

    public void render(RenderPass pass, EntityModelInstance<E> instance, WorldRenderContext<E> context) {
        if (instance.getModel() == null)
            throw new IllegalStateException("Cannot render entity " + instance.getEntity().getType().getId() + " without model");

        if (instance.getModel().getInstance().nodes.size == 0)
            throw new IllegalStateException("Cannot render entity " + instance.getEntity().getType().getId() + " without nodes");

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        instance.getModel().getInstance().materials.forEach(m -> {
            m.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            m.set(new DepthTestAttribute(GL20.GL_LEQUAL, true));
            m.set(IntAttribute.createCullFace(GL20.GL_BACK));
            m.set(FloatAttribute.createAlphaTest(0.01f));
        });
        if (instance.getModel().getInstance().userData == null)
            instance.getModel().getInstance().userData = ShaderProviders.MODEL_VIEW.get();
        instance.translate(0, -1.625, 0);
        instance.render(context, context.getBufferSource().getBuffer(pass.renderTypeFor(getRenderMaterial())));
    }

    private RenderMaterial getRenderMaterial() {
        return RenderMaterials.ENTITY;
    }

    public abstract void animate(EntityModelInstance<E> instance, WorldRenderContext<E> context);

    @Nullable
    public abstract QVModel createModel(E entity);

    public abstract EntityTextures getTextures();

    public void dispose() {
        
    }
}
