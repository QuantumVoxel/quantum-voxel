package dev.ultreon.quantum.client.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.atlas.TextureAtlas;
import dev.ultreon.quantum.client.gui.Bounds;
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.util.Suppliers;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import org.jetbrains.annotations.ApiStatus;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.function.Supplier;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.VertexAttribute.*;

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
public class RenderPass {
    private static final Array<RenderPass> MANAGED = new Array<>();

    private final String name;
    private final Supplier<? extends ShaderProvider> shader;
    private final Supplier<Material> material;
    private final Supplier<Material> instanceMaterial;
    private final VertexAttribute[] attributes;
    private final int mode;

    private RenderPass(Builder builder) {
        this.name = builder.name;
        this.shader = builder.shader;
        this.material = builder.material;
        this.instanceMaterial = builder.instanceMaterial;
        this.attributes = builder.attributes;
        this.mode = builder.mode;
        MANAGED.add(this);
    }

    public String name() {
        return name;
    }

    public int mode() {
        return mode;
    }

    public Supplier<? extends ShaderProvider> shader() {
        return shader;
    }

    public Supplier<Material> material() {
        return material;
    }

    public static Builder builder(VertexAttribute... attributes) {
        return new Builder(attributes);
    }

    public VertexAttributes attributes() {
        return new VertexAttributes(attributes);
    }

    public static class Builder {
        private final VertexAttribute[] attributes;
        private int mode = GL_TRIANGLES;
        private String name;
        private Supplier<? extends ShaderProvider> shader;
        private Supplier<Material> material;
        private Supplier<Material> instanceMaterial;
        private final Array<AttributeDelegate> attributeDelegates = new Array<>();
        private final Array<AttributeDelegate> instanceAttributeDelegates = new Array<>();
        private final Color backgroundColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);

        public Builder(VertexAttribute... attributes) {
            this.attributes = attributes;
        }

        public Builder mode(int mode) {
            this.mode = mode;
            return this;
        }

        public Builder name(String passName) {
            name = passName;
            return this;
        }

        public Builder shader(Supplier<? extends ShaderProvider> shaderSupplier) {
            shader = shaderSupplier;
            return this;
        }

        @Deprecated
        public Builder material(Supplier<Material> materialSupplier) {
            material = materialSupplier;
            return this;
        }

        public Builder depthTest() {
            this.instanceAttributeDelegates.add(new DepthTest(GL_LEQUAL, 0, 1, true));
            return this;
        }

        public Builder depthTest(int func) {
            this.instanceAttributeDelegates.add(new DepthTest(func, 0, 1, true));
            return this;
        }

        public Builder depthTest(boolean depthMask) {
            this.instanceAttributeDelegates.add(new DepthTest(GL_LEQUAL, 0, 1, depthMask));
            return this;
        }

        public Builder depthTest(int func, float rangeNear, float rangeFar) {
            this.instanceAttributeDelegates.add(new DepthTest(func, rangeNear, rangeFar, true));
            return this;
        }

        public Builder depthTest(int func, float rangeNear, float rangeFar, boolean depthMask) {
            this.instanceAttributeDelegates.add(new DepthTest(func, rangeNear, rangeFar, depthMask));
            return this;
        }

        public Builder depthTest(float rangeNear, float rangeFar) {
            this.instanceAttributeDelegates.add(new DepthTest(GL_LEQUAL, rangeNear, rangeFar, true));
            return this;
        }

        public Builder blending() {
            this.instanceAttributeDelegates.add(new Blending(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
            return this;
        }

        public Builder blending(int src, int dst) {
            this.attributeDelegates.add(new Blending(src, dst));
            return this;
        }

        public Builder atlas(NamespaceID textureId) {
            this.instanceAttributeDelegates.add(new Atlas(textureId, TextureAtlas.TextureAtlasType.DIFFUSE));
            return this;
        }

        public Builder atlas(NamespaceID textureId, TextureAtlas.TextureAtlasType type) {
            this.instanceAttributeDelegates.add(new Atlas(textureId, type));
            return this;
        }

        public Builder texture(NamespaceID textureId) {
            this.instanceAttributeDelegates.add(new Texture(textureId));
            return this;
        }

        public Builder textureRegion(NamespaceID atlasId, NamespaceID textureId) {
            this.instanceAttributeDelegates.add(new TextureRegion(atlasId, textureId, TextureAtlas.TextureAtlasType.DIFFUSE));
            return this;
        }

        public Builder textureRegion(NamespaceID atlasId, NamespaceID textureId, TextureAtlas.TextureAtlasType type) {
            this.instanceAttributeDelegates.add(new TextureRegion(atlasId, textureId, type));
            return this;
        }

        public Builder alphaTest() {
            this.instanceAttributeDelegates.add(new AlphaTest(0.5f));
            return this;
        }

        public Builder alphaTest(float threshold) {
            this.instanceAttributeDelegates.add(new AlphaTest(threshold));
            return this;
        }

        public Builder cull(int cullFace) {
            this.instanceAttributeDelegates.add(new CullFace(cullFace));
            return this;
        }

        public Builder fogColor(Color color) {
            this.attributeDelegates.add(new FogColor(color));
            return this;
        }

        public Builder fog(float density, float start, float end) {
            this.attributeDelegates.add(new Fog(density, start, end));
            return this;
        }

        public Builder ambientColor(Color color) {
            this.attributeDelegates.add(new AmbientColor(color));
            return this;
        }

        public Builder backgroundColor(Color color) {
            this.backgroundColor.set(color);
            return this;
        }

        public RenderPass build() {
            material = Suppliers.memoize(() -> {
                Material material = new Material();
                attributeDelegates.forEach(delegate -> delegate.apply(material));
                return material;
            });
            instanceMaterial = Suppliers.memoize(() -> {
                Material material = new Material();
                instanceAttributeDelegates.forEach(delegate -> delegate.apply(material));
                return material;
            });
            return new RenderPass(this);
        }

        public record CullFace(int mode) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(IntAttribute.createCullFace(mode));
            }
        }

