layout(triangles) in;
layout(triangle_strip, max_vertices=3) out;

uniform mat4 u_projViewWorldTrans;
uniform mat3 u_normalMatrix;

in VS_OUT {
    vec2 diffuseUV;
    vec2 emissiveUV;
    vec2 specularUV;
    vec4 color;
    float opacity;
    float alphaTest;
} gs_in[];

out vec3 v_normal;
out vec2 v_diffuseUV;
out vec2 v_emissiveUV;
out vec2 v_specularUV;
out vec4 v_color;
out float v_opacity;
out float v_alphaTest;

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
        v_normal = normalize(mat3(transpose(u_projViewWorldTrans)) * N);

        v_diffuseUV = gs_in[i].diffuseUV;
        v_emissiveUV = gs_in[i].emissiveUV;
        v_specularUV = gs_in[i].specularUV;
        v_color = gs_in[i].color;
        v_opacity = gs_in[i].opacity;
        v_alphaTest = gs_in[i].alphaTest;

        EmitVertex( );
    }

    EndPrimitive( );
}
