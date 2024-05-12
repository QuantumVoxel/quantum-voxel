package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.data.gen.provider.DepthFunc;
import dev.ultreon.quantum.client.render.DestinationBlending;
import dev.ultreon.quantum.client.render.SourceBlending;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MaterialManager implements Manager<Material> {
    public static final @NotNull Identifier DEFAULT_ID = Identifier.parse("default");
    private final ResourceManager resourceManager;
    private final TextureManager textureManager;
    private final CubemapManager cubemapManager;

    private final Material defaultMaterial = new Material();
    private final Map<Identifier, Material> materials = new LinkedHashMap<>();

    public MaterialManager(ResourceManager resourceManager, TextureManager textureManager, CubemapManager cubemapManager) {
        this.resourceManager = resourceManager;
        this.textureManager = textureManager;
        this.cubemapManager = cubemapManager;
    }

    public void reload(ReloadContext context) {
        this.materials.clear();
        this.defaultMaterial.id = "default";
        this.register(Identifier.parse("default"), this.defaultMaterial);
    }

    @Override
    public Material register(@NotNull Identifier id, @NotNull Material material) {
        this.materials.put(id, material);
        return material;
    }

    public @Nullable Material get(Identifier id) {
        if (id.equals(DEFAULT_ID)) {
            return this.defaultMaterial;
        }

        if (this.materials.containsKey(id)) {
            return this.materials.get(id);
        }

        try (InputStream inputStream = resourceManager.openResourceStream(id.mapPath(path -> "materials/" + path + ".json5"))) {
            Material material = new Material();
            material.id = id.toString();
            Json5Element parse = CommonConstants.JSON5.parse(inputStream);

            this.loadInto(material, this.textureManager, this.cubemapManager, parse);
            this.register(id, material);
            return material;
        } catch (IOException e) {
            QuantumClient.LOGGER.error("Failed to load material %s", id, e);
            return new Material();
        }
    }

    private void loadInto(Material material, TextureManager textureManager, CubemapManager cubemapManager, Json5Element parse) {
        if (!parse.isJson5Object()) {
            return;
        }

        Json5Object asJson5Object = parse.getAsJson5Object();
        Json5Array attributesArr = asJson5Object.getAsJson5Array("attributes");
        attributesArr.forEach(attributeElem -> {
            Json5Object attrObj = attributeElem.getAsJson5Object();
            String type = attrObj.getAsJson5Primitive("type").getAsString();
            Attribute attribute = switch (type) {
                case "blending" -> loadBlending(attrObj);
                case "depth_test" -> loadDepthTest(attrObj);
                case "color" -> loadColor(attrObj);
                case "texture" -> loadTexture(attrObj, textureManager);
                case "cubemap" -> loadCubemap(attrObj, cubemapManager);
                default -> {
                    QuantumClient.LOGGER.warn("Unknown material attribute type %s", type);
                    yield null;
                }
            };

            if (attribute != null) {
                material.set(attribute);
            }
        });
    }

    private Attribute loadCubemap(Json5Object attrObj, CubemapManager cubemapManager) {
        @NotNull Identifier textureId = Identifier.parse(attrObj.getAsJson5Primitive("cubemap").getAsString());
        Cubemap cubemap = cubemapManager.get(textureId);
        if (cubemap == null) {
            return null;
        }
        return new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap);
    }

    private Attribute loadBlending(Json5Object attrObj) {
        String src = attrObj.getAsJson5Primitive("src_factor").getAsString();
        String dst = attrObj.getAsJson5Primitive("dst_factor").getAsString();
        
        boolean blended = attrObj.getAsJson5Primitive("blended").getAsBoolean();
        
        float opacity = attrObj.getAsJson5Primitive("opacity").getAsFloat(); // TODO: Implement opacity

        SourceBlending srcBlending = SourceBlending.valueOf(src.toUpperCase(Locale.ROOT));
        DestinationBlending dstBlending = DestinationBlending.valueOf(dst.toUpperCase(Locale.ROOT));

        return new BlendingAttribute(blended, srcBlending.id, dstBlending.id, opacity);
    }

    private Attribute loadDepthTest(Json5Object attrObj) {
        Json5Primitive depthMask1 = attrObj.getAsJson5Primitive("depth_mask");
        DepthFunc func = DepthFunc.valueOf(attrObj.getAsJson5Primitive("func").getAsString().toUpperCase(Locale.ROOT));
        boolean depthMask = depthMask1 == null || depthMask1.getAsBoolean();

        Json5Object rangeObj = attrObj.getAsJson5Object("range");
        float near = 0;
        float far = 1;
        if (rangeObj != null) {
            near = rangeObj.getAsJson5Primitive("near").getAsFloat();
            far = rangeObj.getAsJson5Primitive("far").getAsFloat();
        }

        return new DepthTestAttribute(func.getGlFunc(), near, far, depthMask);
    }

    private Attribute loadColor(Json5Object attrObj) {
        Json5Primitive r = attrObj.getAsJson5Primitive("r");
        Json5Primitive g = attrObj.getAsJson5Primitive("g");
        Json5Primitive b = attrObj.getAsJson5Primitive("b");
        Json5Primitive a = attrObj.getAsJson5Primitive("a");

        String type = attrObj.getAsJson5Primitive("color_type").getAsString();

        Color color = new Color(r.getAsFloat(), g.getAsFloat(), b.getAsFloat(), a.getAsFloat());

        return switch (type) {
            case "diffuse" -> ColorAttribute.createDiffuse(color);
            case "ambient" -> ColorAttribute.createAmbient(color);
            case "ambient_light" -> ColorAttribute.createAmbientLight(color);
            case "emissive" -> ColorAttribute.createEmissive(color);
            case "specular" -> ColorAttribute.createSpecular(color);
            case "fog" -> ColorAttribute.createFog(color);
            case "reflection" -> ColorAttribute.createReflection(color);
            default -> {
                QuantumClient.LOGGER.warn("Unknown material color type %s", type);
                yield null;
            }
        };
    }

    private Attribute loadTexture(Json5Object attrObj, TextureManager textureManager) {
        String identifier = attrObj.getAsJson5Primitive("target").getAsString();
        Identifier id = new Identifier(identifier).mapPath(path -> "textures/" + path + ".png");
        Texture texture = textureManager.getTexture(id);

        String type = attrObj.getAsJson5Primitive("texture_type").getAsString();
        return createTexAttr(type, texture);
    }

    @Nullable
    private static Attribute createTexAttr(String textureType, Texture texture) {
        return switch (textureType) {
            case "diffuse" -> TextureAttribute.createDiffuse(texture);
            case "normal" -> TextureAttribute.createNormal(texture);
            case "specular" -> TextureAttribute.createSpecular(texture);
            case "emissive" -> TextureAttribute.createEmissive(texture);
            case "bump" -> TextureAttribute.createBump(texture);
            case "reflection" -> TextureAttribute.createReflection(texture);
            default -> {
                QuantumClient.LOGGER.warn("Unknown material texture type %s", textureType);
                yield null;
            }
        };
    }
}
