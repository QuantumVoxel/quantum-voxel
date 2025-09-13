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

#ifndef LOD_LEVEL
#define LOD_LEVEL 0
#endif

uniform mat4 u_projTrans;
uniform sampler2D u_texture;
varying vec2 v_texCoord0;

varying vec3 fragCoord;

void main() {
    vec4 color = texture2D(u_texture, v_texCoord0);

    // XOR RGB components with alpha component
    color.rgb = color.rgb * color.a;
    color.rgb = vec3(1.0) - color.rgb;

    gl_FragColor = color;
}