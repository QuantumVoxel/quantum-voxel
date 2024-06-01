attribute vec4 a_position;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;

void main() {
    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position.xyz, 1.0);
}
