package com.ultreon.quantum.client.particle;

import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderer;
import com.ultreon.quantum.client.ClientRegistries;
import com.ultreon.quantum.util.Identifier;
import org.lwjgl.opengl.GL20;

public class ParticleControllerRenderers {
    public static final BillboardRenderer DEFAULT = register("billboard_renderer", new BillboardRenderer(new BillboardParticleBatch(ParticleShader.AlignMode.ViewPoint, true, 2000)));
    public static final ParticleControllerRenderer<?, ?> GLOW = register("glow", new BillboardRenderer(new BillboardParticleBatch(ParticleShader.AlignMode.ViewPoint, true, 3000, new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA), new DepthTestAttribute(GL20.GL_LEQUAL, true))));

    private static <T extends ParticleControllerRenderer<?, ?>> T register(String name, T value) {
        ClientRegistries.PARTICLE_CONTROLLER_RENDERER.register(new Identifier(name), value);
        return value;
    }
    
    public static void init() {
        
    }
}
