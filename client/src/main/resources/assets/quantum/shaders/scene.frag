#line 2

#ifdef GL_ES
#define LOW lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOW
#define HIGH
#endif

varying MED vec3 v_normal;
varying MED vec3 v_modelNormal;

varying LOW vec4 v_color;

#if LOD_LEVEL == 0
varying HIGH vec2 v_diffuseUV;
varying HIGH vec2 v_emissiveUV;
varying HIGH vec2 v_normalUV;
varying HIGH vec2 v_specularUV;
#elif LOD_LEVEL >= 2
varying MED vec2 v_diffuseUV;
varying MED vec2 v_emissiveUV;
varying MED vec2 v_normalUV;
varying MED vec2 v_specularUV;
#else
varying MED vec2 v_diffuseUV;
varying MED vec2 v_emissiveUV;
varying MED vec2 v_normalUV;
varying MED vec2 v_specularUV;
#endif
uniform sampler2D u_diffuseTexture;
uniform sampler2D u_emissiveTexture;
uniform sampler2D u_normalTexture;
uniform sampler2D u_specularTexture;
uniform vec4 u_fogColor;
varying MED float v_fog;

#if LOD_LEVEL == 0
varying HIGH vec3 v_position;
#elif LOD_LEVEL == 1
varying HIGH vec3 v_position;
#elif LOD_LEVEL == 2
varying HIGH vec3 v_position;
#elif LOD_LEVEL == 3
varying MED vec3 v_position;
#else
varying MED vec3 v_position;
#endif

uniform mat4 u_modelMatrix;
uniform mat4 u_viewMatrix;
uniform mat4 u_projectionMatrix;

uniform float u_globalSunlight;
uniform vec2 u_atlasSize;
uniform vec2 u_atlasOffset;
uniform float lodThreshold;

struct SHC {
    vec3 L00, L1m1, L10, L11, L2m2, L2m1, L20, L21, L22;
};

SHC groove = SHC(
        vec3(0.3783264, 0.4260425, 0.4504587),
        vec3(0.2887813, 0.3586803, 0.4147053),
        vec3(0.0379030, 0.0295216, 0.0098567),
        vec3(-0.1033028, -0.1031690, -0.0884924),
        vec3(-0.0621750, -0.0554432, -0.0396779),
        vec3(0.0077820, -0.0148312, -0.0471301),
        vec3(-0.0935561, -0.1254260, -0.1525629),
        vec3(-0.0572703, -0.0502192, -0.0363410),
        vec3(0.0203348, -0.0044201, -0.0452180)
    );

SHC beach = SHC(
        vec3(0.6841148, 0.6929004, 0.7069543),
        vec3(0.3173355, 0.3694407, 0.4406839),
        vec3(-0.1747193, -0.1737154, -0.1657420),
        vec3(-0.4496467, -0.4155184, -0.3416573),
        vec3(-0.1690202, -0.1703022, -0.1525870),
        vec3(-0.0837808, -0.0940454, -0.1027518),
        vec3(-0.0319670, -0.0214051, -0.0147691),
        vec3(0.1641816, 0.1377558, 0.1010403),
        vec3(0.3697189, 0.3097930, 0.2029923)
    );

SHC tomb = SHC(
        vec3(1.0351604, 0.7603549, 0.7074635),
        vec3(0.4442150, 0.3430402, 0.3403777),
        vec3(-0.2247797, -0.1828517, -0.1705181),
        vec3(0.7110400, 0.5423169, 0.5587956),
        vec3(0.6430452, 0.4971454, 0.5156357),
        vec3(-0.1150112, -0.0936603, -0.0839287),
        vec3(-0.3742487, -0.2755962, -0.2875017),
        vec3(-0.1694954, -0.1343096, -0.1335315),
        vec3(0.5515260, 0.4222179, 0.4162488)
    );

vec3 sh_light(vec3 normal, SHC l) {
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
        2.0 * C1 * l.L21 * x * z +
        2.0 * C1 * l.L2m1 * y * z +
        2.0 * C2 * l.L11 * x +
        2.0 * C2 * l.L1m1 * y +
        2.0 * C2 * l.L10 * z
    );
}

vec3 gamma(vec3 color) {
    return pow(color, vec3(1.0 / 2.0));
}

vec2 transformUV(vec2 uv) {
    int blockWidth = 16;
    int blocksPerRow = int(u_atlasSize.x) / blockWidth;
    float uPerBlock = float(blockWidth) / float(u_atlasSize.x);
    float vPerBlock = float(blockWidth) / float(u_atlasSize.y);

    vec3 texGetNormal = -abs(v_modelNormal);
    vec2 uvMult = fract(vec2(dot(texGetNormal.zxy, v_position),
                dot(texGetNormal.yzx, v_position)));
    vec2 uvStart = uv;
    vec2 v_texUV;
    if (v_modelNormal.x != 0.0) {
        v_texUV = uvStart / u_atlasOffset + vec2(vPerBlock * uvMult.y, uPerBlock * uvMult.x);
    } else {
        v_texUV = uvStart / u_atlasOffset + vec2(uPerBlock * uvMult.x, vPerBlock * uvMult.y);
    }
    return v_texUV;
}

void main() {
    vec2 v_diffuseTexUV = transformUV(v_diffuseUV);
    vec2 v_emissiveTexUV = transformUV(v_emissiveUV);

    vec3 normal = v_normal;

    float sunLight = v_color.a;
    vec4 blockLight = vec4(v_color.rgb, 1.0);

    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseTexUV);
    #if LOD_LEVEL < 1
    if (diffuse.a <= 0.01) discard;
    #endif
    gl_FragColor.a = 1.0;

    #if LOD_LEVEL < 2
    vec3 light = vec3(u_globalSunlight) * sunLight;
    light += blockLight.rgb * (1.0 - light);
    #else
    vec3 light = vec3(u_globalSunlight);
    #endif

    vec3 emissive = vec3(0.0);
    #if LOD_LEVEL < 1
    emissive = texture2D(u_emissiveTexture, v_emissiveTexUV).rgb;
    gl_FragColor.rgb = (diffuse.rgb) * light + (emissive * (1.0 - light));
    #else
    gl_FragColor.rgb = (diffuse.rgb) * light;
    #endif

    #if LOD_LEVEL < 2
    gl_FragColor = vec4(gl_FragColor.xyz * gamma(sh_light(v_normal, groove)).r, gl_FragColor.w);
    #endif
    gl_FragColor.rgb = mix(gl_FragColor.rgb, vec3(u_fogColor), v_fog);
}
