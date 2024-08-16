package dev.ultreon.quantum.client.util;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ultreon.quantum.client.GameTextureAttribute;
import dev.ultreon.quantum.client.data.gen.provider.DepthFunc;
import dev.ultreon.quantum.util.NamespaceID;

public class ClientCodecs {
    public static final MapCodec<FloatAttribute> ALPHA_TEST_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.FLOAT.fieldOf("value").forGetter((FloatAttribute floatAttribute) -> floatAttribute.value))
            .apply(instance, FloatAttribute::createAlphaTest)
    );
    public static final MapCodec<FloatAttribute> SHININESS_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.FLOAT.fieldOf("value").forGetter((FloatAttribute floatAttribute) -> floatAttribute.value))
            .apply(instance, FloatAttribute::createShininess)
    );
    public static final MapCodec<IntAttribute> CULL_FACE_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(Codec.INT.fieldOf("value").forGetter((IntAttribute intAttribute) -> intAttribute.value))
            .apply(instance, IntAttribute::createCullFace)
    );
    public static final MapCodec<GameTextureAttribute> TEXTURE_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.fieldOf("type").forGetter((GameTextureAttribute textureAttribute) -> Attribute.getAttributeAlias(textureAttribute.type)),
                    NamespaceID.CODEC.fieldOf("atlas").forGetter(GameTextureAttribute::getAtlasId),
                    NamespaceID.CODEC.fieldOf("texture").forGetter(GameTextureAttribute::getTextureId)
            )
            .apply(instance, GameTextureAttribute::new));
    public static final MapCodec<TextureAtlasAttribute> TEXTURE_ATLAS_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.fieldOf("type").forGetter((TextureAtlasAttribute textureAttribute) -> Attribute.getAttributeAlias(textureAttribute.type)),
                    NamespaceID.CODEC.fieldOf("atlas").forGetter(TextureAtlasAttribute::getAtlasId)
            )
            .apply(instance, TextureAtlasAttribute::new));

    public static final MapCodec<ColorAttribute> COLOR_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.fieldOf("type").forGetter((ColorAttribute colorAttribute) -> Attribute.getAttributeAlias(colorAttribute.type)),
                    Codec.FLOAT.fieldOf("red").forGetter((ColorAttribute colorAttribute) -> colorAttribute.color.r),
                    Codec.FLOAT.fieldOf("green").forGetter((ColorAttribute colorAttribute) -> colorAttribute.color.g),
                    Codec.FLOAT.fieldOf("blue").forGetter((ColorAttribute colorAttribute) -> colorAttribute.color.b),
                    Codec.FLOAT.optionalFieldOf("alpha", 1f).forGetter((ColorAttribute colorAttribute) -> colorAttribute.color.a)
            )
            .apply(instance, (type, red, green, blue, alpha) -> new ColorAttribute(Attribute.getAttributeType(type), red, green, blue, alpha))
    );

    public static final MapCodec<GameCubemapAttribute> CUBEMAP_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.fieldOf("type").forGetter((GameCubemapAttribute cubemapAttribute) -> Attribute.getAttributeAlias(cubemapAttribute.type)),
                    NamespaceID.CODEC.fieldOf("atlas").forGetter(GameCubemapAttribute::getCubemapId)
            )
            .apply(instance, GameCubemapAttribute::new));

    public static final MapCodec<DepthTestAttribute> DEPTH_TEST_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.xmap(s -> DepthFunc.valueOf(s.toUpperCase()), func -> func.name().toLowerCase()).fieldOf("func").forGetter((DepthTestAttribute depthTestAttribute) -> DepthFunc.byGlId(depthTestAttribute.depthFunc)),
                    Codec.BOOL.optionalFieldOf("mask", true).forGetter((DepthTestAttribute depthTestAttribute) -> depthTestAttribute.depthMask),
                    Codec.FLOAT.optionalFieldOf("range_near", 0.0f).forGetter((DepthTestAttribute depthTestAttribute) -> depthTestAttribute.depthRangeNear),
                    Codec.FLOAT.optionalFieldOf("range_far", 1000.0f).forGetter((DepthTestAttribute depthTestAttribute) -> depthTestAttribute.depthRangeFar)
            )
            .apply(instance, (DepthFunc func, Boolean mask, Float range_near, Float range_far) -> new DepthTestAttribute(DepthTestAttribute.Type, func.getGlFunc(), range_near, range_far, mask)));

    public static final MapCodec<BlendingAttribute> BLENDING_ATTRIBUTE = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    Codec.STRING.xmap(s -> BlendFactor.valueOf(s.toUpperCase()), func -> func.name().toLowerCase()).fieldOf("src").forGetter((BlendingAttribute blendingAttribute) -> BlendFactor.byGlFactor(blendingAttribute.sourceFunction)),
                    Codec.STRING.xmap(s -> BlendFactor.valueOf(s.toUpperCase()), func -> func.name().toLowerCase()).fieldOf("dest").forGetter((BlendingAttribute blendingAttribute) -> BlendFactor.byGlFactor(blendingAttribute.destFunction)),
                    Codec.FLOAT.optionalFieldOf("opacity", 1.0f).forGetter((BlendingAttribute blendingAttribute) -> blendingAttribute.opacity),
                    Codec.BOOL.optionalFieldOf("blended", true).forGetter((BlendingAttribute blendingAttribute) -> blendingAttribute.blended)
            )
            .apply(instance, (src, dst, opacity, blended) -> new BlendingAttribute(blended, src.getGlFactor(), dst.getGlFactor(), opacity)));



    public static final MapCodec<Attribute> ATTRIBUTE = new KeyDispatchCodec<>(
            "type",
            Codec.STRING,
            (Attribute a) -> switch (a) {
                case FloatAttribute floatAttribute -> {
                    String attributeAlias = Attribute.getAttributeAlias(floatAttribute.type);
                    yield switch (attributeAlias) {
                        case FloatAttribute.AlphaTestAlias -> DataResult.success("alpha_test");
                        case FloatAttribute.ShininessAlias -> DataResult.success("shininess");
                        case null, default -> DataResult.error(() -> "Unknown float attribute type: " + attributeAlias);
                    };
                }
                case IntAttribute intAttribute -> {
                    String attributeAlias = Attribute.getAttributeAlias(intAttribute.type);
                    yield switch (attributeAlias) {
                        case IntAttribute.CullFaceAlias -> DataResult.success("cull_face");
                        case null, default -> DataResult.error(() -> "Unknown integer attribute type: " + attributeAlias);
                    };
                }
                case GameTextureAttribute textureAttribute -> DataResult.success("texture");
                case TextureAtlasAttribute textureAtlasAttribute -> DataResult.success("texture_atlas");
                case ColorAttribute colorAttribute -> DataResult.success("color");
                case CubemapAttribute cubemapAttribute -> DataResult.success("cubemap");
                case DepthTestAttribute depthTestAttribute -> DataResult.success("depth_test");
                case BlendingAttribute blendingAttribute -> DataResult.success("blending");
                case null, default -> DataResult.error(() -> "Unknown attribute type: " + a.getClass().getName());
            },
            (String s) -> switch (s) {
                case "alpha_test" -> DataResult.success(ALPHA_TEST_ATTRIBUTE);
                case "shininess" -> DataResult.success(SHININESS_ATTRIBUTE);
                case "cull_face" -> DataResult.success(CULL_FACE_ATTRIBUTE);
                case "texture" -> DataResult.success(TEXTURE_ATTRIBUTE);
                case "texture_atlas" -> DataResult.success(TEXTURE_ATLAS_ATTRIBUTE);
                case "color" -> DataResult.success(COLOR_ATTRIBUTE);
                case "cubemap" -> DataResult.success(CUBEMAP_ATTRIBUTE);
                case "depth_test" -> DataResult.success(DEPTH_TEST_ATTRIBUTE);
                case "blending" -> DataResult.success(BLENDING_ATTRIBUTE);
                default -> DataResult.error(() -> "Unknown attribute type: " + s);
            }
    );
}
