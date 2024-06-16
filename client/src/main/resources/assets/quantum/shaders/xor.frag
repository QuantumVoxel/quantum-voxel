#ifdef GL_ES
precision highp float;
precision highp int;
precision lowp sampler2D;
#endif

uniform mat4 u_projTrans;
uniform sampler2D u_texture;
in vec2 v_texCoord0;

out vec4 fragColor;
in vec3 fragCoord;

void main() {
    vec4 color = texture(u_texture, v_texCoord0);

    // XOR RGB components with alpha component
    color.rgb = color.rgb * color.a;
    color.rgb = vec3(1.0) - color.rgb;

    fragColor = color;
}