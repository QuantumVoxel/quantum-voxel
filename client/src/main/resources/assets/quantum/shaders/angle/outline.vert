
#ifdef GL_ES
precision highp float;
#endif

// Attribs
attribute vec3 a_position;

// Uniforms
uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

// Outputs
varying vec4 v_col;
varying float v_line_width;

void main() {
    gl_Position = u_projViewTrans * (u_worldTrans * vec4(a_position, 1.0));

    v_col = vec4(0.0, 0.0, 0.0, 1.0);
    v_line_width = 6.0;
}