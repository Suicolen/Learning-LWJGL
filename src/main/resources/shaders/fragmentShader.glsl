#version 460

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector;
in vec3 toCameraVector;
in vec4 v_outcolor;

out vec4 out_Color;

uniform sampler2D textureSampler;
uniform float time;
uniform vec3 lightColor;
uniform float shineDamper;
uniform float reflectivity;

void main() {

    vec2 uv = pass_textureCoords;
    // uv.y = 1-uv.y;
    //  uv.y += time / 7.;
    //uv.y = mod(uv.y, 1.);
    //vec3 col = texture(textureSampler, uv, 1.).rgb;

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitLightVector = normalize(toLightVector);

    float nDotl = dot(unitNormal, unitLightVector);
    //   vec3 ground = vec3(0.2,0.04,0.01);
    //  vec3 sky = vec3(0.01,0.2,0.4);
    // vec3 c = mix(sky,ground, (normal.y + 1.0) * 0.5);
    float brightness = max(nDotl, 0.2);
    vec3 diffuse = brightness * lightColor * v_outcolor.xyz;

    vec3 unitVectorToCamera = normalize(toCameraVector);
    vec3 lightDirection = -unitLightVector;
    vec3 reflectedLightDirection = reflect(-lightDirection, unitNormal);

    float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
    specularFactor = max(specularFactor, 0.0);
    float dampedFactor = pow(specularFactor, shineDamper);
    vec3 finalSpecular = dampedFactor * reflectivity * lightColor;

    out_Color = vec4(diffuse, 1.0) * texture(textureSampler, pass_textureCoords) + vec4(finalSpecular, 1.0);

    //out_Color = vec4(surfaceNormal, 1.0);
}