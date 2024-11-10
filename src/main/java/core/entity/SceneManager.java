package core.entity;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import core.ObjectLoader;
import core.entity.terrain.Terrain;
import core.lighting.DirectionalLight;
import core.lighting.PointLight;
import core.lighting.SpotLight;

public class SceneManager
{
	private static final Vector3f DEFAULT_AMBIENT_LIGHT = new Vector3f(1.3f, 1.3f, 1.3f);

	private static volatile SceneManager instance;

	private List<Entity>  entities;
	private List<Terrain> terrains;

	private Vector3f         ambientLight;
	private PointLight[]     pointLights;
	private SpotLight[]      spotLights;
	private DirectionalLight directionalLight;
	private float            lightAngle;

	private float spotAngle = 0;
	private float spotInc   = 1;

	private SceneManager()
	{
		directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(-1, 0, 0), 1);
		entities         = new ArrayList<>();
		terrains         = new ArrayList<>();
		ambientLight     = DEFAULT_AMBIENT_LIGHT;
	}

	static SceneManager getInstance()
	{
		if(instance == null) synchronized(ObjectLoader.class)
		{
			if(instance == null) instance = new SceneManager();
		}

		return instance;
	}

	public void addEntity(final Entity entity)
	{
		entities.add(entity);
	}

	public void addTerrain(final Terrain terrain)
	{
		terrains.add(terrain);
	}

	public Vector3f getAmbientLight()
	{
		return ambientLight;
	}

	public DirectionalLight getDirectionalLight()
	{
		return directionalLight;
	}

	public List<Entity> getEntities()
	{
		return entities;
	}

	public float getLightAngle()
	{
		return lightAngle;
	}

	public PointLight[] getPointLights()
	{
		return pointLights;
	}

	public float getSpotAngle()
	{
		return spotAngle;
	}

	public float getSpotInc()
	{
		return spotInc;
	}

	public SpotLight[] getSpotLights()
	{
		return spotLights;
	}

	public List<Terrain> getTerrains()
	{
		return terrains;
	}

	public void incLightAngle(final float increment)
	{
		lightAngle += increment;
	}

	public void incSpotAngle(final float increment)
	{
		spotAngle *= spotAngle;
	}

	public void setAmbientLight(final float x, final float y, final float z)
	{
		ambientLight = new Vector3f(x, y, z);
	}

	public void setAmbientLight(final Vector3f ambientLight)
	{
		this.ambientLight = ambientLight;
	}

	public void setDirectionalLight(final DirectionalLight directionalLight)
	{
		this.directionalLight = directionalLight;
	}

	public void setEntities(final List<Entity> entities)
	{
		this.entities = entities;
	}

	public void setLightAngle(final float lightAngle)
	{
		this.lightAngle = lightAngle;
	}

	public void setPointLights(final PointLight[] pointLights)
	{
		this.pointLights = pointLights;
	}

	public void setSpotAngle(final float spotAngle)
	{
		this.spotAngle = spotAngle;
	}

	public void setSpotInc(final float spotInc)
	{
		this.spotInc = spotInc;
	}

	public void setSpotLights(final SpotLight[] spotLights)
	{
		this.spotLights = spotLights;
	}

	public void setTerrains(final List<Terrain> terrains)
	{
		this.terrains = terrains;
	}
}