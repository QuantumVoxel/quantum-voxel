#version 410

#ifdef GL_ES
precision mediump float;
#endif

in vec3 v_position;

uniform vec4 u_topColor; // = vec3(0.5, 0.64, 0.985);
uniform vec4 u_midColor; // = vec3(0.75, 0.825, 0.945);
uniform vec4 u_bottomColor; // = vec3(0.75, 0.825, 0.945);

uniform vec4 u_posZColor;
uniform vec4 u_negZColor;

out vec4 fragColor;
in vec3 fragCoord;

void main() {
    // Normalize the position to getConfig values between 0 and 1
    vec3 normalizedPosition = normalize(v_position.xyz);
    normalizedPosition.y *= 7.0;
    normalizedPosition.y += 0.5;
    normalizedPosition.y = clamp(normalizedPosition.y, 0.0, 1.0);

    // Calculate the gradient
    vec3 gradient = mix(u_bottomColor.rgb, u_midColor.rgb, normalizedPosition.y);
    gradient = mix(gradient, u_topColor.rgb, normalizedPosition.y * normalizedPosition.y);

    // Sunrise
    gradient = mix(gradient, u_posZColor.rgb, clamp((1.0 - normalizedPosition.z - 0.75), 0.0, 1.0) * u_posZColor.a);

    // Sunset
    gradient = mix(gradient, u_negZColor.rgb, clamp((normalizedPosition.z + 0.25), 0.0, 1.0) * u_negZColor.a);

    // Output the color
    fragColor = vec4(normalizedPosition * 2, 1.0);
}