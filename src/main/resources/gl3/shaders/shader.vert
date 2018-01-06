
#version 330


#include constants.glsl


// Incoming vertex position, Model Space
layout (location = POSITION) in vec3 position;
layout (location = COLOR) in vec3 diffuseColor;
layout (location = NORMAL) in vec3 normal;


uniform GlobalMatrices
{
    mat4 view;
    mat4 proj;
};


// Uniform matrix from Model Space to camera (also known as view) Space
uniform mat4 model;

out vec4 diffuseColor_;
out vec3 vertexNormal;
out vec3 cameraSpacePosition;

void main() {
    vec4 tempCamPosition = proj * (view * (model * vec4(position, 1)));// (modelToCameraMatrix * vec4(position, 1.0));
    gl_Position = tempCamPosition;
    //vertexNormal = (proj * (view * (model * vec4(normal, 1)))).xyz;
    vertexNormal = normal;
    diffuseColor_ = vec4(diffuseColor, 1);
    cameraSpacePosition = vec3(tempCamPosition);
/*
    // Normally gl_Position is in Clip Space and we calculate it by multiplying together all the matrices
    gl_Position = proj * (view * (model * vec4(position, 1)));

    // We assign the color to the outgoing variable.
    interpolatedColor = color;*/
}