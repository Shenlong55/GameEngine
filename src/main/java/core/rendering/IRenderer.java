package core.rendering;

import core.ObjectLoader;
import core.entity.Model;
import core.entity.SceneManager;

public interface IRenderer<T>
{
	int MAX_POINT_LIGHTS = 5;
	int MAX_SPOT_LIGHTS  = 5;

	void bind(final Model model);

	void cleanup();

	void initialize(final ObjectLoader objectLoader) throws Exception;

	void prepare(T t);

	void render(final SceneManager sceneManager);

	void unbind();
}