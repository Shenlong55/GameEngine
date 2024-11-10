package core.lighting;

import org.joml.Vector3f;

public class SpotLight extends PointLight
{
	private Vector3f	coneDirection;
	private float		cutoff;

	public SpotLight(Vector3f color, Vector3f position, float intensity, float constant, float linear, float exponent, Vector3f coneDirection, float cutoff)
	{
		super(color, position, intensity, constant, linear, exponent);
		this.coneDirection = coneDirection;
		this.cutoff = cutoff;
	}

	public SpotLight(PointLight pointLight, Vector3f coneDirection, float cutoff)
	{
		super(pointLight);
		this.coneDirection = coneDirection;
		this.cutoff = cutoff;
	}

	public SpotLight(SpotLight spotLight)
	{
		super(spotLight);
	}

	public Vector3f getConeDirection()
	{
		return coneDirection;
	}

	public void setConeDirection(Vector3f coneDirection)
	{
		this.coneDirection = coneDirection;
	}

	public float getCutoff()
	{
		return cutoff;
	}

	public void setCutoff(float cutoff)
	{
		this.cutoff = cutoff;
	}
}