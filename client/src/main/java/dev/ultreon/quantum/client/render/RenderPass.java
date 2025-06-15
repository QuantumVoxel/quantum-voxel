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
import dev.ultreon.quantum.client.management.TextureAtlasManager;
import dev.ultreon.quantum.client.shaders.Shaders;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.util.Suppliers;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import org.jetbrains.annotations.ApiStatus;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.ClientRegistries;
import dev.ultreon.quantum.util.NamespaceID;

import java.util.Objects;
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
    private final Supplier<? extends PBRShaderProvider> pbrShader;
    private final Supplier<? extends ShaderProvider> shader;
    private final Supplier<Material> material;
    private final Supplier<Material> pbrMaterial;
    private final Supplier<Material> instanceMaterial;
    private final VertexAttribute[] attributes;
    private final int mode;

    private RenderPass(Builder builder) {
        this.name = builder.name;
        this.shader = builder.shader;
        this.pbrShader = builder.pbrShader;
        this.material = builder.material;
        this.pbrMaterial = builder.pbrMaterial;
        this.instanceMaterial = builder.instanceMaterial;
        this.attributes = builder.attributes;
        this.mode = builder.mode;
        MANAGED.add(this);
    }

    public PBRShaderProvider createPBRShader() {
        return pbrShader.get();
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

    public boolean doesMerging() {
        return true;
    }

    public static class Builder {
        private final VertexAttribute[] attributes;
        private int mode = GL_TRIANGLES;
        private String name;
        private Supplier<? extends ShaderProvider> shader;
        private Supplier<? extends PBRShaderProvider> pbrShader = () -> new PBRShaderProvider(null);
        private Supplier<Material> material;
        private Supplier<Material> instanceMaterial;
        private Supplier<Material> pbrMaterial = () -> {
            TextureAtlas blocksTextureAtlas = QuantumClient.get().blocksTextureAtlas;
            Material mat = new Material();
            mat.set(PBRTextureAttribute.createBaseColorTexture(blocksTextureAtlas.getTexture()));
            if (blocksTextureAtlas.getEmissiveTexture() != null) mat.set(PBRTextureAttribute.createEmissiveTexture(blocksTextureAtlas.getEmissiveTexture()));
            if (blocksTextureAtlas.getNormalTexture() != null) mat.set(PBRTextureAttribute.createNormalTexture(blocksTextureAtlas.getNormalTexture()));
            return mat;
        };
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

        public Builder pbrShader(Supplier<? extends ShaderProvider> shaderSupplier) {
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
            RenderPass renderPass = new RenderPass(this);
            register(renderPass);
            return renderPass;
        }

        public static final class CullFace implements AttributeDelegate {
            private final int mode;

            public CullFace(int mode) {
                this.mode = mode;
            }

            @Override
                    public void apply(Material material) {
                        material.set(IntAttribute.createCullFace(mode));
                    }

            public int mode() {
                return mode;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (CullFace) obj;
                return this.mode == that.mode;
            }

            @Override
            public int hashCode() {
                return Objects.hash(mode);
            }

            @Override
            public String toString() {
                return "CullFace[" +
                       "mode=" + mode + ']';
            }

                }

        public interface AttributeDelegate {
            void apply(Material material);
        }

        public static final class AlphaTest implements AttributeDelegate {
            private final float threshold;

            public AlphaTest(float threshold) {
                this.threshold = threshold;
            }

            @Override
                    public void apply(Material material) {
                        material.set(FloatAttribute.createAlphaTest(threshold));
                    }

            public float threshold() {
                return threshold;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (AlphaTest) obj;
                return Float.floatToIntBits(this.threshold) == Float.floatToIntBits(that.threshold);
            }

            @Override
            public int hashCode() {
                return Objects.hash(threshold);
            }

            @Override
            public String toString() {
                return "AlphaTest[" +
                       "threshold=" + threshold + ']';
            }

                }

        public static final class DepthTest implements AttributeDelegate {
            private final int func;
            private final float rangeNear;
            private final float rangeFar;
            private final boolean depthMask;

            public DepthTest(int func, float rangeNear, float rangeFar, boolean depthMask) {
                this.func = func;
                this.rangeNear = rangeNear;
                this.rangeFar = rangeFar;
                this.depthMask = depthMask;
            }

            @Override
                    public void apply(Material material) {
                        material.set(new DepthTestAttribute(func, rangeNear, rangeFar, depthMask));
                    }

            public int func() {
                return func;
            }

            public float rangeNear() {
                return rangeNear;
            }

            public float rangeFar() {
                return rangeFar;
            }

            public boolean depthMask() {
                return depthMask;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (DepthTest) obj;
                return this.func == that.func &&
                       Float.floatToIntBits(this.rangeNear) == Float.floatToIntBits(that.rangeNear) &&
                       Float.floatToIntBits(this.rangeFar) == Float.floatToIntBits(that.rangeFar) &&
                       this.depthMask == that.depthMask;
            }

            @Override
            public int hashCode() {
                return Objects.hash(func, rangeNear, rangeFar, depthMask);
            }

            @Override
            public String toString() {
                return "DepthTest[" +
                       "func=" + func + ", " +
                       "rangeNear=" + rangeNear + ", " +
                       "rangeFar=" + rangeFar + ", " +
                       "depthMask=" + depthMask + ']';
            }

                }

        public static final class Blending implements AttributeDelegate {
            private final int src;
            private final int dst;

            public Blending(int src, int dst) {
                this.src = src;
                this.dst = dst;
            }

            @Override
            public void apply(Material material) {
                        material.set(new BlendingAttribute(src, dst));
                    }

            public int src() {
                return src;
            }

            public int dst() {
                return dst;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (Blending) obj;
                return this.src == that.src &&
                       this.dst == that.dst;
            }

            @Override
            public int hashCode() {
                return Objects.hash(src, dst);
            }

            @Override
            public String toString() {
                return "Blending[" +
                       "src=" + src + ", " +
                       "dst=" + dst + ']';
            }

                }

        public static final class Atlas implements AttributeDelegate {
            private final NamespaceID textureId;
            private final TextureAtlas.TextureAtlasType type;

            public Atlas(NamespaceID textureId, TextureAtlas.TextureAtlasType type) {
                this.textureId = textureId;
                this.type = type;
            }

            @Override
                    public void apply(Material material) {
                        QuantumClient client = QuantumClient.get();
                        TextureAtlas atlas = client.getAtlas(textureId);
                        if (atlas == null) throw new IllegalStateException("Texture atlas " + textureId + " not found");
                        atlas.apply(material, type);
                    }

            public NamespaceID textureId() {
                return textureId;
            }

            public TextureAtlas.TextureAtlasType type() {
                return type;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (Atlas) obj;
                return Objects.equals(this.textureId, that.textureId) &&
                       Objects.equals(this.type, that.type);
            }

            @Override
            public int hashCode() {
                return Objects.hash(textureId, type);
            }

            @Override
            public String toString() {
                return "Atlas[" +
                       "textureId=" + textureId + ", " +
                       "type=" + type + ']';
            }

                }

        public static final class Texture implements AttributeDelegate {
            private final NamespaceID textureId;

            public Texture(NamespaceID textureId) {
                this.textureId = textureId;
            }

            @Override
                    public void apply(Material material) {
                        QuantumClient client = QuantumClient.get();
                        TextureManager textureManager = client.getTextureManager();
                        var texture = textureManager.getTexture(textureId);
                        material.set(TextureAttribute.createDiffuse(texture));
                    }

            public NamespaceID textureId() {
                return textureId;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (Texture) obj;
                return Objects.equals(this.textureId, that.textureId);
            }

            @Override
            public int hashCode() {
                return Objects.hash(textureId);
            }

            @Override
            public String toString() {
                return "Texture[" +
                       "textureId=" + textureId + ']';
            }

                }

        public static final class TextureRegion implements AttributeDelegate {
            private final NamespaceID atlasId;
            private final NamespaceID textureId;
            private final TextureAtlas.TextureAtlasType type;

            public TextureRegion(NamespaceID atlasId, NamespaceID textureId, TextureAtlas.TextureAtlasType type) {
                this.atlasId = atlasId;
                this.textureId = textureId;
                this.type = type;
            }

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

            public NamespaceID atlasId() {
                return atlasId;
            }

            public NamespaceID textureId() {
                return textureId;
            }

            public TextureAtlas.TextureAtlasType type() {
                return type;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (TextureRegion) obj;
                return Objects.equals(this.atlasId, that.atlasId) &&
                       Objects.equals(this.textureId, that.textureId) &&
                       Objects.equals(this.type, that.type);
            }

            @Override
            public int hashCode() {
                return Objects.hash(atlasId, textureId, type);
            }

            @Override
            public String toString() {
                return "TextureRegion[" +
                       "atlasId=" + atlasId + ", " +
                       "textureId=" + textureId + ", " +
                       "type=" + type + ']';
            }

                }

        public static final class FogColor implements AttributeDelegate {
            private final Color color;

            public FogColor(Color color) {
                this.color = color;
            }

            @Override
                    public void apply(Material material) {
                        material.set(ColorAttribute.createFog(color));
                    }

            public Color color() {
                return color;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (FogColor) obj;
                return Objects.equals(this.color, that.color);
            }

            @Override
            public int hashCode() {
                return Objects.hash(color);
            }

            @Override
            public String toString() {
                return "FogColor[" +
                       "color=" + color + ']';
            }

                }

        public static final class Fog implements AttributeDelegate {
            private final float density;
            private final float start;
            private final float end;

            public Fog(float density, float start, float end) {
                this.density = density;
                this.start = start;
                this.end = end;
            }

            @Override
                    public void apply(Material material) {
                        material.set(FogAttribute.createFog(start, end, density));
                    }

            public float density() {
                return density;
            }

            public float start() {
                return start;
            }

            public float end() {
                return end;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (Fog) obj;
                return Float.floatToIntBits(this.density) == Float.floatToIntBits(that.density) &&
                       Float.floatToIntBits(this.start) == Float.floatToIntBits(that.start) &&
                       Float.floatToIntBits(this.end) == Float.floatToIntBits(that.end);
            }

            @Override
            public int hashCode() {
                return Objects.hash(density, start, end);
            }

            @Override
            public String toString() {
                return "Fog[" +
                       "density=" + density + ", " +
                       "start=" + start + ", " +
                       "end=" + end + ']';
            }

                }

        public static final class AmbientColor implements AttributeDelegate {
            private final Color color;

            public AmbientColor(Color color) {
                this.color = color;
            }

            @Override
                    public void apply(Material material) {
                        material.set(ColorAttribute.createAmbient(color));
                    }

            public Color color() {
                return color;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (AmbientColor) obj;
                return Objects.equals(this.color, that.color);
            }

            @Override
            public int hashCode() {
                return Objects.hash(color);
            }

            @Override
            public String toString() {
                return "AmbientColor[" +
                       "color=" + color + ']';
            }

                }
    }

    public static final RenderPass SKYBOX = RenderPass.builder(Position(), Normal(), ColorUnpacked(), TexCoords(0))
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

    public static final RenderPass TRANSPARENT = RenderPass.builder(Position(), Normal(), ColorUnpacked(), TexCoords(0))
            .name("transparent")
            .shader(Shaders.TRANSPARENT)
            .blending()
            .depthTest()
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .build();

    public static final RenderPass WATER = RenderPass.builder(Position(), Normal(), ColorUnpacked(), TexCoords(0))
            .name("water")
            .shader(Shaders.WATER)
            .blending()
            .depthTest()
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .build();

    public static final RenderPass OPAQUE = RenderPass.builder(Position(), Normal(), ColorUnpacked(), TexCoords(0))
            .name("opaque")
            .shader(Shaders.WORLD)
            .depthTest()
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .build();

    public static final RenderPass GIZMO = RenderPass.builder(Position(), ColorUnpacked(), TexCoords(0))
            .name("gizmo")
            .shader(Shaders.GIZMO)
            .alphaTest(0.01f)
            .blending()
            .depthTest()
            .build();

    public static final RenderPass GIZMO_OUTLINE = RenderPass.builder(Position(), ColorUnpacked(), TexCoords(0))
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

    public static final RenderPass CUTOUT = RenderPass.builder(Position(), Normal(), ColorUnpacked(), TexCoords(0))
            .name("cutout")
            .shader(Shaders.CUTOUT)
            .atlas(TextureAtlasManager.BLOCK_ATLAS_ID)
            .alphaTest()
            .depthTest()
            .build();

    public static final RenderPass FOLIAGE = RenderPass.builder(Position(), Normal(), ColorUnpacked(), TexCoords(0))
            .name("foliage")
            .shader(Shaders.FOLIAGE)
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
        ClientRegistries.RENDER_PASS.register(new NamespaceID(CommonConstants.NAMESPACE, renderType.name), renderType);
        return renderType;
    }

    public static RenderPass[] values() {
        return MANAGED.toArray(RenderPass.class);
    }

    public static RenderPass byName(String renderPass) {
        RenderPass pass = ClientRegistries.RENDER_PASS.get(new NamespaceID(CommonConstants.NAMESPACE, renderPass));
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

    public Material createPBRMaterial() {
        return pbrMaterial.get();
    }


    public Material createInstanceMaterial() {
        return instanceMaterial.get();
    }
}

