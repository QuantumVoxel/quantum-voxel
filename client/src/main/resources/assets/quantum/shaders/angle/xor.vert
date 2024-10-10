#ifdef GL_ES
precision highp float;
precision highp int;
#endif

in vec4 a_position;
in vec2 a_texCoord0;
uniform mat4 u_projTrans;
out vec2 v_texCoord0;

void main() {
    v_texCoord0 = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}