package core.entity.terrain;

public class BlendMapTerrain
{
	TerrainTexture background, redTexture, blueTexture, greenTexture;

	public BlendMapTerrain(TerrainTexture background, TerrainTexture redTexture, TerrainTexture blueTexture, TerrainTexture greenTexture)
	{
		this.background = background;
		this.redTexture = redTexture;
		this.blueTexture = blueTexture;
		this.greenTexture = greenTexture;
	}

	public TerrainTexture getBackground()
	{
		return background;
	}

	public TerrainTexture getRedTexture()
	{
		return redTexture;
	}

	public TerrainTexture getBlueTexture()
	{
		return blueTexture;
	}

	public TerrainTexture getGreenTexture()
	{
		return greenTexture;
	}
}