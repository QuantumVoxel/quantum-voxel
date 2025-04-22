
#ifdef GL_ES
precision mediump float;
#endif

varying vec3 v_position;

uniform vec4 u_topColor; // = vec3(0.5, 0.64, 0.985);
uniform vec4 u_midColor; // = vec3(0.75, 0.825, 0.945);
uniform vec4 u_bottomColor; // = vec3(0.75, 0.825, 0.945);

varying vec3 fragCoord;

void main() {
    // Normalize the position to getConfig values between 0 and 1
    vec3 normalizedPosition = normalize(v_position.xyz);
    normalizedPosition.y *= 7.0;
    normalizedPosition.y += 0.5;
    normalizedPosition.y = clamp(normalizedPosition.y, 0.0, 1.0);

    // Calculate the gradient
    vec3 gradient = mix(u_bottomColor.rgb, u_midColor.rgb, normalizedPosition.y);
    gradient = mix(gradient, u_topColor.rgb, normalizedPosition.y * normalizedPosition.y);

    // Output the color
    gl_FragColor = vec4(gradient, 1.0);
}