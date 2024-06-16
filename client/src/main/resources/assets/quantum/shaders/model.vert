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

in MED vec4 a_position;
in MED vec4 a_diffuseColor;
in MED vec2 a_texCoord0;
in MED vec2 a_texCoord1;
uniform mat4 u_projTrans;
out MED vec4 v_color;
out MED vec2 v_texCoords;

void main()
{
    v_color = a_position;
    v_color.a = v_color.a * (255.0 / 254.0);
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}

