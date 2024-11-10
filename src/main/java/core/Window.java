package core;

import java.util.Properties;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

final class Window
{
	private final float    fieldOfView;
	private final Matrix4f projectionMatrix;
	private final String   title;
	private final boolean  vSync;
	private final float    zFar;
	private final float    zNear;

	private long    handle;
	private boolean resized;
	private int     width, height;

	/**
	 * @param gameProperties
	 */
	Window(final Properties gameProperties)
	{
		if(gameProperties == null)
		{
			//Set window properties to default values
			fieldOfView = (float) Math.toRadians(60);
			height      = 0;
			title       = "DRAGON ENGINE";
			vSync       = false;
			width       = 0;
			zFar        = 0.01f;
			zNear       = 1000f;
		}
		else
		{
			fieldOfView = (float) Math.toRadians(Integer.parseInt(gameProperties.getProperty("window.field.of.view")));
			height      = Integer.parseInt(gameProperties.getProperty("window.height"));
			title       = gameProperties.getProperty("game.title");
			vSync       = Boolean.parseBoolean(gameProperties.getProperty("window.vsync"));
			width       = Integer.parseInt(gameProperties.getProperty("window.width"));
			zFar        = Float.parseFloat(gameProperties.getProperty("window.z.far"));
			zNear       = Float.parseFloat(gameProperties.getProperty("window.z.near"));
		}

		projectionMatrix = new Matrix4f();
	}

	//TODO Review this method
	void appendToTitle(final String string)
	{
		GLFW.glfwSetWindowTitle(handle, title + ": " + string);
	}

	void cleanup()
	{
		GLFW.glfwDestroyWindow(handle);
	}

	long getHandle()
	{
		return handle;
	}

	Matrix4f getProjectionMatrix()
	{
		return projectionMatrix.setPerspective(fieldOfView, (float) width / height, zNear, zFar);
	}

	String getTitle()
	{
		return title;
	}

	void initialize()
	{
		//Use window hints to set initial window state
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_TRUE);

		if(width == 0 || height == 0)
		{
			//Set window state to maximized
			//TODO Should both of these values be 100?
			width  = 100;
			height = 100;
			GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
		}

		//Create window and throw an error if unsuccessful
		handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
		if(handle == MemoryUtil.NULL) throw new RuntimeException("Failed to create GLFW window.");

		//Set a callback function to update the width and height of this object when the window is resized
		GLFW.glfwSetFramebufferSizeCallback(handle, (window, width, height)->
		{
			this.width  = width;
			this.height = height;
			resized     = true;
		});

		//Set a callback function to close the window when the escape key is released
		GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods)->
		{
			if(key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) GLFW.glfwSetWindowShouldClose(window, true);
		});

		//Make the context of the window current and set vSync if enabled
		GLFW.glfwMakeContextCurrent(handle);
		if(vSync) GLFW.glfwSwapInterval(1);

		//Finish setting up OpenGL
		GL.createCapabilities();
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	void update()
	{
		GLFW.glfwSwapBuffers(handle);
		GLFW.glfwPollEvents();
	}

	void updateViewport()
	{
		if(resized)
		{
			GL11.glViewport(0, 0, width, height);
			resized = false;
		}
	}

	boolean windowShouldClose()
	{
		return GLFW.glfwWindowShouldClose(handle);
	}
}