
#ifdef GL_ES
#define LOW lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOW
#define HIGH
#endif

#ifndef LOD_LEVEL
#define LOD_LEVEL 0
#endif
uniform vec4 u_color;

void main() {
    gl_FragColor = u_color;
}