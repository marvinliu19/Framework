#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)
// vec4 getEnvironmentColor(vec3 dir)

// Lighting Information

// Camera Information
uniform vec3 worldCam;

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world-space coordinates

void main() {
  vec3 V = normalize(worldCam - worldPos.xyz);
  vec3 I = normalize(worldPos.xyz - worldCam);
  vec3 N = normalize(fN);
  vec3 R = reflect(I, N);
	gl_FragColor = getEnvironmentColor(V);
}