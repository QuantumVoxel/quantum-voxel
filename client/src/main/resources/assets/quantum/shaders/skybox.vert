#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_position;
attribute vec3 a_position;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

void main() {
    vec4 pos = u_worldTrans * vec4(a_position, 1.0);

    gl_Position = u_projViewTrans * pos;
    v_position = a_position;
}
