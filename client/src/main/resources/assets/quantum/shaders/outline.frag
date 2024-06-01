#ifdef GL_ES
precision mediump float;
#endif

out vec4 fragColor;

uniform vec2 u_resolution;

uniform sampler2D u_texture;

const float OUTLINE_WIDTH = 0.1;

void main() {
    vec2 uv = gl_FragCoord.xy / textureSize(u_texture, 0).xy;
    float dist = distance(uv, vec2(0.5));
    float alpha = smoothstep(0.5 - OUTLINE_WIDTH, 0.5 + OUTLINE_WIDTH, dist);
    fragColor = vec4(0.0, 0.0, 0.0, alpha);

    if (alpha == 0.0) {
        discard;
    }
}
