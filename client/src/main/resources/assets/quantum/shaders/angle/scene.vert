#version 320 es

#ifdef GL_ES
precision highp float;
#endif

in vec3 a_position;
uniform mat4 u_projViewTrans;

in vec4 a_color;
in vec3 a_normal;
uniform mat3 u_normalMatrix;

in vec2 a_texCoord0;

uniform vec4 u_diffuseUVTransform;
uniform vec4 u_emissiveUVTransform;
uniform mat4 u_worldTrans;
uniform vec4 u_cameraPosition;

out VS_OUT {
	vec3 normal;
	vec2 diffuseUV;
	vec2 emissiveUV;
	vec4 color;
	vec3 position;
	float fog;
} gs_out;

void main() {
	vec2 v_diffuseUV;
	vec2 v_emissiveUV;
	vec4 v_color;
	vec3 v_position;
	float v_fog;

	v_diffuseUV = a_texCoord0;
	v_position = a_position.xyz;

	v_emissiveUV = a_texCoord0;

	v_color = a_color;

	#ifdef alphaTestFlag
		v_alphaTest = u_alphaTest;
	#endif //alphaTestFlag
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);

	vec3 flen = u_cameraPosition.xyz - pos.xyz;
	float fog = dot(flen, flen) * u_cameraPosition.w;
	v_fog = min(fog, 1.0);

	gs_out.diffuseUV = v_diffuseUV;
	gs_out.emissiveUV = v_emissiveUV;
	gs_out.color = v_color;
    gs_out.position = v_position;
	gs_out.normal = a_normal;
	gs_out.fog = v_fog;

	gl_Position = u_projViewTrans * pos;
}
