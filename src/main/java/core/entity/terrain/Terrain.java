package core.entity.terrain;

import org.joml.Vector3f;

import core.ObjectLoader;
import core.entity.Material;
import core.entity.Model;
import core.entity.Texture;

public class Terrain
{
	private static final float	SIZE			= 800;
	private static int			VERTEX_COUNT	= 128;

	private Vector3f		position;
	private Model			model;
	private TerrainTexture	blendMap;
	private BlendMapTerrain	blendMapTerrain;

	public Terrain(Vector3f position, ObjectLoader objectLoader, Material material, TerrainTexture blendMap, BlendMapTerrain blendMapTerrain)
	{
		this.position = position;
		this.model = generateTerrain(objectLoader);
		this.model.setMaterial(material);
		this.blendMap = blendMap;
		this.blendMapTerrain = blendMapTerrain;
	}

	private Model generateTerrain(ObjectLoader objectLoader)
	{
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int vertexCountSubOne = (VERTEX_COUNT - 1);
		int[] indices = new int[6 * vertexCountSubOne * vertexCountSubOne];
		int vertexPointer = 0;

		for (int i = 0; i < VERTEX_COUNT; i++)
		{
			for (int j = 0; j < VERTEX_COUNT; j++)
			{
				vertices[vertexPointer * 3] = j / vertexCountSubOne * SIZE;
				vertices[vertexPointer * 3 + 1] = 0; // height map
				vertices[vertexPointer * 3 + 2] = i / vertexCountSubOne * SIZE;
				normals[vertexPointer * 3] = 0;
				normals[vertexPointer * 3 + 1] = 1;
				normals[vertexPointer * 3 + 2] = 0;
				textureCoords[vertexPointer * 2] = j / vertexCountSubOne;
				textureCoords[vertexPointer * 2 + 1] = i / vertexCountSubOne;
				vertexPointer++;
			}
		}

		int pointer = 0;
		for (int x = 0; x < vertexCountSubOne; x++)
		{
			for (int z = 0; z < vertexCountSubOne; z++)
			{
				int topLeft = (x * VERTEX_COUNT) + z;
				int topRight = topLeft + 1;
				int bottomLeft = ((x + 1) * VERTEX_COUNT) + z;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;

			}
		}

		return objectLoader.loadModel(vertices, textureCoords, normals, indices);
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public Model getModel()
	{
		return model;
	}

	public Material getMaterial()
	{
		return model.getMaterial();
	}

	public Texture getTexture()
	{
		return model.getTexture();
	}

	public TerrainTexture getBlendMap()
	{
		return blendMap;
	}

	public BlendMapTerrain getBlendMapTerrain()
	{
		return blendMapTerrain;
	}
}