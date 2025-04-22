package dev.ultreon.quantum.client.management;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.utils.JsonValue;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.block.Block;
import dev.ultreon.quantum.client.QuantumClient;
import dev.ultreon.quantum.client.data.gen.provider.DepthFunc;
import dev.ultreon.quantum.client.render.DestinationBlending;
import dev.ultreon.quantum.client.render.SourceBlending;
import dev.ultreon.quantum.client.texture.TextureManager;
import dev.ultreon.quantum.resources.ReloadContext;
import dev.ultreon.quantum.resources.ResourceManager;
import dev.ultreon.quantum.util.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MaterialManager implements Manager<Material> {
    public static final Material DEFAULT_MATERIAL = new Material();
    public static final @NotNull NamespaceID DEFAULT_ID = NamespaceID.parse("default");
    private final Map<Block, Material> blockMaterialRegistry = new LinkedHashMap<>();

    private final ResourceManager resourceManager;
    private final TextureManager textureManager;
    private final CubemapManager cubemapManager;

    private final Map<NamespaceID, Material> materials = new HashMap<>();

    public MaterialManager(ResourceManager resourceManager, TextureManager textureManager, CubemapManager cubemapManager) {
        this.resourceManager = resourceManager;
        this.textureManager = textureManager;
        this.cubemapManager = cubemapManager;
    }

    public Material getMaterialFor(Block block) {
        Material blockMaterial = blockMaterialRegistry.get(block);
        if (blockMaterial == null) {
            registerBlockMaterial(block);
            blockMaterial = blockMaterialRegistry.get(block);
            if (blockMaterial == null) {
                blockMaterialRegistry.put(block, DEFAULT_MATERIAL);
                return DEFAULT_MATERIAL;
            }
        }
        return blockMaterial;
    }

    public void registerBlockMaterial(Block block, Material material) {
        if (blockMaterialRegistry.containsKey(block))
            QuantumClient.LOGGER.warn("Material for block {} already registered, overwriting...", block.getId());
        blockMaterialRegistry.put(block, material);
    }

    public void registerBlockMaterial(Block block) {
        Material material = get(block.getId().mapPath(path -> "block/" + path));
        if (material == null) return;
        registerBlockMaterial(block, material);
    }

    public void reload(ReloadContext context) {
        this.materials.clear();
        DEFAULT_MATERIAL.id = "default";
        this.register(NamespaceID.parse("default"), DEFAULT_MATERIAL);
    }

    @Override
    public Material register(@NotNull NamespaceID id, @NotNull Material material) {
        this.materials.put(id, material);
        return material;
    }

    public @Nullable Material get(NamespaceID id) {
        if (id.equals(DEFAULT_ID)) {
            return DEFAULT_MATERIAL;
        }

        if (this.materials.containsKey(id)) {
            return this.materials.get(id);
        }

        try (InputStream inputStream = resourceManager.openResourceStream(id.mapPath(path -> "materials/" + path + ".json5"))) {
            if (inputStream == null) {
                return null;
            }
            Material material = new Material();
            material.id = id.toString();
            JsonValue parse = CommonConstants.JSON_READ.parse(inputStream);

            this.loadInto(material, this.textureManager, this.cubemapManager, parse);
            this.register(id, material);
            return material;
        } catch (IOException e) {
            QuantumClient.LOGGER.error("Failed to load material {}", id, e);
            return new Material();
        }
    }

    private void loadInto(Material material, TextureManager textureManager, CubemapManager cubemapManager, JsonValue parse) {
        if (!parse.isObject()) {
            return;
        }

        JsonValue asJsonValue = parse;
        JsonValue attributesArr = asJsonValue.get("attributes");
        attributesArr.forEach(attributeElem -> {
            JsonValue attrObj = attributeElem;
            String type = attrObj.getString("type");
            Attribute attribute;
            switch (type) {
                case "blending":
                    attribute = loadBlending(attrObj);
                    break;
                case "depth_test":
                    attribute = loadDepthTest(attrObj);
                    break;
                case "color":
                    attribute = loadColor(attrObj);
                    break;
                case "texture":
                    attribute = loadTexture(attrObj, textureManager);
                    break;
                case "cubemap":
                    attribute = loadCubemap(attrObj, cubemapManager);
                    break;
                default:
                    QuantumClient.LOGGER.warn("Unknown material attribute type {}", type);
                    attribute = null;
                    break;
            }

            if (attribute != null) {
                material.set(attribute);
            }
        });
    }

    private Attribute loadCubemap(JsonValue attrObj, CubemapManager cubemapManager) {
        @NotNull NamespaceID textureId = NamespaceID.parse(attrObj.getString("cubemap"));
        Cubemap cubemap = cubemapManager.get(textureId);
        if (cubemap == null) {
            return null;
        }
        return new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap);
    }

    private Attribute loadBlending(JsonValue attrObj) {
        String src = attrObj.get("src_factor").asString();
        String dst = attrObj.get("dst_factor").asString();
        
        boolean blended = attrObj.get("blended").asBoolean();
        
        float opacity = attrObj.get("opacity").asFloat(); // TODO: Implement opacity

        SourceBlending srcBlending = SourceBlending.valueOf(src.toUpperCase(Locale.ROOT));
        DestinationBlending dstBlending = DestinationBlending.valueOf(dst.toUpperCase(Locale.ROOT));

        return new BlendingAttribute(blended, srcBlending.id, dstBlending.id, opacity);
    }

    private Attribute loadDepthTest(JsonValue attrObj) {
        JsonValue depthMask1 = attrObj.get("depth_mask");
        DepthFunc func = DepthFunc.valueOf(attrObj.get("func").asString().toUpperCase(Locale.ROOT));
        boolean depthMask = depthMask1 == null || depthMask1.asBoolean();

        JsonValue rangeObj = attrObj.get("range");
        float near = 0;
        float far = 1;
        if (rangeObj != null) {
            near = rangeObj.get("near").asFloat();
            far = rangeObj.get("far").asFloat();
        }

        return new DepthTestAttribute(func.getGlFunc(), near, far, depthMask);
    }

    private Attribute loadColor(JsonValue attrObj) {
        JsonValue r = attrObj.get("r");
        JsonValue g = attrObj.get("g");
        JsonValue b = attrObj.get("b");
        JsonValue a = attrObj.get("a");

        String type = attrObj.get("color_type").asString();

        Color color = new Color(r.asFloat(), g.asFloat(), b.asFloat(), a.asFloat());

        switch (type) {
            case "diffuse":
                return ColorAttribute.createDiffuse(color);
            case "ambient":
                return ColorAttribute.createAmbient(color);
            case "ambient_light":
                return ColorAttribute.createAmbientLight(color);
            case "emissive":
                return ColorAttribute.createEmissive(color);
            case "specular":
                return ColorAttribute.createSpecular(color);
            case "fog":
                return ColorAttribute.createFog(color);
            case "reflection":
                return ColorAttribute.createReflection(color);
            default:
                QuantumClient.LOGGER.warn("Unknown material color type {}", type);
                return null;
        }
    }

    private Attribute loadTexture(JsonValue attrObj, TextureManager textureManager) {
        String identifier = attrObj.get("target").asString();
        NamespaceID id = new NamespaceID(identifier).mapPath(path -> "textures/" + path + ".png");
        Texture texture = textureManager.getTexture(id);

        String type = attrObj.get("texture_type").asString();
        return createTexAttr(type, texture);
    }

    @Nullable
    private static Attribute createTexAttr(String textureType, Texture texture) {
        switch (textureType) {
            case "diffuse":
                return TextureAttribute.createDiffuse(texture);
            case "normal":
                return TextureAttribute.createNormal(texture);
            case "specular":
                return TextureAttribute.createSpecular(texture);
            case "emissive":
                return TextureAttribute.createEmissive(texture);
            case "bump":
                return TextureAttribute.createBump(texture);
            case "reflection":
                return TextureAttribute.createReflection(texture);
            default:
                QuantumClient.LOGGER.warn("Unknown material texture type {}", textureType);
                return null;
        }
    }

}
