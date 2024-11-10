package core.rendering;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import core.DragonEngine;
import core.ObjectLoader;
import core.entity.Model;
import core.entity.SceneManager;
import core.entity.terrain.Terrain;

class TerrainRenderer implements IRenderer<Terrain>
{
	private final List<Terrain> terrains;
	private final RenderManager renderManager;
	private final ShaderManager shaderManager;

	public TerrainRenderer(final RenderManager renderManager) throws Exception
	{
		terrains           = new ArrayList<>();
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

		renderManager.enableCulling();

		shaderManager.setUniform("backgroundTexture", 0);
		shaderManager.setUniform("redTexture", 1);
		shaderManager.setUniform("greenTexture", 2);
		shaderManager.setUniform("blueTexture", 3);
		shaderManager.setUniform("blendMap", 4);
		shaderManager.setUniform("material", model.getMaterial());
	}

	@Override
	public void cleanup()
	{
		shaderManager.cleanup();
	}

	public List<Terrain> getTerrains()
	{
		return terrains;
	}

	@Override
	public void initialize(final ObjectLoader objectLoader) throws Exception
	{
		shaderManager.createVertexShader(objectLoader.loadResource("/shaders/terrain_vertex.vs"));
		shaderManager.createFragmentShader(objectLoader.loadResource("/shaders/terrain_fragment.fs"));
		shaderManager.link();
		shaderManager.createUniform("backgroundTexture");
		shaderManager.createUniform("redTexture");
		shaderManager.createUniform("greenTexture");
		shaderManager.createUniform("blueTexture");
		shaderManager.createUniform("blendMap");
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
	public void prepare(final Terrain terrain)
	{
		final var blendMapTerrain = terrain.getBlendMapTerrain();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, blendMapTerrain.getBackground().getId());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, blendMapTerrain.getRedTexture().getId());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, blendMapTerrain.getGreenTexture().getId());
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, blendMapTerrain.getBlueTexture().getId());
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrain.getBlendMap().getId());

		shaderManager.setUniform("transformationMatrix", createTransformationMatrix(terrain));
		shaderManager.setUniform("viewMatrix", renderManager.getCamera().getViewMatrix());
	}

	@Override
	public void render(final SceneManager sceneManager)
	{
		shaderManager.bind();
		shaderManager.setUniform("projectionMatrix", DragonEngine.getWindowProjectionMatrix());
		renderManager.renderLights(sceneManager, shaderManager);
		for(final Terrain terrain: terrains)
		{
			bind(terrain.getModel());
			prepare(terrain);
			GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			unbind();
		}
		terrains.clear();
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

	private Matrix4f createTransformationMatrix(final Terrain terrain)
	{
		final var matrix = new Matrix4f();
		matrix.identity().translate(terrain.getPosition()).scale(1);
		return matrix;
	}
}
