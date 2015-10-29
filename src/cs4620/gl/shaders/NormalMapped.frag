#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

// RenderObject Input

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

// Shading Information
uniform float shininess;

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world-space coordinates
varying vec3 vTan; // tangent space x axis in world space
varying vec3 vBitan; // tangent space y axis im world space

void main() {
  // matrix that transforms vectors from tangent space into world space
  mat3 tangentToWorld = mat3(vTan, vBitan, fN);

  // get normal as encoded in texture map color
  vec3 normal = 2.0 * getNormalColor(fUV).xyz - 1.0;

  // normals from texture are in tangent space so must convert to world space
  vec3 N = normalize(tangentToWorld * normal);

  // rest of code is same as phong shader
  vec3 V = normalize(worldCam - worldPos.xyz);  
  vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);

  for (int i = 0; i < numLights; i++) {
    float r = length(lightPosition[i] - worldPos.xyz);
    vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
    vec3 H = normalize(L + V);

    // calculate diffuse term
    vec4 Idiff = getDiffuseColor(fUV) * max(dot(N, L), 0.0);

    // calculate specular term
    vec4 Ispec = getSpecularColor(fUV) * pow(max(dot(N, H), 0.0), shininess);

    finalColor += vec4(lightIntensity[i], 0.0) * (Idiff + Ispec) / (r*r);
  }

  // calculate ambient term
  vec4 Iamb = getDiffuseColor(fUV);
  
  gl_FragColor = (finalColor + vec4(ambientLightIntensity, 0.0) * Iamb) * exposure;

}