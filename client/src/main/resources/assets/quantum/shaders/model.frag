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

in MED vec4 v_color;
in MED vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_ssaoMap;
uniform MED vec2 u_resolution;// Screen resolution

out vec4 fragColor;
in vec3 fragCoord;

void main() {
    MED vec2 texCoord = v_texCoords.xy;

    // Sample texture
    MED vec4 diffuse = texture(u_texture, texCoord).rgba;

    fragColor = diffuse;
}