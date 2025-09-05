#version 330 core

out vec4 reflectionColor;

uniform sampler2D gNormal;
uniform sampler2D colorBuffer;
uniform sampler2D depthMap;
uniform sampler2D gReflection; // material reflection factor (0–1)

uniform float SCR_WIDTH;
uniform float SCR_HEIGHT;
uniform mat4 invProjection;
uniform mat4 invViewMatrix;
uniform mat4 projection;

// sky gradient colors
uniform vec4 topColor;
uniform vec4 midColor;
uniform vec4 bottomColor;

bool rayIsOutofScreen(vec2 ray){
    return (ray.x > 1.0 || ray.y > 1.0 || ray.x < 0.0 || ray.y < 0.0);
}

// hash-based random generator for jitter
float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

// gradient function from your sky shader
vec3 getSkyGradient(vec3 dir){
    // normalize direction
    vec3 normalizedPosition = normalize(dir);
    normalizedPosition.y *= 7.0;
    normalizedPosition.y += 0.5;
    normalizedPosition.y = clamp(normalizedPosition.y, 0.0, 1.0);

    // gradient blend
    vec3 gradient = mix(bottomColor.rgb, midColor.rgb, normalizedPosition.y);
    gradient = mix(gradient, topColor.rgb, normalizedPosition.y * normalizedPosition.y);
    return gradient;
}

vec3 TraceRay(vec3 rayPos, vec3 dir, int iterationCount, float stepSize, out bool hitSomething){
    float sampleDepth;
    vec3 hitColor = vec3(0);
    hitSomething = false;

    // minimum step to avoid self-intersection
    const float minStep = 1.0 / 200.0;
    rayPos += dir * minStep;

    for(int i = 0; i < iterationCount; i++){
        rayPos += dir * stepSize;

        if(rayIsOutofScreen(rayPos.xy)){
            break;
        }

        sampleDepth = texture(depthMap, rayPos.xy).r;
        float depthDif = rayPos.z - sampleDepth;

        // looser epsilon for smoother hits
        if(depthDif >= 0.0 && depthDif < 0.01){
            hitColor = texture(colorBuffer, rayPos.xy).rgb;
            hitSomething = true;
            break;
        }
    }
    return hitColor;
}

void main(){
    float maxRayDistance = 100.0f;

    // current pixel coords
    vec2 uv = vec2(gl_FragCoord.x / SCR_WIDTH, gl_FragCoord.y / SCR_HEIGHT);

    // reflection factor from material
    float reflectionStrength = texture(gReflection, uv).r;
    if(reflectionStrength <= 0.001){
        reflectionColor = vec4(0,0,0,0);
        return;
    }

    // normal + depth
    vec3 normalView = texture(gNormal, uv).rgb;
    float pixelDepth = texture(depthMap, uv).r;
    vec3 pixelPositionTexture = vec3(uv, pixelDepth);

    // reconstruct view position
    vec4 positionView = invProjection * vec4(pixelPositionTexture * 2.0 - vec3(1.0), 1.0);
    positionView /= positionView.w;

    // reflection vector with jitter for gloss
    vec3 reflectionView = normalize(reflect(positionView.xyz, normalView));

    // add small jitter
    vec3 glossyOffset = normalize(vec3(
        rand(uv * 12.3),
        rand(uv * 45.7),
        0.0
    )) * 0.08; // tweak glossiness strength
    reflectionView = normalize(reflectionView + glossyOffset);

    // reflectionView is in view space
    vec3 reflectionWorld = normalize((invViewMatrix * vec4(reflectionView, 0.0)).xyz);

    if(reflectionView.z > 0.0){
        // fallback skybox
        vec3 sky = getSkyGradient(reflectionWorld);
        reflectionColor = vec4(sky * reflectionStrength, 1.0);
        return;
    }

    // ray end in view space
    vec3 rayEndPositionView = positionView.xyz + reflectionView * maxRayDistance;

    // project into texture space
    vec4 rayEndPositionTexture = projection * vec4(rayEndPositionView, 1.0);
    rayEndPositionTexture /= rayEndPositionTexture.w;
    rayEndPositionTexture.xyz = (rayEndPositionTexture.xyz + vec3(1.0)) / 2.0;

    // ray direction in texture space
    vec3 rayDirectionTexture = rayEndPositionTexture.xyz - pixelPositionTexture;

    ivec2 screenSpaceStartPosition = ivec2(uv.x * SCR_WIDTH, uv.y * SCR_HEIGHT);
    ivec2 screenSpaceEndPosition   = ivec2(rayEndPositionTexture.x * SCR_WIDTH, rayEndPositionTexture.y * SCR_HEIGHT);
    ivec2 screenSpaceDistance      = screenSpaceEndPosition - screenSpaceStartPosition;
    int screenSpaceMaxDistance     = max(abs(screenSpaceDistance.x), abs(screenSpaceDistance.y)) / 2;

    // bigger steps = fewer iterations
    int iterationCount = clamp(screenSpaceMaxDistance / 2, 0, 128); // min/max iterations
    float stepSize = 1.0 / float(iterationCount);

    // trace the ray
    bool hit;
    vec3 outColor = TraceRay(pixelPositionTexture, rayDirectionTexture, iterationCount, stepSize, hit);

    if(!hit){
        // if no hit, fallback to sky gradient
        outColor = getSkyGradient(reflectionWorld);
    }

    // scale by reflection factor
    reflectionColor = vec4(outColor * reflectionStrength, 1.0);
}
