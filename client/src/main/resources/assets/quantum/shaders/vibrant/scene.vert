#line 1

#ifdef GL_ES
precision highp float;
#endif

in vec3 a_position;
uniform mat4 u_projViewTrans;

in vec4 a_color;
in vec3 a_normal;
uniform mat3 u_normalTrans;

in vec2 a_texCoord0;

uniform vec4 u_diffuseUVTransform;
uniform vec4 u_emissiveUVTransform;
uniform mat4 u_worldTrans;
uniform vec4 u_cameraPosition;

out vec3 v_normal;
out vec3 v_modelNormal;
out vec2 v_diffuseUV;
out vec2 v_emissiveUV;
out vec4 v_color;
out vec3 v_position;
out float v_fog;

void main() {
	v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
	v_position = a_position.xyz;

	v_emissiveUV = u_emissiveUVTransform.xy + a_texCoord0 * u_emissiveUVTransform.zw;

	v_color = a_color;

	#ifdef alphaTestFlag
		v_alphaTest = u_alphaTest;
	#endif //alphaTestFlag
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);

	vec3 flen = u_cameraPosition.xyz - pos.xyz;
	float fog = dot(flen, flen) * u_cameraPosition.w;
	v_fog = min(fog, 1.0);
	v_normal = a_normal;
	v_modelNormal = a_normal;

	gl_Position = u_projViewTrans * pos;
}
