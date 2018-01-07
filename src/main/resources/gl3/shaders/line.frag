#version 330

#define EPS 0.001
#define MAX 20.0
#define STEPS 100
#define PI 3.14159

#include constants.glsl


in vec4 diffuseColor_;
in vec3 vertexNormal;
in vec3 cameraSpacePosition;

layout (location = FRAG_COLOR) out vec4 outputColor;

uniform vec3 cameraSpaceLightPos;

void main()
{
    outputColor = diffuseColor_;
}