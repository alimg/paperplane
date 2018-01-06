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

uniform vec3 modelSpaceLightPos;

uniform vec3 cameraSpaceLightPos;

uniform float lightAttenuation;

const vec4 specularColor = vec4(0.25, 0.25, 0.25, 1.0);
uniform float shininessFactor;


float calcAttenuation(in vec3 cameraSpacePosition, out vec3 lightDirection)
{
    vec3 lightDifference =  cameraSpaceLightPos - cameraSpacePosition;
    float lightDistanceSqr = dot(lightDifference, lightDifference);
    lightDirection = lightDifference * inversesqrt(lightDistanceSqr);

    return (1 / (1.0 + lightAttenuation * sqrt(lightDistanceSqr)));
}


void main()
{
    vec4 lightIntensity = vec4(0.9,0.9, 0.8, 0.8);
    vec4 ambientIntensity = vec4(0.4);

    vec3 lightDir = vec3(0.6);
    float atten = calcAttenuation(cameraSpacePosition, lightDir);
    vec4 attenIntensity = atten * lightIntensity;

    vec3 surfaceNormal = normalize(vertexNormal);
    float cosAngIncidence = dot(surfaceNormal, lightDir);
    cosAngIncidence = clamp(abs(cosAngIncidence), 0, 1);

    vec3 viewDirection = normalize(-cameraSpacePosition);

    vec3 halfAngle = normalize(lightDir + viewDirection);
    float angleNormalHalf = acos(dot(halfAngle, surfaceNormal));
    float exponent = angleNormalHalf / shininessFactor;
    exponent = -(exponent * exponent);
    float gaussianTerm = exp(exponent);

    gaussianTerm = cosAngIncidence != 0.0 ? gaussianTerm : 0.0;

    outputColor = (diffuseColor_ * attenIntensity * cosAngIncidence) +
            (specularColor * attenIntensity * gaussianTerm) +
            (diffuseColor_ * ambientIntensity);
}