
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

varying HIGH vec3 v_normal;
varying HIGH vec3 v_modelNormal;

varying LOWP vec4 v_color;

varying HIGH vec2 v_diffuseUV;
varying HIGH vec2 v_emissiveUV;
varying HIGH vec2 v_normalUV;
varying HIGH vec2 v_specularUV;
uniform sampler2D u_diffuseTexture;
uniform sampler2D u_emissiveTexture;
uniform sampler2D u_normalTexture;
uniform sampler2D u_specularTexture;
uniform MED vec4 u_fogColor;
varying LOWP float v_fog;
varying HIGH vec3 v_position;

uniform LOWP float u_globalSunlight;
uniform HIGH vec2 u_atlasSize;
uniform HIGH vec2 u_atlasOffset;
uniform MED float lodThreshold;

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

vec2 transformUV(vec2 uv) {
    int blockWidth = 16;
    int blocksPerRow = int(u_atlasSize.x)/blockWidth;
    float uPerBlock = float(blockWidth) / float(u_atlasSize.x);
    float vPerBlock = float(blockWidth) / float(u_atlasSize.y);

    vec3 texGetNormal = -abs(v_modelNormal);
    vec2 uvMult = fract(vec2(dot(texGetNormal.zxy, v_position),
    dot(texGetNormal.yzx, v_position)));
    vec2 uvStart = uv;
    vec2 v_texUV;
    if(v_modelNormal.x != 0.0) {
        v_texUV = uvStart / u_atlasOffset + vec2(vPerBlock*uvMult.y, uPerBlock*uvMult.x);
    } else {
        v_texUV = uvStart / u_atlasOffset + vec2(uPerBlock*uvMult.x, vPerBlock*uvMult.y);
    }
    return v_texUV;
}

void main() {
    vec2 v_diffuseTexUV = transformUV(v_diffuseUV);
    vec2 v_emissiveTexUV = transformUV(v_emissiveUV);

    vec3 normal = v_normal;

    float depth = gl_FragCoord.z / gl_FragCoord.w;

    float sunLight = v_color.a;
    vec4 blockLight = vec4(v_color.rgb, 1.0);

    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseTexUV);
    //    if (depth > lodThreshold) gl_FragColor.a = 1.0;
    //    else {
    //        #ifdef blendedFlag
    //            gl_FragColor.a = diffuse.a;
    //        #else
    if (diffuse.a <= 0.01) discard;
    gl_FragColor.a = 1.0;
    //        #endif
    //    }

    vec3 light = vec3(u_globalSunlight);
    //    light *= sunLight * 2 - 0.4;
    //    light += (blockLight.rgb - (light * blockLight.rgb));

    vec3 emissive;
    if (depth > lodThreshold) emissive = texture2D(u_emissiveTexture, v_emissiveTexUV).rgb;
    else emissive = vec3(0.0);
    gl_FragColor.rgb = (diffuse.rgb) * light + (emissive * (1.0 - light));

    vec3 depthIn3Channels;
    depthIn3Channels.r = mod(depth, 1.0);
    depth -= depthIn3Channels.r;
    depth /= 256.0;

    depthIn3Channels.g = mod(depth, 1.0);
    depth -= depthIn3Channels.g;
    depth /= 256.0;

    depthIn3Channels.b = depth;

    gl_FragColor = vec4(gl_FragColor.xyz*gamma(sh_light(v_normal, groove)), gl_FragColor.w);
    gl_FragColor.rgb = mix(gl_FragColor.rgb, vec3(u_fogColor), v_fog);
}
