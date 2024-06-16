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
in float v_opacity;
#ifdef alphaTestFlag
in float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag) || defined(emissiveTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
in MED vec2 diffuseUV;
#endif

#ifdef specularTextureFlag
in MED vec2 v_specularUV;
#endif

#ifdef emissiveTextureFlag
in MED vec2 emissiveUV;
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

in vec2 v_rawUV;
in vec3 v_position;

uniform float u_globalSunlight;
uniform vec2 u_atlasSize;
uniform vec2 u_atlasOffset;

struct SHC{
    vec3 L00, L1m1, L10, L11, L2m2, L2m1, L20, L21, L22;
};

SHC groove = SHC(
vec3( 0.3783264,  0.4260425,  0.4504587),
vec3( 0.2887813,  0.3586803,  0.4147053),
vec3( 0.0379030,  0.0295216,  0.0098567),
vec3(-0.1033028, -0.1031690, -0.0884924),
vec3(-0.0621750, -0.0554432, -0.0396779),
vec3( 0.0077820, -0.0148312, -0.0471301),
vec3(-0.0935561, -0.1254260, -0.1525629),
vec3(-0.0572703, -0.0502192, -0.0363410),
vec3( 0.0203348, -0.0044201, -0.0452180)
);

SHC beach = SHC(
vec3( 0.6841148,  0.6929004,  0.7069543),
vec3( 0.3173355,  0.3694407,  0.4406839),
vec3(-0.1747193, -0.1737154, -0.1657420),
vec3(-0.4496467, -0.4155184, -0.3416573),
vec3(-0.1690202, -0.1703022, -0.1525870),
vec3(-0.0837808, -0.0940454, -0.1027518),
vec3(-0.0319670, -0.0214051, -0.0147691),
vec3( 0.1641816,  0.1377558,  0.1010403),
vec3( 0.3697189,  0.3097930,  0.2029923)
);

SHC tomb = SHC(
vec3( 1.0351604,  0.7603549,  0.7074635),
vec3( 0.4442150,  0.3430402,  0.3403777),
vec3(-0.2247797, -0.1828517, -0.1705181),
vec3( 0.7110400,  0.5423169,  0.5587956),
vec3( 0.6430452,  0.4971454,  0.5156357),
vec3(-0.1150112, -0.0936603, -0.0839287),
vec3(-0.3742487, -0.2755962, -0.2875017),
vec3(-0.1694954, -0.1343096, -0.1335315),
vec3( 0.5515260,  0.4222179,  0.4162488)
);

vec3 sh_light(vec3 normal, SHC l){
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

vec3 gamma(vec3 color){
    return pow(color, vec3(1.0/2.0));
}

out vec4 fragColor;
in vec3 fragCoord;

void main() {
    #if defined(diffuseTextureFlag)
    vec2 v_diffuseTexUV = diffuseUV;
    #endif

    #if defined(emissiveTextureFlag)
    vec2 v_emissiveTexUV = emissiveUV;
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
    fragColor.a = diffuse.a * v_opacity;
    #ifdef alphaTestFlag
    if (fragColor.a <= v_alphaTest)
    discard;
    #endif
    #else
    fragColor.a = 1.0;
    if (diffuse.a <= 0.02) discard;
    #endif

    #ifdef normalFlag
    fragColor = vec4(fragColor.xyz*gamma(sh_light(normal, groove)).r, fragColor.w);
    #endif

    #ifdef fogFlag
    fragColor.rgb = mix(fragColor.rgb, vec3(u_fogColor), v_fog);
    #endif // end fogFlag
}
