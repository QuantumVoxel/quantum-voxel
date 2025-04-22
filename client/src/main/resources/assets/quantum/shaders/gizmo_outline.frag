
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

varying vec3 v_normal;
varying vec3 v_modelNormal;

varying vec4 v_color;

varying MED vec2 v_diffuseUV;
varying MED vec2 v_emissiveUV;
varying MED vec2 v_normalUV;
varying MED vec2 v_specularUV;
uniform vec4 u_color;
uniform vec4 u_fogColor;

uniform sampler2D u_framebuffer;

varying float v_fog;
varying vec3 v_position;

uniform float u_globalSunlight;
uniform vec2 u_atlasSize;
uniform vec2 u_atlasOffset;
uniform float lodThreshold;


void main() {
    vec4 diffuse = u_color;

    // Blend with gl_FragColor if it's not opaque
    // GL_ONE, GL_ONE_MINUS_SRC_ALPHA
    gl_FragColor = diffuse;
}
