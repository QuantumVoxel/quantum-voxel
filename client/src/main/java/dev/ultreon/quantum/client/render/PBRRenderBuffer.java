package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class PBRRenderBuffer extends RenderBuffer {
    private final Environment forceEnvironment;

    PBRRenderBuffer(RenderPass pass, Environment forceEnvironment) {
        super(pass);
        this.forceEnvironment = forceEnvironment;
    }

    @Override
    public void render(Renderable instance) {
        instance.environment = forceEnvironment;
        if (!started) throw new IllegalStateException("RenderBuffer not started");
        instance.meshPart.primitiveType = primitiveType;
        instance.material = instanceMaterial;
        instance.shader = shader.getShader(instance);
        this.modelBatch.render(instance);
        this.currentRenderCount++;
    }

    @Override
    public Material getMaterial(RenderPass pass) {
        return pass.createPBRMaterial();
    }

    @Override
    public ShaderProvider getShader(RenderPass pass) {
        return pass.createPBRShader();
    }
}
