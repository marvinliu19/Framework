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

varying vec4 worldPos; // vertex position in world-space coordinates

void main() {
  worldPos = mWorld * vPosition;
  gl_Position = mViewProjection * worldPos;

}