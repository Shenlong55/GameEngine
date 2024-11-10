package game;

import java.util.Properties;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import core.DragonEngine;
import core.IGameLogic;
import core.InputManager;
import core.ObjectLoader;
import core.entity.Entity;
import core.entity.Model;
import core.entity.SceneManager;
import core.entity.Texture;
import core.rendering.Camera;

final class GameLogic implements IGameLogic
{
	private final Vector3f   cameraIncrement;
	private final Properties gameProperties;

	private Vector2f     rotationVector;
	private SceneManager sceneManager;

	public GameLogic(final Properties gameProperties)
	{
		cameraIncrement     = new Vector3f(0, 0, 0);
		rotationVector      = new Vector2f();
		this.gameProperties = gameProperties;
	}

	@Override
	public void cleanup()
	{}

	@Override
	public void initialize() throws Exception
	{
		sceneManager = DragonEngine.getSceneManager();
		//sceneManager.setLightAngle(-90);

		final ObjectLoader objectLoader = DragonEngine.getObjectLoader();
		final Model        model        = objectLoader.loadObjModel("/models/cube.obj");
		model.setTexture(new Texture(objectLoader.loadTexture("textures/blue.png")), 1f);

		//		final var backgroundTexture = new TerrainTexture(objectLoader.loadTexture("textures/terrain.png"));
		//		final var redTexture        = new TerrainTexture(objectLoader.loadTexture("textures/flowers.png"));
		//		final var greenTexture      = new TerrainTexture(objectLoader.loadTexture("textures/stone.png"));
		//		final var blueTexture       = new TerrainTexture(objectLoader.loadTexture("textures/dirt.png"));
		//		final var blendMap          = new TerrainTexture(objectLoader.loadTexture("textures/blendMap.png"));

		//		final var blendMapTerrain = new BlendMapTerrain(backgroundTexture, redTexture, greenTexture, blueTexture);

		//		final var terrain  = new Terrain(new Vector3f(0, 1, -800), objectLoader, new Material(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f), 0.1f), blendMap, blendMapTerrain);
		//		final var terrain2 = new Terrain(new Vector3f(-800, 1, -800), objectLoader, new Material(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f), 0.1f), blendMap, blendMapTerrain);
		//		sceneManager.addTerrain(terrain);
		//		sceneManager.addTerrain(terrain2);

		//		final var random = new Random();
		//		for(var i = 0; i < 2000; i++)
		//		{
		//			final var x = random.nextFloat() * 800;
		//			final var z = random.nextFloat() * -800;
		//			sceneManager.addEntity(new Entity(model, new Vector3f(x, 2, z), new Vector3f(0, 0, 0), 1));
		//		}
		sceneManager.addEntity(new Entity(model, new Vector3f(0, 2, -5f), new Vector3f(0, 0, 0), 1));

		//		final var pointLight = new PointLight(new Vector3f(1, 1, 1), new Vector3f(-0.5f, -0.5f, -3.2f), 1.0f, 0, 0, 1);

		//		final var lightPosition  = new Vector3f(1f, 15f, -5f);
		//		final var lightIntensity = 500f;
		//		final var exponent       = 0.5f;
		//		final var coneDir        = new Vector3f(0, -50, 0);
		//		final var cutoff         = (float) Math.cos(Math.toRadians(140));
		//		final var spotLight1     = new SpotLight(new Vector3f(0.25f, 0f, 0f), lightPosition, lightIntensity, 0f, 0f, exponent, coneDir, cutoff);
		//		final var spotLight2     = new SpotLight(new Vector3f(0f, 0.25f, 0f), lightPosition, lightIntensity, 0f, 0f, exponent, coneDir, cutoff);

		//		final var directionalLight = new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(-1, 0, 0), 1.0f);

		//		sceneManager.setDirectionalLight(directionalLight);
		//		sceneManager.setPointLights(new PointLight[]{pointLight});
		//		sceneManager.setSpotLights(new SpotLight[]{spotLight1, spotLight2});
	}

