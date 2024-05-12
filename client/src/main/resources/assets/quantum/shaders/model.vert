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

attribute MED vec4 a_position;
attribute MED vec4 a_diffuseColor;
attribute MED vec2 a_texCoord0;
attribute MED vec2 a_texCoord1;
uniform mat4 u_projTrans;
varying MED vec4 v_color;
varying MED vec2 v_texCoords;

void main()
{
    v_color = a_position;
    v_color.a = v_color.a * (255.0 / 254.0);
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}

