#ifdef GL_ES
precision mediump float;
#endif

// Attribs
attribute vec3 a_position;

// Uniforms
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;
uniform float u_lineWidth = 6.0;

// Outputs
varying vec4 v_col;
varying float v_line_width;

void main() {
    gl_Position = u_projViewTrans * (u_worldTrans * vec4(a_position, 1.0));

    v_col = vec4(0.0, 0.0, 0.0, 1.0);
    v_line_width = u_lineWidth;
}