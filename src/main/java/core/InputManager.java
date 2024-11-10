package core;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public final class InputManager
{
	private static volatile InputManager instance;

	private final Vector2d mousePreviousPosition, mouseCurrentPosition;

	private Vector2f mouseDisplayVector;
	private boolean  mouseInWindow = false, mouseLeftButtonPress = false, mouseRightButtonPress = false;
	private long     windowHandle;

	private InputManager()
	{
		mousePreviousPosition = new Vector2d(-1);
		mouseCurrentPosition  = new Vector2d();
		mouseDisplayVector    = new Vector2f();
	}

	static InputManager getInstance()
	{
		if(instance == null) synchronized(InputManager.class)
		{
			if(instance == null) instance = new InputManager();
		}

		return instance;
	}

	public Vector2f getMouseDisplayVector()
	{
		return mouseDisplayVector;
	}

	public boolean isKeyPressed(final int keyCode)
	{
		return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
	}

	public boolean isLeftMouseButtonPressed()
	{
		return mouseLeftButtonPress;
	}

	public boolean isRightMouseButtonPressed()
	{
		return mouseRightButtonPress;
	}

	/**
	 * Sets the windowHandle and creates callback functions for when the mouse cursor position updates, when it enters/exits the window and when the
	 * mouse buttons are pressed.
	 *
	 * @param windowHandle
	 */
	void initialize(final long windowHandle)
	{
		this.windowHandle = windowHandle;

		GLFW.glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos)->
		{
			mouseCurrentPosition.x = xpos;
			mouseCurrentPosition.y = ypos;
		});

		GLFW.glfwSetCursorEnterCallback(windowHandle, (window, entered)->
		{
			mouseInWindow = entered;
		});

		GLFW.glfwSetMouseButtonCallback(windowHandle, (window, button, action, mod)->
		{
			mouseLeftButtonPress  = button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS;
			mouseRightButtonPress = button == GLFW.GLFW_MOUSE_BUTTON_2 && action == GLFW.GLFW_PRESS;
		});
	}

	/**
	 * Updates the current position, previous position and display vector of the mouse.
	 */
	void updateMouseData()
	{
		mouseDisplayVector = new Vector2f(0, 0);
		if(mousePreviousPosition.x > 0 && mousePreviousPosition.y > 0 && mouseInWindow)
		{
			final double x = mouseCurrentPosition.x - mousePreviousPosition.x;
			final double y = mouseCurrentPosition.y - mousePreviousPosition.y;
			if(x != 0) mouseDisplayVector.y = (float) x;
			if(y != 0) mouseDisplayVector.x = (float) y;
		}

		mousePreviousPosition.x = mouseCurrentPosition.x;
		mousePreviousPosition.y = mouseCurrentPosition.y;
	}
}