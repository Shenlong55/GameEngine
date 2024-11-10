package core.rendering;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera
{
	private final Vector3f position, rotation;

	public Camera()
	{
		position = new Vector3f(0.0f, 2.0f, 0.0f);
		rotation = new Vector3f(0.0f, 0.0f, 0.0f);
	}

	public Camera(final Vector3f position, final Vector3f rotation)
	{
		this.position = position;
		this.rotation = rotation;
	}

	public Vector3f getPosition()
	{
		return position;
	}

	public Vector3f getRotation()
	{
		return rotation;
	}

	public Matrix4f getViewMatrix()
	{
		final var matrix = new Matrix4f();
		matrix.identity();
		matrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0)).rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0)).rotate((float) Math.toRadians(rotation.z),
				new Vector3f(0, 0, 1));
		matrix.translate(-position.x, -position.y, -position.z);
		return matrix;
	}

	public void movePosition(final float x, final float y, final float z)
	{
		if(z != 0)
		{
			position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * z;
			position.z += (float) Math.cos(Math.toRadians(rotation.y)) * z;
		}
		if(x != 0)
		{
			position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * x;
			position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * x;
		}

		position.y += y;
	}

	public void moveRotation(final float x, final float y, final float z)
	{
		rotation.x += x;
		rotation.y += y;
		rotation.z += z;
	}

	public void setPosition(final float x, final float y, final float z)
	{
		position.x = x;
		position.y = y;
		position.z = z;
	}

	public void setRotation(final float x, final float y, final float z)
	{
		rotation.x = x;
		rotation.y = y;
		rotation.z = z;
	}
}