#version 330 core

out vec4 FragColor;

in vec2 v_texCoords;

uniform sampler2D colorTexture;
uniform sampler2D refTexture;
uniform sampler2D maskTexture;

void main(){
	vec3 baseColor = texture(colorTexture, v_texCoords).rgb;
	//vec3 ks = texture(specularTexture, v_texCoords).rgb;
    // reflection factor from material
    float reflectionStrength = texture(maskTexture, v_texCoords).g;
    vec4 reflectionColor = texture(refTexture, v_texCoords);
    if(reflectionColor.a < 1.0){
        FragColor = vec4(baseColor, 1);
        return;
    }

	vec3 finalColor = mix(baseColor, reflectionColor.rgb, 0.6);
	FragColor = vec4(finalColor, 1);
}