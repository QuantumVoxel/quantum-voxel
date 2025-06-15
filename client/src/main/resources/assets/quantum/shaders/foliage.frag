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

void main() {
    gl_FragColor.a = 1.0;

    vec2 v_diffuseTexUV = v_diffuseUV;
    vec2 v_emissiveTexUV = v_emissiveUV;

    vec3 normal = v_normal;

    float sunLight = v_color.a;
    vec4 blockLight = vec4(v_color.b);
    float ao = v_color.g;

    vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseTexUV);
    #if LOD_LEVEL < 1
    if (diffuse.a <= 0.5) discard;
    #endif

    #if LOD_LEVEL < 2
    vec3 light = vec3(2.0-u_globalSunlight) * sunLight;
    light += blockLight.rgb * (1.0 - light);
    #else
    vec3 light = vec3(u_globalSunlight);
    #endif

    light *= ao;

    vec3 emissive = vec3(0.0);
    #if LOD_LEVEL < 1
    emissive = texture2D(u_emissiveTexture, v_emissiveTexUV).rgb;
    gl_FragColor.rgb = (diffuse.rgb) * light + (emissive * (1.0 - light));
    #else
    gl_FragColor.rgb = (diffuse.rgb) * light;
    #endif

    gl_FragColor.rgb = mix(gl_FragColor.rgb, vec3(u_fogColor), v_fog);
}
