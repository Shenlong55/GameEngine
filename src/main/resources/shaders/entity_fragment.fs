#version 400 core

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec2 fragTextureCoord;
in vec3 fragNormal;
in vec3 fragPos;

out vec4 fragColor;

struct Material
{
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	int hasTexture;
	float reflectance;
};

struct DirectionalLight
{
	vec3 color;
	vec3 direction;
	float intensity;
};

struct PointLight
{
	vec3 color;
	vec3 position;
	float intensity;
	float constant;
	float linear;
	float exponent;
};

struct SpotLight
{
	PointLight pl;
	vec3 coneDir;
	float cutoff;
};

uniform sampler2D textureSampler;
uniform vec3 ambientLight;
uniform Material material;
uniform float specularPower;
uniform DirectionalLight directionalLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(Material material, vec2 textCoords)
{
	if(material.hasTexture == 1)
	{
		ambientC = texture(textureSampler, textCoords);
		diffuseC = ambientC;
		specularC = ambientC;
	}
	else
	{
		ambientC = material.ambient;
		diffuseC = material.diffuse;
		specularC = material.specular;
	}
}

vec4 calcLightColor(vec3 lightColor, float lightIntensity, vec3 position, vec3 toLightDir, vec3 normal)
{
	vec4 diffuseColor = vec4(0, 0, 0, 0);
	vec4 specularColor = vec4(0, 0, 0, 0);
	
	//diffuse light
	float diffuseFactor = max(dot(normal, toLightDir), 0.0);
	diffuseColor = diffuseC * vec4(lightColor, 1.0) * lightIntensity * diffuseFactor;
	
	//specular color
	vec3 cameraDirection = normalize(-position);
	vec3 fromLightDir = -toLightDir;
	vec3 reflectedLight = normalize(reflect(fromLightDir, normal));
	float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0);
	specularFactor = pow(specularFactor, specularPower);
	specularColor = specularC * lightIntensity * specularFactor * material.reflectance * vec4(lightColor, 1.0);
	
	return diffuseColor + specularColor;
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
	vec3 lightDir = light.position - position;
	vec3 toLightDir = normalize(lightDir);
	vec4 lightColor = calcLightColor(light.color, light.intensity, position, toLightDir, normal);
	
	//attenuation
	float distance = length(lightDir);
	float attenuationInv = light.constant + light.linear * distance + light.exponent * distance * distance;
	return lightColor / attenuationInv;
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal)
{
	vec3 lightDir = light.pl.position - position;
	vec3 toLightDir = normalize(lightDir);
	vec3 fromLightDir = -toLightDir;
	float spotAlpha = dot(fromLightDir, normalize(light.coneDir));
	
	vec4 color = vec4(0, 0, 0, 0);
	
	if(spotAlpha > light.cutoff)
	{
		color = calcPointLight(light.pl, position, normal);
		color *= (1.0 - (1.0 - spotAlpha) / (1.0 - light.cutoff));
	}
	
	return color;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
	return calcLightColor(light.color, light.intensity, position, normalize(light.direction), normal);
}

void main()
{
	setupColors(material, fragTextureCoord);
	
	vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, fragPos, fragNormal);
	
	for(int i = 0; i < MAX_POINT_LIGHTS; i++)
	{
		if(pointLights[i].intensity > 0)
		{
			diffuseSpecularComp += calcPointLight(pointLights[i], fragPos, fragNormal);
		}
	}
	
	for(int i = 0; i < MAX_SPOT_LIGHTS; i++)
	{
		if(spotLights[i].pl.intensity > 0)
		{
			diffuseSpecularComp += calcSpotLight(spotLights[i], fragPos, fragNormal);
		}
	}
	
	fragColor = ambientC * vec4(ambientLight, 1) * diffuseSpecularComp;
}