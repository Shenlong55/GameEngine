package core;

import java.lang.reflect.Method;
import java.util.Properties;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import core.entity.SceneManager;
import core.rendering.RenderManager;

/**
 * Engine for creating 3D games. Manages the game window, input, loading objects, rendering and the main game loop.
 */
public final class DragonEngine
{
	private static final long ONE_SECOND = 1000000000;

	private static volatile DragonEngine instance;

	private static boolean       clampFramerate;
	private static long          frameTime;
	private static boolean       initialized;
	private static long          tickTime;
	private static InputManager  inputManager;
	private static ObjectLoader  objectLoader;
	private static RenderManager renderManager;
	private static SceneManager  sceneManager;
	private static Window        window;

	private GLFWErrorCallback errorCallback;
	private IGameLogic        gameLogic;
	private Properties        gameProperties;

	private DragonEngine()
	{}

	/**
	 * Returns an instance of the input manager.
	 *
	 * @return InputManager - Input manager instance
	 */
	public static InputManager getInputManager()
	{
		if(!initialized) throw new IllegalStateException("Engine has not been initialized.");
		return inputManager;
	}

	/**
	 * Returns an instance of the engine.
	 *
	 * @return DragonEngine - Engine instance
	 */
	public static DragonEngine getInstance()
	{
		if(instance == null) synchronized(DragonEngine.class)
		{
			if(instance == null) instance = new DragonEngine();
		}

		return instance;
	}

	/**
	 * Returns an instance of the object loader.
	 *
	 * @return ObjectLoader - Object loader instance
	 */
	public static ObjectLoader getObjectLoader()
	{
		if(!initialized) throw new IllegalStateException("Engine has not been initialized.");
		return objectLoader;
	}

	/**
	 * Returns an instance of the render manager.
	 *
	 * @return RenderManager - Render manager instance
	 */
	public static RenderManager getRenderManager()
	{
		if(!initialized) throw new IllegalStateException("Engine has not been initialized.");
		return renderManager;
	}

	/**
	 * Returns an instance of the scene manager.
	 *
	 * @return SceneManager - Scene manager instance
	 */
	public static SceneManager getSceneManager()
	{
		if(!initialized) throw new IllegalStateException("Engine has not been initialized.");
		return sceneManager;
	}

	/**
	 * Updates and returns the game window projection matrix
	 *
	 * @return Matrix4f - Projection matrix for the game window
	 */
	public static Matrix4f getWindowProjectionMatrix()
	{
		if(!initialized) throw new IllegalStateException("Engine has not been initialized.");
		return window.getProjectionMatrix();
	}

	/**
	 * Initializes the engine and starts the game.
	 *
	 * @param gameLogic
	 * @throws Exception
	 */
	public void start(final IGameLogic gameLogic, final Properties gameProperties) throws Exception
	{
		if(initialized) return;
		this.gameLogic      = gameLogic;
		this.gameProperties = gameProperties;

		initialize();
		run();
		cleanup();
	}

	/**
	 * Calls cleanup on the engines components and the game and terminates GLFW.
	 */
	private void cleanup()
	{
		window.cleanup();
		renderManager.cleanup();
		objectLoader.cleanup();
		gameLogic.cleanup();
		errorCallback.free();
		GLFW.glfwTerminate();
	}

	/**
	 * Initializes GLFW, the engines components and the game.
	 *
	 * @throws Exception
	 */
	private void initialize() throws Exception
	{
		//Set a callback function that prints errors to the "standard" error output stream and initialize GLFW
		GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
		if(!GLFW.glfwInit()) throw new IllegalStateException("Unable to initialize GLFW.");

		//Set clampFramerate, frameTime and tickTime
		clampFramerate = Boolean.parseBoolean(gameProperties.getProperty("engine.clamp.framerate"));
		frameTime      = ONE_SECOND / Long.parseLong(gameProperties.getProperty("engine.max.framerate"));
		tickTime       = ONE_SECOND / Long.parseLong(gameProperties.getProperty("engine.tickrate"));

		//Create the game window
		window = new Window(gameProperties);

		//Use reflection to access the getInstance methods of RenderManager and SceneManager because they are in 'sub-packages'
		final Method getRenderManagerInstance = RenderManager.class.getDeclaredMethod("getInstance");
		final Method getSceneManagerInstance  = SceneManager.class.getDeclaredMethod("getInstance");
		getRenderManagerInstance.setAccessible(true);
		getSceneManagerInstance.setAccessible(true);

		//Create instances of the engines components
		inputManager  = InputManager.getInstance();
		objectLoader  = ObjectLoader.getInstance();
		renderManager = (RenderManager) getRenderManagerInstance.invoke(null);
		sceneManager  = (SceneManager) getSceneManagerInstance.invoke(null);

		//Initialize the engines components
		window.initialize();
		inputManager.initialize(window.getHandle());
		renderManager.initialize(objectLoader);

		//TODO make clampFramerate configurable
		clampFramerate = false;
		initialized    = true;

		//Initialize the game
		gameLogic.initialize();
	}

	/**
	 * Runs the main game loop. Processes input, then processes game ticks using a fixed time step, renders as often as possible unless clamp frame
	 * rate is enabled and then updates the FPS/TPS display if a second has passed.+
	 */
	//TODO Review main game loop
	private void run()
	{
		//Initialize game loop variables
		long previousStartTime = System.nanoTime();
		int  frames            = 0;
		int  ticks             = 0;
		long secondCounter     = 0L;
		long unprocessedTime   = 0L;
		long unrenderedTime    = 0L;
		//Run the main game loop until the game window is marked to be closed
		while(!window.windowShouldClose())
		{
			//Set the start time of this run, calculate the time elapsed since the last run started and update the previous start time for the next run
			final long startTime   = System.nanoTime();
			final long elapsedTime = startTime - previousStartTime;
			previousStartTime = startTime;

			//Add the elapsed time to the unprocessed time, unrendered time and second counter
			unprocessedTime += elapsedTime;
			unrenderedTime  += elapsedTime;
			secondCounter   += elapsedTime;

			//Process input
			inputManager.updateMouseData();
			gameLogic.input();

			//Process game ticks using a fixed time step
			while(unprocessedTime > tickTime)
			{
				final long updateStartTime = System.nanoTime();
				unprocessedTime -= tickTime;
				gameLogic.update();
				if(System.nanoTime() - updateStartTime > tickTime) System.out.println("Time to process game update has exceeded tick time.");
				ticks++;
			}

			//Always render unless clamp frame rate option is enabled
			if(!clampFramerate || unrenderedTime > frameTime)
			{
				unrenderedTime = 0;
				window.updateViewport();
				gameLogic.render();
				renderManager.render(sceneManager);
				window.update();
				frames++;
			}

			//Update the FPS/TPS display if a second has passed
			if(secondCounter >= ONE_SECOND)
			{
				window.appendToTitle("fps=" + frames + "/tps=" + ticks);
				frames        = 0;
				ticks         = 0;
				secondCounter = 0;
			}
		}
	}
}