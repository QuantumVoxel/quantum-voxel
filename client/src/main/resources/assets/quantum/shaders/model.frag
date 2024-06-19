#version 410

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if __VERSION__ < 130
#define texture texture
#else
#define texture texture
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

in vec3 normal;

#if defined(colorFlag)
in vec4 v_color;
#endif

#ifdef blendedFlag
#ifdef alphaTestFlag
in float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
in MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
in MED vec2 v_specularUV;
#endif

#ifdef emissiveTextureFlag
in MED vec2 v_emissiveUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef emissiveColorFlag
uniform vec4 u_emissiveColor;
#endif

#ifdef emissiveTextureFlag
uniform sampler2D u_emissiveTexture;
#endif

#ifdef lightingFlag
in vec3 v_lightDiffuse;

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef specularFlag
in vec3 v_lightSpecular;
#endif //specularFlag

#ifdef shadowMapFlag
uniform sampler2D u_shadowTexture;
uniform float u_shadowPCFOffset;
in vec3 v_shadowMapUv;
#define separateAmbientFlag

float getShadowness(vec2 offset)
{
    const vec4 bitShifts = vec4(1.0, 1.0 / 255.0, 1.0 / 65025.0, 1.0 / 16581375.0);
    return step(v_shadowMapUv.z, dot(texture(u_shadowTexture, v_shadowMapUv.xy + offset), bitShifts));//+(1.0/255.0));
}

float getShadow()
{
    return (//getShadowness(vec2(0,0)) +
    getShadowness(vec2(u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, u_shadowPCFOffset)) +
    getShadowness(vec2(u_shadowPCFOffset, -u_shadowPCFOffset)) +
    getShadowness(vec2(-u_shadowPCFOffset, -u_shadowPCFOffset))) * 0.25;
}
#endif //shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
in vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif //lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
in float v_fog;
#endif // fogFlag

in vec3 v_position;

uniform float u_globalSunlight;
uniform vec2 u_atlasSize;
uniform vec2 u_atlasOffset;

struct SHC{
    float L00, L1m1, L10, L11, L2m2, L2m1, L20, L21, L22;
};

SHC groove = SHC(
    0.3783264,
    0.2887813,
    0.0379030,
    -0.1033028,
    -0.0621750,
    0.0077820,
    -0.0935561,
    -0.0572703,
    0.0203348
);

float sh_light(vec3 normal, SHC l){
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;

    const float C1 = 0.429043;
    const float C2 = 0.511664;
    const float C3 = 0.743125;
    const float C4 = 0.886227;
    const float C5 = 0.247708;

    return (
        C1 * l.L22 * (x * x - y * y) +
        C3 * l.L20 * z * z +
        C4 * l.L00 -
        C5 * l.L20 +
        2.0 * C1 * l.L2m2 * x * y +
        2.0 * C1 * l.L21  * x * z +
        2.0 * C1 * l.L2m1 * y * z +
        2.0 * C2 * l.L11  * x +
        2.0 * C2 * l.L1m1 * y +
        2.0 * C2 * l.L10  * z
    );
}

float gamma(float color){
    return pow(color, 1.0/2.0);
}

out vec4 fragColor;
in vec3 fragCoord;

void main() {
    #if defined(diffuseTextureFlag)
        vec2 v_diffuseTexUV = v_diffuseUV;
    #endif

    #if defined(emissiveTextureFlag)
        vec2 v_emissiveTexUV = v_emissiveUV;
    #endif

    #if defined(diffuseTextureFlag) && defined(colorFlag)
        vec4 diffuse = texture(u_diffuseTexture, v_diffuseTexUV) * v_color;
    #elif defined(diffuseTextureFlag)
        vec4 diffuse = texture(u_diffuseTexture, v_diffuseTexUV);
    #elif defined(diffuseColorFlag) && defined(colorFlag)
        vec4 diffuse = u_diffuseColor * v_color;
    #elif defined(diffuseColorFlag)
        vec4 diffuse = u_diffuseColor;
    #elif defined(colorFlag)
        vec4 diffuse = v_color;
    #else
        vec4 diffuse = vec4(1.0);
    #endif

    #if defined(emissiveTextureFlag)
        vec4 emissive = texture(u_emissiveTexture, v_emissiveTexUV);
    #else
        vec4 emissive = vec4(0.0);
    #endif

    fragColor.rgb = (diffuse.rgb) * u_globalSunlight + (emissive.rgb * (1.0 - u_globalSunlight));

    #ifdef blendedFlag
    #ifdef alphaTestFlag
        if (fragColor.a <= v_alphaTest)
            discard;
    #endif
    #else
        fragColor.a = 1.0;
        if (diffuse.a <= 0.02)
            discard;
    #endif

    fragColor = vec4(fragColor.xyz*gamma(sh_light(normal, groove)), fragColor.w);

    #ifdef fogFlag
        fragColor.rgb = mix(fragColor.rgb, vec3(u_fogColor), v_fog);
    #endif // end fogFlag
}
