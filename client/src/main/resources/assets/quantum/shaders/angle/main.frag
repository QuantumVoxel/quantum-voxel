
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_texture;
void main()
{
    gl_gl_FragColor = texture2D(u_texture, v_texCoords) * 2.0;
}