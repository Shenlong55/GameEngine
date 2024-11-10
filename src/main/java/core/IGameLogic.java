package core;

/**
 * An interface that defines the behaviors that need to be implemented for games using the engine.
 */
public interface IGameLogic
{
	/**
	 * The game method called by the engine during shut down.
	 */
	void cleanup();

	/**
	 * The game method called by the engine immediately after initialization.
	 *
	 * @throws Exception
	 */
	void initialize() throws Exception;

	/**
	 * The first game method called during the main game loop. Used to poll inputs.
	 */
	void input();

	/**
	 * The third game method called during the main game loop. User to render game objects.
	 */
	void render();

	/**
	 * The second game method called during the main game loop. Used to update game objects.
	 */
	void update();
}