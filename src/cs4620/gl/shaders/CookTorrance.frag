#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

const float PI = 3.141592653589793238462643383279;

// Lighting Information
const int MAX_LIGHTS = 16;
const float F_0 = 0.04;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

// Shading Information
uniform float roughness; // 0 : smooth, 1: rough

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world-space coordinates

void main() {

  vec3 N = normalize(fN);
  vec3 V = normalize(worldCam - worldPos.xyz);

  vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);

  for (int i = 0; i < numLights; i++) {
    float r = length(lightPosition[i] - worldPos.xyz);
    vec3 L = normalize(lightPosition[i] - worldPos.xyz);
    vec3 H = normalize(L + V);

    vec4 ks = getSpecularColor(fUV);
    vec4 kd = getDiffuseColor(fUV);
    float F = F_0 + ((1 - F_0) * pow((1 - dot(V, H)),5));
    float D = 1/(pow(roughness,2)*pow(dot(N,H),4));
    D *= exp((pow(dot(N,H),2) - 1) / ((pow(roughness,2)*pow(dot(N,H),2))));
    float G = min(1, (2 * dot(N, H) * dot(N,V)) / dot(V,H));
    G = min(G, (2 * dot(N, H) * dot(N,L)) / dot(V,H));
    vec4 I = vec4(lightIntensity[i], 0.0);

    vec4 clr = ((ks * F * D * G)/(PI*dot(N,V)*dot(N,L)) + kd)*max(dot(N,L),0.0)*(I/(r*r));
    finalColor += clr;
  }

  vec4 Iamb = getDiffuseColor(fUV);
	gl_FragColor = (finalColor + vec4(ambientLightIntensity, 0.0) * Iamb) * exposure; 
  
}