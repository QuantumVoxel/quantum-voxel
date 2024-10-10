#version 320 es

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
uniform vec4 u_color;
uniform vec4 u_fogColor;

uniform sampler2D u_framebuffer;

in float v_fog;
in vec3 v_position;

uniform float u_globalSunlight;
uniform vec2 u_atlasSize;
uniform vec2 u_atlasOffset;
uniform float lodThreshold;

layout(location = 0) out vec4 diffuseOut;

void main() {
    vec4 diffuse = u_color;

    // Blend with diffuseOut if it's not opaque
    // GL_ONE, GL_ONE_MINUS_SRC_ALPHA
    diffuseOut = diffuse;
}
