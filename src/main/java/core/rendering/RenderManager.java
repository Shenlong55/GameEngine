package core.rendering;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import core.ObjectLoader;
import core.entity.Entity;
import core.entity.SceneManager;

public class RenderManager
{
	private static final float SPECULAR_POWER = 10f;

	private static volatile RenderManager instance;

	private Camera          camera;
	private EntityRenderer  entityRenderer;
	private TerrainRenderer terrainRenderer;

	private boolean isCulling = false;

	private RenderManager()
	{}

	/**
	 * Returns an instance of the render manager.
	 *
	 * @return RenderManager - Render manager instanceF
	 */
	static RenderManager getInstance()
	{
		if(instance == null) synchronized(RenderManager.class)
		{
			if(instance == null) instance = new RenderManager();
		}

		return instance;
	}

	public void cleanup()
	{
		entityRenderer.cleanup();
		terrainRenderer.cleanup();
	}

	public void clear()
	{
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	public void disableCulling()
	{
		if(isCulling)
		{
			GL11.glDisable(GL11.GL_CULL_FACE);
			isCulling = false;
		}
	}

	public void enableCulling()
	{
		if(!isCulling)
		{
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glCullFace(GL11.GL_BACK);
			isCulling = true;
		}
	}

	public Camera getCamera()
	{
		return camera;
	}

	public void initialize(final ObjectLoader objectLoader) throws Exception
	{
		if(camera == null) camera = new Camera();
		entityRenderer  = new EntityRenderer(this);
		terrainRenderer = new TerrainRenderer(this);

		entityRenderer.initialize(objectLoader);
		terrainRenderer.initialize(objectLoader);
	}

	public void render(final SceneManager sceneManager)
	{
		for(final Entity entity: sceneManager.getEntities())
		{
			final var entityList = entityRenderer.getEntities().get(entity.getModel());
			if(entityList != null) entityList.add(entity);
			else
			{
				final List<Entity> newEntityList = new ArrayList<>();
				newEntityList.add(entity);
				entityRenderer.getEntities().put(entity.getModel(), newEntityList);
			}
		}
		terrainRenderer.getTerrains().addAll(sceneManager.getTerrains());

		clear();

		entityRenderer.render(sceneManager);
		terrainRenderer.render(sceneManager);
	}

	public void renderLights(final SceneManager sceneManager, final ShaderManager shaderManager)
	{
		shaderManager.setUniform("ambientLight", sceneManager.getAmbientLight());
		shaderManager.setUniform("specularPower", SPECULAR_POWER);

		final var pointLights = sceneManager.getPointLights();
		var       numLights   = pointLights != null ? pointLights.length : 0;
		for(var i = 0; i < numLights; i++)
			shaderManager.setUniform("pointLights", pointLights[i], i);

		final var spotLights = sceneManager.getSpotLights();
		numLights = spotLights != null ? spotLights.length : 0;
		for(var i = 0; i < numLights; i++)
			shaderManager.setUniform("spotLights", spotLights[i], i);

		shaderManager.setUniform("directionalLight", sceneManager.getDirectionalLight());
	}

	public void setCamera(final Camera camera)
	{
		this.camera = camera;
	}
}