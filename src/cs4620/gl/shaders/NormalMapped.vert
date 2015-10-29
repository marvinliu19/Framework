#version 120

// Note: We multiply a vector with a matrix from the left side (M * v)!
// mProj * mView * mWorld * pos

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec3 vNormal; // Sem (NORMAL 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)
attribute vec3 vTangent; // Sem (TANGENT 0)
attribute vec3 vBitangent; // Sem (BINORMAL 0)

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world-space coordinates
varying vec3 vTan; // tangent space x axis in world space
varying vec3 vBitan; // tangent space y axis in world space

void main() {

  worldPos = mWorld * vPosition;
  gl_Position = mViewProjection * worldPos;

  // transforms tangent, bitangent, normal vectors into world space
  fN = normalize((mWorldIT * vNormal).xyz);
  vTan = normalize((mWorldIT * vTangent).xyz);
  vBitan = normalize((mWorldIT * vBitangent).xyz);

  fUV = vUV;
}