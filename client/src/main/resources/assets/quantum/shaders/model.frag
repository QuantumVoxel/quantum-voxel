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

in vec3 v_normal;
in vec3 v_modelNormal;

in vec4 v_color;

in MED vec2 v_diffuseUV;
in MED vec2 v_emissiveUV;
in MED vec2 v_normalUV;
in MED vec2 v_specularUV;
in MED vec2 v_reflectiveUV;
uniform sampler2D u_diffuseTexture;
uniform sampler2D u_emissiveTexture;
uniform sampler2D u_normalTexture;
uniform sampler2D u_specularTexture;
uniform sampler2D u_reflectiveTexture;
uniform vec4 u_fogColor;
in float v_fog;
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

layout(location = 0) out vec4 diffuseOut;
layout(location = 1) out vec3 reflectiveOut;
layout(location = 2) out vec3 depthOut;
layout(location = 3) out vec3 positionOut;
layout(location = 4) out vec3 normalOut;
layout(location = 5) out vec4 specularOut;

void main() {
    vec2 v_diffuseTexUV = v_diffuseUV;
    vec2 v_emissiveTexUV = v_emissiveUV;
    vec2 v_reflectiveTexUV = v_reflectiveUV;

    vec3 normal = v_normal;

//    float sunLight = v_color.a;
//    vec4 blockLight = vec4(v_color.rgb, 1.0);

    vec4 diffuse = texture(u_diffuseTexture, v_diffuseTexUV);
    diffuseOut.a = 1.0;
    #ifdef blendedFlag
    diffuseOut.a = diffuse.a;
    #endif
    if (diffuse.a <= 0.02) discard;

    vec3 light = vec3(u_globalSunlight);
//    light *= sunLight * 2 - 0.4;
//    light += (blockLight.rgb - (light * blockLight.rgb));

    vec3 emissive = texture(u_emissiveTexture, v_emissiveTexUV).rgb;
    vec4 reflective = texture(u_reflectiveTexture, v_reflectiveTexUV);
    diffuseOut.rgb = (diffuse.rgb) * light + (emissive * (1.0 - light));

    float depth = gl_FragCoord.z / gl_FragCoord.w;

    vec3 depthIn3Channels;
    depthIn3Channels.r = mod(depth, 1.0);
    depth -= depthIn3Channels.r;
    depth /= 256.0;

    depthIn3Channels.g = mod(depth, 1.0);
    depth -= depthIn3Channels.g;
    depth /= 256.0;

    depthIn3Channels.b = depth;

    diffuseOut = vec4(diffuseOut.xyz*gamma(sh_light(v_normal, groove)), diffuseOut.w);
    diffuseOut.rgb = mix(diffuseOut.rgb, vec3(u_fogColor), v_fog);
    positionOut = v_position;
    normalOut = normal;
    reflectiveOut = vec3(1.0 - reflective.a);
    specularOut = vec4(0.0, 0.0, 0.0, 0.0);
    depthOut = depthIn3Channels;
}
