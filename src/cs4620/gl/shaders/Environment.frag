#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)
// vec4 getEnvironmentColor(vec3 dir)

// Lighting Information

// Camera Information
uniform vec3 worldCam;

varying vec4 worldPos; // vertex position in world-space coordinates

void main() {
  vec3 I = normalize(worldPos.xyz - worldCam);
	gl_FragColor = getEnvironmentColor(I);
}