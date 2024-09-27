#version 320 es

layout(lines) in;
layout(line_strip, max_vertices=4) out;

uniform mat4 u_projViewWorldTrans;
uniform mat3 u_normalMatrix;
uniform float u_lodThreshold;

in VS_OUT {
    vec3 normal;
    vec2 diffuseUV;
    vec2 emissiveUV;
    vec4 color;
    vec3 position;
    float fog;
} gs_in[];

out vec3 v_normal;
out vec3 v_modelNormal;
out vec2 v_diffuseUV;
out vec2 v_emissiveUV;
out vec4 v_color;
out vec3 v_position;
out float v_fog;

void main( void )
{
    vec3 N = vec3(0.0, 0.0, 1.0);

    for( int i=0; i<gl_in.length( ); ++i )
    {
        gl_Position = gl_in[i].gl_Position;
        v_normal = N;

        v_modelNormal = gs_in[i].normal;
        v_diffuseUV = gs_in[i].diffuseUV;
        v_emissiveUV = gs_in[i].emissiveUV;
        v_color = gs_in[i].color;
        v_position = gs_in[i].position;
        v_fog = gs_in[i].fog;

        EmitVertex( );
    }

    EndPrimitive( );
}
