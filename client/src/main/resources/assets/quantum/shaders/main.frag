#version 330 core

in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform sampler2D uPosition;
uniform sampler2D uNormal;
uniform sampler2D uDiffuse;
uniform sampler2D uReflective;
uniform mat4 view;
uniform mat4 projection;
uniform vec2 screenSize;
uniform float maxDistance;
uniform float thickness;
uniform float resolution;

void main()
{
    vec2 texCoord = gl_FragCoord.xy / screenSize;

    float reflective = texture(uReflective, texCoord).r;
    if (reflective < 0.5) {
        vec3 diffuse = texture(uDiffuse, texCoord).rgb;
        FragColor = vec4(diffuse, 1.0);
        return;
    }

    vec3 positionFrom = texture(uPosition, texCoord).xyz;
    vec3 unitPositionFrom = normalize(positionFrom);
    vec3 normal = normalize(texture(uNormal, texCoord).xyz);
    vec3 pivot = normalize(reflect(unitPositionFrom, normal));

    vec4 startView = vec4(positionFrom + (pivot * 0), 1.0);
    vec4 endView = vec4(positionFrom + (pivot * maxDistance), 1.0);

    vec4 startFrag = projection * view * startView;
    startFrag.xyz /= startFrag.w;
    startFrag.xy = startFrag.xy * 0.5 + 0.5;
    startFrag.xy *= screenSize;

    vec4 endFrag = projection * view * endView;
    endFrag.xyz /= endFrag.w;
    endFrag.xy = endFrag.xy * 0.5 + 0.5;
    endFrag.xy *= screenSize;

    vec2 frag = startFrag.xy;
    vec2 uv = frag / screenSize;

    float deltaX = endFrag.x - startFrag.x;
    float deltaY = endFrag.y - startFrag.y;

    float useX = abs(deltaX) >= abs(deltaY) ? 1.0 : 0.0;
    float delta = mix(abs(deltaY), abs(deltaX), useX) * clamp(resolution, 0.0, 1.0);

    vec2 increment = vec2(deltaX, deltaY) / max(delta, 0.001);

    float search0 = 0.0;
    float search1 = 0.0;
    int hit0 = 0;
    int hit1 = 0;
    float viewDistance = startView.y;
    float depth = thickness;

    for (int i = 0; i < int(delta); ++i) {
        frag += increment;
        uv.xy = frag / screenSize;
        vec3 positionTo = texture(uPosition, uv.xy).xyz;

        search1 = mix((frag.y - startFrag.y) / deltaY, (frag.x - startFrag.x) / deltaX, useX);

        viewDistance = (startView.y * endView.y) / mix(endView.y, startView.y, search1);
        depth = viewDistance - positionTo.y;

        if (depth > 0.0 && depth < thickness) {
            hit0 = 1;
            break;
        } else {
            search0 = search1;
        }
    }

    search1 = search0 + ((search1 - search0) / 2.0);
    int steps = int(delta) * hit0;

    for (int i = 0; i < steps; ++i) {
        frag = mix(startFrag.xy, endFrag.xy, search1);
        uv.xy = frag / screenSize;
        vec3 positionTo = texture(uPosition, uv.xy).xyz;

        viewDistance = (startView.y * endView.y) / mix(endView.y, startView.y, search1);
        depth = viewDistance - positionTo.y;

        if (depth > 0.0 && depth < thickness) {
            hit1 = 1;
            search1 = search0 + ((search1 - search0) / 2.0);
        } else {
            float temp = search1;
            search1 = search1 + ((search1 - search0) / 2.0);
            search0 = temp;
        }
    }

    float visibility = hit1;
    vec4 reflectionColor = texture(uDiffuse, texCoord.xy) * visibility;
    FragColor = reflectionColor;
}
