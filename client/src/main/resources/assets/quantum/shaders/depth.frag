#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
in vec2 v_texCoords0;
uniform sampler2D u_diffuseTexture;
uniform float u_alphaTest;
#endif

out vec4 fragColor;
in vec3 fragCoord;

void main() {
    float depth = fragCoord.z / fragCoord.w;

    vec3 depthIn3Channels;
    depthIn3Channels.r = mod(depth, 1.0);
    depth -= depthIn3Channels.r;
    depth /= 256.0;

    depthIn3Channels.g = mod(depth, 1.0);
    depth -= depthIn3Channels.g;
    depth /= 256.0;

    depthIn3Channels.b = depth;

    fragColor = vec4(depthIn3Channels, 1.0);
}
