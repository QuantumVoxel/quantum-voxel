#version 330 core

in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform sampler2D uPosition;
uniform sampler2D uNormal;
uniform sampler2D uDiffuse;
uniform sampler2D uDepth;
uniform sampler2D uReflective;
uniform mat4 view;
uniform mat4 projection;
uniform vec2 screenSize;
uniform float maxDistance;
uniform float thickness;
uniform float resolution;

// SSAO (Screen Space AO) - by moranzcw - 2021
// Email: moranzcw@gmail.com
// License Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.

#define PI 3.14159265359
#define AOradius 12
#define Samples 256.0

uniform float iGamma = 1.1;

// --------------------------------------
// oldschool rand() from Visual Studio
// --------------------------------------
int   seed = 1;
void  srand(int s ) { seed = s; }
int   rand(void)  { seed=seed*0x343fd+0x269ec3; return (seed>>16)&32767; }
float frand(void) { return float(rand())/32767.0; }
// --------------------------------------
// hash by Hugo Elias
// --------------------------------------
int hash( int n ) { n=(n<<13)^n; return n*(n*n*15731+789221)+1376312589; }

vec3 sphereVolumeRandPoint()
{
    vec3 p = vec3(frand(),frand(),frand()) * 2.0 - 1.0;
    while(length(p)>1.0)
    {
        p = vec3(frand(),frand(),frand()) * 2.0 - 1.0;
    }
    return p;
}

float depth(vec2 coord)
{
    vec2 uv = coord*vec2(screenSize.y/screenSize.x,1.0);
    vec3 encodedDepth = texture(uDepth, uv).xyz;

    float depth;
    depth  = encodedDepth.b * 256.0 * 256.0;
    depth += encodedDepth.g * 256.0;
    depth += encodedDepth.r;

    return depth;
}

float SSAO(vec2 coord)
{
    float cd = depth(coord);
    float screenRadius = 0.5 * (AOradius / cd) / 0.53135;
    float li = 0.0;
    float count = 0.0;
    for(float i=0.0; i<Samples; i++)
    {
        vec3 p = sphereVolumeRandPoint() * frand();
        vec2 sp = vec2(coord.x + p.x * screenRadius, coord.y + p.y * screenRadius);
        float d = depth(sp);
        float at = pow(length(p)-1.0, 2.0);
        li += step(cd + p.z * AOradius, d) * at;
        count += at;
    }
    return li / count;
}

vec3 background(float yCoord)
{
    return vec3(1);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    // coordinate
    vec2 uv = fragCoord/screenSize.xy;
    vec2 coord = fragCoord/screenSize.y;

    float d = depth(coord);
    vec3 ao = vec3(0.4) + step(d, 1e5-1.0) * vec3(0.8) * SSAO(coord);

    vec3 color = pow(ao,vec3(1.0/iGamma)); // gamma
    fragColor = vec4(color, 1.0);
}

void main()
{
    vec2 texCoord = gl_FragCoord.xy / screenSize;

    vec3 diffuse = texture(uDiffuse, texCoord).rgb;
    vec2 fragCoord = gl_FragCoord.xy;
    vec4 fragColor;
    mainImage(fragColor, fragCoord);

    vec3 color = diffuse * fragColor.rgb;

    if (color.a < 0.01) discard;

    FragColor = vec4(color, 1.0);
    return;
}
