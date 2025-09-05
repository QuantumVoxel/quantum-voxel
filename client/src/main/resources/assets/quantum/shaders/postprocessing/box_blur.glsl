#version 330 core

uniform sampler2D colorTexture;

uniform vec2 parameters;

out vec4 fragColor;

void main() {
    vec2 texSize  = textureSize(colorTexture, 0).xy;
    vec2 texCoord = gl_FragCoord.xy / texSize;

    fragColor = texture(colorTexture, texCoord);

    int size = int(parameters.x);
    if (size <= 0) { return; }

    float separation = parameters.y;
          separation = max(separation, 1);

    fragColor.rgb = vec3(0);

    float count = 0.0;

    for (int i = -size; i <= size; ++i) {
        for (int j = -size; j <= size; ++j) {
            vec4 color = texture(colorTexture, (gl_FragCoord.xy + (vec2(i, j) * separation)) / texSize);
            if (color.a == 0.0) continue;
            fragColor.rgb += color.rgb;

            count += 1.0;
        }
    }

    fragColor.rgb /= count;
}