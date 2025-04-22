
#ifdef GL_ES
precision mediump float;
#endif

uniform vec2 u_aa_radius;

varying vec4 g_col;
varying float g_u;
varying float g_v;
varying float g_line_width;
varying float g_line_length;

void main()
{
    /* We render a quad that is fattened by r, giving total width of the line to be w+r. We want smoothing to happen
       around w, so that the edge is properly smoothed out. As such, varying the smoothstep function we have:
       Far edge   : 1.0                                          = (w+r) / (w+r)
       Close edge : 1.0 - (2r / (w+r)) = (w+r)/(w+r) - 2r/(w+r)) = (w-r) / (w+r)
       This way the smoothing is centered around 'w'.
     */
    float au = 1.0 - smoothstep( 1.0 - ((2.0*u_aa_radius[0]) / g_line_width),  1.0, abs(g_u / g_line_width) );
    float av = 1.0 - smoothstep( 1.0 - ((2.0*u_aa_radius[1]) / g_line_length), 1.0, abs(g_v / g_line_length) );
    gl_gl_FragColor = g_col;
    gl_gl_FragColor.a *= min(av, au);
}
