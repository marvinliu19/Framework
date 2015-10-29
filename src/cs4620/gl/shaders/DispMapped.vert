#version 120

// Note: We multiply a vector with a matrix from the left side (M * v)!
// mProj * mView * mWorld * pos

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;
uniform float dispMagnitude;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec3 vNormal; // Sem (NORMAL 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world-space coordinates

void main() {
  // We have to use the inverse transpose of the world transformation matrix for the normal
  fN = normalize((mWorldIT * vNormal).xyz);
  fUV = vUV;

  // get the color of the height map texture
  vec3 color = getNormalColor(fUV).xyz;
  // height is the average of the three color channels
  float height = (color.x + color.y + color.z) / 3.0;
  // transform vPosition in direction of vNormal in object space
  vec4 position = vPosition + (vec4(vNormal, 0.0) * height * dispMagnitude);

  // transform position from object space into world space coordinates
  worldPos = mWorld * position;
  // output projection coordinates of worldPos
  gl_Position = mViewProjection * worldPos;
}
