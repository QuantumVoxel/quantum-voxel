#ifdef GL_ES
precision highp float;
#endif

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#if defined(specularFlag) || defined(fogFlag)
#define cameraPositionFlag
#endif

in vec3 a_position;
uniform mat4 u_projViewTrans;

#if defined(colorFlag)
in vec4 a_color;
#endif // colorFlag

#ifdef normalFlag
in vec3 a_normal;
uniform mat3 u_normalMatrix;
#endif // normalFlag
#endif // normalFlag

#ifdef textureFlag
in vec2 a_texCoord0;
#endif // textureFlag

#ifdef diffuseTextureFlag
uniform vec4 u_diffuseUVTransform;
#endif

#ifdef emissiveTextureFlag
uniform vec4 u_emissiveUVTransform;
#endif

#ifdef specularTextureFlag
uniform vec4 u_specularUVTransform;
#endif

#ifdef boneWeight0Flag
#define boneWeightsFlag
in vec2 a_boneWeight0;
#endif //boneWeight0Flag

#ifdef boneWeight1Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight1;
#endif //boneWeight1Flag

#ifdef boneWeight2Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight2;
#endif //boneWeight2Flag

#ifdef boneWeight3Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight3;
#endif //boneWeight3Flag

#ifdef boneWeight4Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight4;
#endif //boneWeight4Flag

#ifdef boneWeight5Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight5;
#endif //boneWeight5Flag

#ifdef boneWeight6Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight6;
#endif //boneWeight6Flag

#ifdef boneWeight7Flag
#ifndef boneWeightsFlag
#define boneWeightsFlag
#endif
in vec2 a_boneWeight7;
#endif //boneWeight7Flag

#if defined(numBones) && defined(boneWeightsFlag)
#if (numBones > 0)
#define skinningFlag
#endif
#endif

uniform mat4 u_worldTrans;

#if defined(numBones)
#if numBones > 0
uniform mat4 u_bones[numBones];
#endif //numBones
#endif

#ifdef shininessFlag
uniform float u_shininess;
#else
const float u_shininess = 20.0;
#endif // shininessFlag

#ifdef blendedFlag
uniform float u_opacity;

#ifdef alphaTestFlag
uniform float u_alphaTest;
#endif //alphaTestFlag
#endif // blendedFlag

#ifdef lightingFlag
out vec3 v_lightDiffuse;

#ifdef ambientLightFlag
uniform vec3 u_ambientLight;
#endif // ambientLightFlag

#ifdef ambientCubemapFlag
uniform vec3 u_ambientCubemap[6];
#endif // ambientCubemapFlag

#ifdef sphericalHarmonicsFlag
uniform vec3 u_sphericalHarmonics[9];
#endif //sphericalHarmonicsFlag

#ifdef specularFlag
out vec3 v_lightSpecular;
#endif // specularFlag

#ifdef cameraPositionFlag
uniform vec4 u_cameraPosition;
#endif // cameraPositionFlag

#ifdef fogFlag
out float v_fog;
#endif // fogFlag


#if defined(numDirectionalLights) && (numDirectionalLights > 0)
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights

#if defined(numPointLights) && (numPointLights > 0)
struct PointLight
{
	vec3 color;
	vec3 position;
};
uniform PointLight u_pointLights[numPointLights];
#endif // numPointLights

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef shadowMapFlag
uniform mat4 u_shadowMapProjViewTrans;
out vec3 v_shadowMapUv;
#define separateAmbientFlag
#endif //shadowMapFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
out vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif // lightingFlag

out vec2 v_rawUV;
out vec3 v_position;

out VS_OUT {
	vec2 diffuseUV;
	vec2 emissiveUV;
	vec2 specularUV;
	vec4 color;
	float opacity;
	float alphaTest;
} gs_out;

void main() {
	vec2 v_diffuseUV;
	vec2 v_emissiveUV;
	vec2 v_specularUV;
	vec4 v_color;
	float v_opacity;
	float v_alphaTest;

	#ifdef diffuseTextureFlag
	v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;
	v_position = a_position.xyz;
	#endif //diffuseTextureFlag

	#ifdef emissiveTextureFlag
	v_emissiveUV = u_emissiveUVTransform.xy + a_texCoord0 * u_emissiveUVTransform.zw;
	#endif //emissiveTextureFlag

	#if defined(colorFlag)
	v_color = a_color;
	#endif // colorFlag

	#ifdef blendedFlag
	v_opacity = u_opacity;
	#ifdef alphaTestFlag
	v_alphaTest = u_alphaTest;
	#endif //alphaTestFlag
	#endif // blendedFlag

	#ifdef skinningFlag
	vec4 pos = u_worldTrans * skinning * vec4(a_position, 1.0);
	#else
	vec4 pos = u_worldTrans * vec4(a_position, 1.0);
	#endif

	#ifdef fogFlag
	vec3 flen = u_cameraPosition.xyz - pos.xyz;
	float fog = dot(flen, flen) * u_cameraPosition.w;
	v_fog = min(fog, 1.0);
	#endif

	gs_out.diffuseUV = v_diffuseUV;
	gs_out.emissiveUV = v_emissiveUV;
	gs_out.specularUV = v_specularUV;
	gs_out.color = v_color;
	gs_out.opacity = v_opacity;
	gs_out.alphaTest = v_alphaTest;

	gl_Position = u_projViewTrans * pos;
}
