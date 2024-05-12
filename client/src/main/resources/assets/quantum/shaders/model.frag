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

varying MED vec4 v_color;
varying MED vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_ssaoMap;
uniform MED vec2 u_resolution;// Screen resolution

void main() {
    MED vec2 texCoord = v_texCoords.xy;

    // Sample texture
    MED vec4 diffuse = texture2D(u_texture, texCoord).rgba;

    gl_FragColor = diffuse;
}