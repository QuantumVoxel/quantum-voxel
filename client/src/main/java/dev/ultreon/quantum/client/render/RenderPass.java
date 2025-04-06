package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.util.Utils;
import org.jetbrains.annotations.ApiStatus;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.function.Supplier;

import static com.badlogic.gdx.graphics.GL20.*;

/**
 * The RenderEffect class represents different visual effects that can be applied during rendering.
 * It is used to register and manage different rendering effects.
 * <p>
 * This class provides a static method to register a new render effect and a method to get the shader for a renderable.
 * </p>
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public record RenderPass(String name, Supplier<? extends ShaderProvider> shader, Supplier<Material> material) {
    private static final Array<RenderPass> MANAGED = new Array<>();

    public static final RenderPass SKYBOX = RenderPass.register(new RenderPass("skybox", Shaders.SKYBOX, () -> Utils.make(new Material(), material -> {
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass CELESTIAL_BODIES = RenderPass.register(new RenderPass("celestial_bodies", Shaders.SKYBOX, () -> Utils.make(new Material(), material -> {
        material.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass OPAQUE = RenderPass.register(new RenderPass("opaque", Shaders.SCENE, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass TRANSPARENT = RenderPass.register(new RenderPass("transparent", Shaders.SCENE, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(FloatAttribute.createAlphaTest(0.01f));
        material.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass GIZMO = RenderPass.register(new RenderPass("gizmo", Shaders.GIZMO, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(FloatAttribute.createAlphaTest(0.01f));
        material.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass GIZMO_OUTLINE = RenderPass.register(new RenderPass("gizmo_outline", Shaders.GIZMO_OUTLINE, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(FloatAttribute.createAlphaTest(0.01f));
        material.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass ENTITY_TRANSPARENT = RenderPass.register(new RenderPass("entity_transparent", Shaders.SCENE, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(FloatAttribute.createAlphaTest(0.01f));
        material.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass WATER = RenderPass.register(new RenderPass("water", Shaders.SCENE, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(FloatAttribute.createAlphaTest(0.01f));
        material.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));
    public static final RenderPass CUTOUT = RenderPass.register(new RenderPass("cutout", Shaders.SCENE, () -> Utils.make(new Material(), material -> {
        QuantumClient client = QuantumClient.get();
        material.set(TextureAttribute.createDiffuse(client.blocksTextureAtlas.getTexture()));
        material.set(TextureAttribute.createEmissive(client.blocksTextureAtlas.getEmissiveTexture()));
        material.set(FloatAttribute.createAlphaTest(0.5f));
        material.set(new DepthTestAttribute(GL_LEQUAL));
    })));

    public RenderPass(String name, Supplier<? extends ShaderProvider> shader, Supplier<Material> material) {
        this.name = name;
        this.shader = shader;
        this.material = material;

        MANAGED.add(this);
    }

    /**
     * Registers a new render effect.
     *
     * @param renderType The render effect to register.
     * @return The registered render effect.
     */
    private static RenderPass register(RenderPass renderType) {
        ClientRegistries.RENDER_EFFECT.register(new NamespaceID(CommonConstants.NAMESPACE, renderType.name), renderType);
        return renderType;
    }

    public static RenderPass[] values() {
        return MANAGED.toArray(RenderPass.class);
    }

    public static RenderPass byName(String renderPass) {
        RenderPass pass = ClientRegistries.RENDER_EFFECT.get(new NamespaceID(CommonConstants.NAMESPACE, renderPass));
        if (pass == null) throw new IllegalArgumentException("Unknown render pass: " + renderPass);
        return pass;
    }

    public static void nopInit() {
        // Load class
    }

    /**
     * Gets the shader for the render effect.
     *
     * @param renderable The renderable to get the shader for.
     * @return The shader for the render effect.
     */
    @ApiStatus.Experimental
    public Shader getShader(Renderable renderable) {
        return new DefaultShader(renderable);
    }

    public ShaderProvider createShader() {
        return shader.get();
    }

    public Material createMaterial() {
        return material.get();
    }
}

