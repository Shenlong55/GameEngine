package core.rendering;

import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import core.entity.Material;
import core.lighting.DirectionalLight;
import core.lighting.PointLight;
import core.lighting.SpotLight;

public class ShaderManager
{
	private final Map<String, Integer>	uniforms;
	private final int					programId;

	private int vertexShaderId, fragmentShaderId;

	public ShaderManager() throws Exception
	{
		programId = GL20.glCreateProgram();
		if (programId == 0)
		{
			throw new Exception("Could not create shader.");
		}

		uniforms = new HashMap<>();
	}

	public void createUniform(String uniformName) throws Exception
	{
		int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
		if (uniformLocation < 0)
		{
			throw new Exception("Could not find uniform " + uniformName);
		}
		uniforms.put(uniformName, uniformLocation);
	}

	public void createDirectionalLightUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".color");
		createUniform(uniformName + ".direction");
		createUniform(uniformName + ".intensity");
	}

	public void createMaterialUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".ambient");
		createUniform(uniformName + ".diffuse");
		createUniform(uniformName + ".specular");
		createUniform(uniformName + ".hasTexture");
		createUniform(uniformName + ".reflectance");
	}

	public void createPointLightUniform(String uniformName) throws Exception
	{
		createUniform(uniformName + ".color");
		createUniform(uniformName + ".position");
		createUniform(uniformName + ".intensity");
		createUniform(uniformName + ".constant");
		createUniform(uniformName + ".linear");
		createUniform(uniformName + ".exponent");
	}

	public void createPointLightListUniform(String uniformName, int size) throws Exception
	{
		for (int i = 0; i < size; i++)
		{
			createPointLightUniform(uniformName + "[" + i + "]");
		}
	}

	public void createSpotLightListUniform(String uniformName, int size) throws Exception
	{
		for (int i = 0; i < size; i++)
		{
			createSpotLightUniform(uniformName + "[" + i + "]");
		}
	}

	public void createSpotLightUniform(String uniformName) throws Exception
	{
		createPointLightUniform(uniformName + ".pl");
		createUniform(uniformName + ".coneDir");
		createUniform(uniformName + ".cutoff");
	}

	public void setUniform(String uniformName, Matrix4f value)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, value.get(stack.mallocFloat(16)));
		}
	}

	public void setUniform(String uniformName, Vector4f value)
	{
		GL20.glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
	}

	public void setUniform(String uniformName, Vector3f value)
	{
		GL20.glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
	}

	public void setUniform(String uniformName, boolean value)
	{
		float res = 0;
		if (value)
		{
			res = 1;
		}
		GL20.glUniform1f(uniforms.get(uniformName), res);
	}

	public void setUniform(String uniformName, int value)
	{
		GL20.glUniform1i(uniforms.get(uniformName), value);
	}

	public void setUniform(String uniformName, float value)
	{
		GL20.glUniform1f(uniforms.get(uniformName), value);
	}

	public void setUniform(String uniformName, Material material)
	{
		setUniform(uniformName + ".ambient", material.getAmbientColor());
		setUniform(uniformName + ".diffuse", material.getDiffuseColor());
		setUniform(uniformName + ".specular", material.getSpecularColor());
		setUniform(uniformName + ".hasTexture", material.hasTexture() ? 1 : 0);
		setUniform(uniformName + ".reflectance", material.getReflectance());
	}

	public void setUniform(String uniformName, DirectionalLight directionalLight)
	{
		setUniform(uniformName + ".color", directionalLight.getColor());
		setUniform(uniformName + ".direction", directionalLight.getDirection());
		setUniform(uniformName + ".intensity", directionalLight.getIntensity());
	}

	public void setUniform(String uniformName, PointLight pointLight)
	{
		setUniform(uniformName + ".color", pointLight.getColor());
		setUniform(uniformName + ".position", pointLight.getPosition());
		setUniform(uniformName + ".intensity", pointLight.getIntensity());
		setUniform(uniformName + ".constant", pointLight.getConstant());
		setUniform(uniformName + ".linear", pointLight.getLinear());
		setUniform(uniformName + ".exponent", pointLight.getExponent());
	}

	public void setUniform(String uniformName, SpotLight spotLight)
	{
		setUniform(uniformName + ".pl", (PointLight) spotLight);
		setUniform(uniformName + ".coneDir", spotLight.getConeDirection());
		setUniform(uniformName + ".cutoff", spotLight.getCutoff());
	}

	public void setUniform(String uniformName, PointLight[] pointLights)
	{
		int numLights = pointLights != null ? pointLights.length : 0;
		for (int i = 0; i < numLights; i++)
		{
			setUniform(uniformName, pointLights[i], i);
		}
	}

	public void setUniform(String uniformName, PointLight pointLight, int pos)
	{
		setUniform(uniformName + "[" + pos + "]", pointLight);
	}

	public void setUniform(String uniformName, SpotLight[] spotLights)
	{
		int numLights = spotLights != null ? spotLights.length : 0;
		for (int i = 0; i < numLights; i++)
		{
			setUniform(uniformName, spotLights[i], i);
		}
	}

	public void setUniform(String uniformName, SpotLight spotLight, int pos)
	{
		setUniform(uniformName + "[" + pos + "]", spotLight);
	}

	public void createVertexShader(String shaderCode) throws Exception
	{
		vertexShaderId = createShader(shaderCode, GL20.GL_VERTEX_SHADER);
	}

	public void createFragmentShader(String shaderCode) throws Exception
	{
		fragmentShaderId = createShader(shaderCode, GL20.GL_FRAGMENT_SHADER);
	}

	public int createShader(String shaderCode, int shaderType) throws Exception
	{
		int shaderId = GL20.glCreateShader(shaderType);
		if (shaderId == 0)
		{
			throw new Exception("Error creating shader.  Type: " + shaderType);
		}

		GL20.glShaderSource(shaderId, shaderCode);
		GL20.glCompileShader(shaderId);

		if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0)
		{
			throw new Exception("Error compiling shader code. Type: " + shaderType + " Info: " + GL20.glGetShaderInfoLog(shaderId, 1024));
		}

		GL20.glAttachShader(programId, shaderId);

		return shaderId;
	}

	public void link() throws Exception
	{
		GL20.glLinkProgram(programId);

		if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0)
		{
			throw new Exception("Error linking shader code. Info: " + GL20.glGetProgramInfoLog(programId, 1024));
		}

		if (vertexShaderId != 0)
		{
			GL20.glDetachShader(programId, vertexShaderId);
		}

		if (fragmentShaderId != 0)
		{
			GL20.glDetachShader(programId, fragmentShaderId);
		}

		GL20.glValidateProgram(programId);

		if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0)
		{
			throw new Exception("Unable to validate shader code. Info: " + GL20.glGetProgramInfoLog(programId, 1024));
		}
	}

	public void bind()
	{
		GL20.glUseProgram(programId);
	}

	public void unbind()
	{
		GL20.glUseProgram(0);
	}

	public void cleanup()
	{
		unbind();
		if (programId != 0)
		{
			GL20.glDeleteProgram(programId);
		}
	}
}