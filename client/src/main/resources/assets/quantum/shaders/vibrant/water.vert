#line 1

#ifdef GL_ES
precision highp float;
#endif

in vec3 a_position;
uniform mat4 u_projViewTrans;

in vec4 a_color;
in vec3 a_normal;
uniform ivec3 u_chunkPosition;
uniform mat3 u_normalMatrix;

in vec2 a_texCoord0;

uniform vec4 u_diffuseUVTransform;
uniform vec4 u_emissiveUVTransform;
uniform mat4 u_worldTrans;
uniform vec4 u_cameraPosition;
uniform mat4 u_projTrans;
uniform mat4 u_modelView;
uniform float u_time;

out vec3 v_normal;
out vec3 v_modelNormal;
out vec2 v_diffuseUV;
out vec2 v_emissiveUV;
out vec4 v_color;
out vec3 v_position;
out float v_fog;

// === Value noise ===
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) +
           (c - a) * u.y * (1.0 - u.x) +
           (d - b) * u.x * u.y;
}

// === Fractal Brownian Motion ===
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p * frequency);
        frequency *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

// === Wave height function ===
float getWaveHeight(vec2 worldXZ, float time) {
    float waveStrength = 0.02;
    float waveSpeed    = 1.5;
    float waveFrequency = 2.0;

    float phaseX = worldXZ.x * waveFrequency + time * waveSpeed;
    float phaseZ = worldXZ.y * waveFrequency + time * waveSpeed;

    float baseWave  = sin(phaseX) + cos(phaseZ);
    float noiseWave = fbm(worldXZ * 0.2 + vec2(time * 0.1, time * 0.15));

    return (baseWave * 0.5 + noiseWave * 1.2) * waveStrength - waveStrength - 0.125;
}

void main() {
    v_diffuseUV = u_diffuseUVTransform.xy + a_texCoord0 * u_diffuseUVTransform.zw;

    vec3 position = a_position;
    float worldX = float(u_chunkPosition.x) * 16.0 + position.x;
    float worldZ = float(u_chunkPosition.z) * 16.0 + position.z;
    vec2 worldXZ = vec2(worldX, worldZ);

    float waveHeight = getWaveHeight(worldXZ, u_time);

    // === Apply displacement depending on face type ===
    float blockBottom = 0.0;
    float blockTop    = 1.0;

    if (a_normal.y > 0.9) {
        // Top face → fully wave
        position.y = blockTop + waveHeight;
    } else if (abs(a_normal.y) < 0.001) {
        // Side face → interpolate displacement between bottom and top
        float t = (position.y - blockBottom) / (blockTop - blockBottom);
        position.y = mix(blockBottom, blockTop + waveHeight, t);
    }
    // Bottom face → unchanged

    v_position = position;
    v_emissiveUV = u_emissiveUVTransform.xy + a_texCoord0 * u_emissiveUVTransform.zw;
    v_color = a_color;

    #ifdef alphaTestFlag
        v_alphaTest = u_alphaTest;
    #endif

    vec4 pos = u_worldTrans * vec4(position, 1.0);

    vec3 flen = u_cameraPosition.xyz - pos.xyz;
    float fog = dot(flen, flen) * u_cameraPosition.w;
    v_fog = min(fog, 1.0);

    // === Normals ===
    vec3 waveNormal;
    if (a_normal.y > 0.9) {
        // Top normals follow wave surface
        float eps = 0.1;
        float hL = getWaveHeight(worldXZ - vec2(eps, 0.0), u_time);
        float hR = getWaveHeight(worldXZ + vec2(eps, 0.0), u_time);
        float hD = getWaveHeight(worldXZ - vec2(0.0, eps), u_time);
        float hU = getWaveHeight(worldXZ + vec2(0.0, eps), u_time);

        waveNormal = normalize(vec3(hL - hR, 2.0 * eps, hD - hU));
    } else {
        // Side & bottom keep original normals
        waveNormal = a_normal;
    }

    v_normal = normalize(u_normalMatrix * waveNormal);
    v_modelNormal = waveNormal;

    gl_Position = u_projViewTrans * pos;
}