	@Override
	public void input()
	{
		cameraIncrement.set(0, 0, 0);

		final InputManager inputManager = DragonEngine.getInputManager();
		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_W)) cameraIncrement.z = -1;
		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_S)) cameraIncrement.z = 1;

		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_A)) cameraIncrement.x = -1;
		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_D)) cameraIncrement.x = 1;

		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_Z)) cameraIncrement.y = -1;
		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_X)) cameraIncrement.y = 1;

		if(inputManager.isRightMouseButtonPressed()) rotationVector = inputManager.getMouseDisplayVector();

		//		SpotLight spotLight = sceneManager.getSpotLights()[0];
		//		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_N))
		//		{
		//			spotLight.getPosition().z += 0.1f;
		//		}
		//		if(inputManager.isKeyPressed(GLFW.GLFW_KEY_M))
		//		{
		//			spotLight.getPosition().z -= 0.1f;
		//		}
	}

	@Override
	public void render()
	{
		final Camera camera           = DragonEngine.getRenderManager().getCamera();
		final float  cameraMoveSpeed  = Float.parseFloat(gameProperties.getProperty("camera.move.speed"));
		final float  mouseSensitivity = Float.parseFloat(gameProperties.getProperty("mouse.sensitivity"));
		camera.movePosition(cameraIncrement.x * cameraMoveSpeed, cameraIncrement.y * cameraMoveSpeed, cameraIncrement.z * cameraMoveSpeed);
		camera.moveRotation(rotationVector.x * mouseSensitivity, rotationVector.y * mouseSensitivity, 0);

		//		final var inputManager = DragonEngine.getInputManager();
		//		if(inputManager.isRightMouseButtonPressed())
		//		{
		//			final var rotationVector = inputManager.getMouseDisplayVector();
		//			camera.moveRotation(rotationVector.x * GameProperties.MOUSE_SENSITIVITY, rotationVector.y * GameProperties.MOUSE_SENSITIVITY, 0);
		//			inputManager.setMouseDisplayVector(new Vector2f());
		//		}
	}

	@Override
	public void update()
	{
		// entity.incRotation(0.0f, 0.5f, 0.0f);

		// sceneManager.incSpotAngle(0.75f);
		// if (sceneManager.getSpotAngle() > 9600)
		// {
		// sceneManager.setSpotInc(-1);
		// }
		// else if (sceneManager.getSpotAngle() <= -9600)
		// {
		// sceneManager.setSpotInc(1);
		// }
		//
		// double spotAngleRad = Math.toRadians(sceneManager.getSpotAngle());
		// Vector3f coneDir = sceneManager.getSpotLights()[0].getPosition();
		// coneDir.x = (float) Math.sin(spotAngleRad);
		//
		// coneDir = sceneManager.getSpotLights()[1].getPosition();
		// coneDir.x = (float) Math.cos(spotAngleRad);

		//		sceneManager.incLightAngle(0.25f);
		//		if(sceneManager.getLightAngle() > 90)
		//		{
		//			sceneManager.getDirectionalLight().setIntensity(0);
		//			if(sceneManager.getLightAngle() >= 360) sceneManager.setLightAngle(-90);
		//		}
		//		else if(sceneManager.getLightAngle() <= -80 || sceneManager.getLightAngle() >= 80)
		//		{
		//			final var factor = 1 - (Math.abs(sceneManager.getLightAngle()) - 80) / 10.0f;
		//			sceneManager.getDirectionalLight().setIntensity(factor);
		//			sceneManager.getDirectionalLight().getColor().y = Math.max(factor, 0.9f);
		//			sceneManager.getDirectionalLight().getColor().z = Math.max(factor, 0.5f);
		//		}
		//		else
		//		{
		//			sceneManager.getDirectionalLight().setIntensity(1);
		//			sceneManager.getDirectionalLight().getColor().x = 1;
		//			sceneManager.getDirectionalLight().getColor().y = 1;
		//			sceneManager.getDirectionalLight().getColor().z = 1;
		//		}
		//		final var angRad = Math.toRadians(sceneManager.getLightAngle());
		//		sceneManager.getDirectionalLight().getDirection().x = (float) Math.sin(angRad);
		//		sceneManager.getDirectionalLight().getDirection().y = (float) Math.cos(angRad);
	}
}