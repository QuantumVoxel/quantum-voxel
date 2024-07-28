#version 330 core

in vec4 v_color;
in vec2 v_texCoords;

out vec4 fragColor;

uniform sampler2D u_texture;

struct TextAttributes {
    int style;
    int color;
    float scale;
};

layout(std430, binding = 0) buffer TextAttributesBuffer {
    TextAttributes textAttributes[];
};

void main() {
    vec4 color = texture(u_texture, v_texCoords) * v_color;
    int index = gl_VertexID / 6;
    bool underline = textAttributes[index].style & 1;
    bool italic = textAttributes[index].style & 2;
    bool bold = textAttributes[index].style & 4;
    bool strikethrough = textAttributes[index].style & 8;
    bool shadow = textAttributes[index].style & 16;

    float red = textAttributes[index].color & 0xFF0000 >> 16;
    float green = textAttributes[index].color & 0xFF00 >> 8;
    float blue = textAttributes[index].color & 0xFF;
    vec4 color = vec4(red / 255.0, green / 255.0, blue / 255.0, 1.0);

    if (bold == 1) {
        // Simple bold effect by sampling texture multiple times
        color += texture(u_texture, v_texCoords + vec2(0.005, 0.0)) * v_color;
    }
    if (italic == 1) {
        // Simple italic effect by offsetting texture coordinates
        v_texCoords.x += 0.005 * v_texCoords.y;
    }
    if (underline == 1) {
        // Underline effect
        if (v_texCoords.y < 0.1) {
            color = textAttributes[index].color;
        }
    }
    if (strikethrough == 1) {
        // Strikethrough effect
        if (v_texCoords.y > 0.45 && v_texCoords.y < 0.55) {
            color = textAttributes[index].color;
        }
    }

    fragColor = color;
}
