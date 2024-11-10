package core.rendering;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.DragonEngine;
import core.ObjectLoader;
import core.entity.Entity;
import core.entity.Model;
import core.entity.SceneManager;

class EntityRenderer implements IRenderer<Entity>
{
	private final Map<Model, List<Entity>> entities;
	private final RenderManager            renderManager;
	private final ShaderManager            shaderManager;

	public EntityRenderer(final RenderManager renderManager) throws Exception
	{
		entities           = new HashMap<>();
		shaderManager      = new ShaderManager();
		this.renderManager = renderManager;
	}

	@Override
	public void bind(final Model model)
	{
		GL30.glBindVertexArray(model.getId());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		if(model.getMaterial().isDisableCulling()) renderManager.disableCulling();
		else renderManager.enableCulling();

		shaderManager.setUniform("material", model.getMaterial());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getId());
	}

	@Override
	public void cleanup()
	{
		shaderManager.cleanup();
	}

	public Map<Model, List<Entity>> getEntities()
	{
		return entities;
	}

	@Override
	public void initialize(final ObjectLoader objectLoader) throws Exception
	{
		shaderManager.createVertexShader(objectLoader.loadResource("/shaders/entity_vertex.vs"));
		shaderManager.createFragmentShader(objectLoader.loadResource("/shaders/entity_fragment.fs"));
		shaderManager.link();
		shaderManager.createUniform("textureSampler");
		shaderManager.createUniform("transformationMatrix");
		shaderManager.createUniform("projectionMatrix");
		shaderManager.createUniform("viewMatrix");
		shaderManager.createUniform("ambientLight");
		shaderManager.createMaterialUniform("material");
		shaderManager.createUniform("specularPower");
		shaderManager.createDirectionalLightUniform("directionalLight");
		shaderManager.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		shaderManager.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
	}

	@Override
	public void prepare(final Entity entity)
	{
		shaderManager.setUniform("textureSampler", 0);
		shaderManager.setUniform("transformationMatrix", createTransformationMatrix(entity));
		shaderManager.setUniform("viewMatrix", renderManager.getCamera().getViewMatrix());
	}

	@Override
	public void render(final SceneManager sceneManager)
	{
		shaderManager.bind();
		shaderManager.setUniform("projectionMatrix", DragonEngine.getWindowProjectionMatrix());
		renderManager.renderLights(sceneManager, shaderManager);
		for(final Model model: entities.keySet())
		{
			bind(model);
			final var entityList = entities.get(model);
			for(final Entity entity: entityList)
			{
				prepare(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbind();
		}
		entities.clear();
		shaderManager.unbind();
	}

	@Override
	public void unbind()
	{
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}

	private Matrix4f createTransformationMatrix(final Entity entity)
	{
		final var matrix = new Matrix4f();
		matrix.identity().translate(entity.getPos()).rotateX((float) Math.toRadians(entity.getRotation().x)).rotateY((float) Math.toRadians(entity.getRotation().y))
				.rotateZ((float) Math.toRadians(entity.getRotation().z)).scale(entity.getScale());
		return matrix;
	}
}
