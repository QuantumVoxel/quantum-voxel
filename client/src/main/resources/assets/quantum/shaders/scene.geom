#line 1

layout(triangles) in;
layout(triangle_strip, max_vertices=3) out;

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

vec3 GetNormal() {
    vec3 a = vec3(gl_in[0].gl_Position) - vec3(gl_in[1].gl_Position);
    vec3 b = vec3(gl_in[2].gl_Position) - vec3(gl_in[1].gl_Position);
    return normalize(cross(a, b));
}

void main( void )
{
    vec3 N = GetNormal();

    for( int i=0; i<gl_in.length( ); ++i )
    {
        gl_Position = gl_in[i].gl_Position;
        v_normal = normalize(u_normalMatrix * mat3(transpose(u_projViewWorldTrans)) * N);

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