        public interface AttributeDelegate {
            void apply(Material material);
        }

        public record AlphaTest(float threshold) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(FloatAttribute.createAlphaTest(threshold));
            }
        }

        public record DepthTest(int func, float rangeNear, float rangeFar, boolean depthMask) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(new DepthTestAttribute(func, rangeNear, rangeFar, depthMask));
            }
        }

        public record Blending(int src, int dst) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(new BlendingAttribute(src, dst));
            }
        }

        public record Atlas(NamespaceID textureId, TextureAtlas.TextureAtlasType type) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                QuantumClient client = QuantumClient.get();
                TextureAtlas atlas = client.getAtlas(textureId);
                if (atlas == null) throw new IllegalStateException("Texture atlas " + textureId + " not found");
                atlas.apply(material, type);
            }
        }

        public record Texture(NamespaceID textureId) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                QuantumClient client = QuantumClient.get();
                TextureManager textureManager = client.getTextureManager();
                var texture = textureManager.getTexture(textureId);
                material.set(TextureAttribute.createDiffuse(texture));
            }
        }

        public record TextureRegion(NamespaceID atlasId, NamespaceID textureId, TextureAtlas.TextureAtlasType type) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                QuantumClient client = QuantumClient.get();
                TextureAtlas atlas = client.getAtlas(atlasId);
                if (atlas == null) throw new IllegalStateException("Texture atlas " + atlasId + " not found");
                var texture = atlas.get(textureId, type);
                if (texture == null) {
                    material.set(TextureAttribute.createDiffuse(TextureManager.DEFAULT_TEX_REG));
                    return;
                }
                material.set(TextureAttribute.createDiffuse(texture));
            }
        }

        public record FogColor(Color color) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(ColorAttribute.createFog(color));
            }
        }

        public record Fog(float density, float start, float end) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(FogAttribute.createFog(start, end, density));
            }
        }

        public record AmbientColor(Color color) implements AttributeDelegate {
            @Override
            public void apply(Material material) {
                material.set(ColorAttribute.createAmbient(color));
            }
        }
    }

    public static final RenderPass SKYBOX = RenderPass.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
            .name("skybox")
            .shader(Shaders.SKYBOX)
            .depthTest(false)
            .build();

    public static final RenderPass CELESTIAL_BODIES = RenderPass.builder(Position(), TexCoords(0))
            .name("celestial_bodies")
            .shader(Shaders.SKYBOX)
            .blending(GL_SRC_ALPHA, GL_ONE)
            .cull(0)
            .depthTest(false)
            .build();

    public static final RenderPass OPAQUE = RenderPass.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
            .name("opaque")
            .shader(Shaders.WORLD)
            .depthTest()
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .build();

    public static final RenderPass TRANSPARENT = RenderPass.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
            .name("transparent")
            .shader(Shaders.WORLD)
            .blending()
            .depthTest()
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .build();

    public static final RenderPass GIZMO = RenderPass.builder(Position(), ColorPacked(), TexCoords(0))
            .name("gizmo")
            .shader(Shaders.GIZMO)
            .alphaTest(0.01f)
            .blending()
            .depthTest()
            .build();

    public static final RenderPass GIZMO_OUTLINE = RenderPass.builder(Position(), ColorPacked(), TexCoords(0))
            .name("gizmo_outline")
            .shader(Shaders.GIZMO_OUTLINE)
            .alphaTest(0.01f)
            .blending()
            .depthTest()
            .build();

    public static final RenderPass ENTITY_TRANSPARENT = RenderPass.builder(Position(), Normal(), TexCoords(0))
            .name("entity_transparent")
            .shader(Shaders.WORLD)
            .blending()
            .depthTest()
            .build();

    public static final RenderPass WATER = RenderPass.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
            .name("water")
            .shader(Shaders.WORLD)
            .blending()
            .depthTest()
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .build();

    public static final RenderPass CUTOUT = RenderPass.builder(Position(), Normal(), ColorPacked(), TexCoords(0))
            .name("cutout")
            .shader(Shaders.WORLD)
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .alphaTest()
            .depthTest()
            .build();

    /**
     * Registers a new render effect.
     *
     * @param renderType The render effect to register.
     * @return The registered render effect.
     */
    public static RenderPass register(RenderPass renderType) {
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


    public Material createInstanceMaterial() {
        return instanceMaterial.get();
    }
}

