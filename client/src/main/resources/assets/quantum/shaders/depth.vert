attribute vec3 a_position;
uniform mat4 u_projViewWorldTrans;

#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
    attribute vec2 a_texCoord0;
    varying vec2 v_texCoords0;
#endif

#ifdef PackedDepthFlag
    varying float v_depth;
#endif //PackedDepthFlag

void main() {
    #ifdef blendedTextureFlag
        v_texCoords0 = a_texCoord0;
    #endif // blendedTextureFlag

    vec4 pos = u_projViewWorldTrans * vec4(a_position, 1.0);

    #ifdef PackedDepthFlag
        v_depth = pos.z / pos.w * 0.5 + 0.5;
    #endif //PackedDepthFlag

    gl_Position = pos;
}
