attribute vec4 a_position;
attribute vec4 a_color;
attribute vec4 a_texCoord0;

uniform mat4 u_worldTrans;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec4 v_texCoords;

void main() {
    float scale = length(a_position.xyz);
    vec4 position = vec4(a_position.x * scale, a_position.y * scale, a_position.z, 1.0);
    v_color = a_color;
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * u_worldTrans * position;
}
