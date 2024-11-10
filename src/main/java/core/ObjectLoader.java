package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import core.entity.Model;

public class ObjectLoader
{
	private static volatile ObjectLoader instance;

	private final List<Integer> vaos     = new ArrayList<>();
	private final List<Integer> vbos     = new ArrayList<>();
	private final List<Integer> textures = new ArrayList<>();

	private ObjectLoader()
	{}

	static ObjectLoader getInstance()
	{
		if(instance == null) synchronized(ObjectLoader.class)
		{
			if(instance == null) instance = new ObjectLoader();
		}

		return instance;
	}

	public Model loadModel(final float[] vertices, final float[] textureCoords, final float[] normals, final int[] indices)
	{
		final var id = createVao();
		storeIndicesBuffer(indices);
		storeDataInAttribList(0, 3, vertices);
		storeDataInAttribList(1, 2, textureCoords);
		storeDataInAttribList(2, 3, normals);
		unbind();
		return new Model(id, indices.length);
	}

	public Model loadObjModel(final String fileName)
	{
		final var lines = readAllLines(fileName);

		final List<Vector3f> vertices = new ArrayList<>();
		final List<Vector3f> normals  = new ArrayList<>();
		final List<Vector2f> textures = new ArrayList<>();
		final List<Vector3i> faces    = new ArrayList<>();

		for(final String line: lines)
		{
			final var tokens = line.split("\\s+");
			switch(tokens[0])
			{
				case "v":
					final var verticesVec = new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
					vertices.add(verticesVec);
					break;
				case "vt":
					final var texturesVec = new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
					textures.add(texturesVec);
					break;
				case "vn":
					final var normalsVec = new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
					normals.add(normalsVec);
					break;
				case "f":
					processFace(tokens[1], faces);
					processFace(tokens[2], faces);
					processFace(tokens[3], faces);
					break;
				default:
					break;
			}
		}

		final List<Integer> indices     = new ArrayList<>();
		final var           verticesArr = new float[vertices.size() * 3];
		var                 i           = 0;
		for(final Vector3f pos: vertices)
		{
			verticesArr[i * 3]     = pos.x;
			verticesArr[i * 3 + 1] = pos.y;
			verticesArr[i * 3 + 2] = pos.z;
			i++;
		}

		final var texCoordArr = new float[vertices.size() * 2];
		final var normalArr   = new float[vertices.size() * 3];

		for(final Vector3i face: faces)
			processVertex(face.x, face.y, face.z, textures, normals, indices, texCoordArr, normalArr);

		final var indicesArr = indices.stream().mapToInt((final Integer v)->v).toArray();

		return loadModel(verticesArr, texCoordArr, normalArr, indicesArr);
	}

	public String loadResource(final String fileName) throws Exception
	{
		String result;
		try(var in = ObjectLoader.class.getResourceAsStream(fileName); var scanner = new Scanner(in, StandardCharsets.UTF_8.name()))
		{
			result = scanner.useDelimiter("\\A").next();
		}
		return result;
	}

	public int loadTexture(final String filename) throws Exception
	{
		int        width, height;
		ByteBuffer buffer;
		try(var stack = MemoryStack.stackPush())
		{
			final var w = stack.mallocInt(1);
			final var h = stack.mallocInt(1);
			final var c = stack.mallocInt(1);

			buffer = STBImage.stbi_load(filename, w, h, c, 4);
			if(buffer == null) throw new Exception("Image file " + filename + " not loaded. " + STBImage.stbi_failure_reason());

			width  = w.get();
			height = h.get();
		}

		final var id = GL11.glGenTextures();
		textures.add(id);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		STBImage.stbi_image_free(buffer);
		return id;
	}

	private int createVao()
	{
		final var id = GL30.glGenVertexArrays();
		vaos.add(id);
		GL30.glBindVertexArray(id);
		return id;
	}

	private void processFace(final String token, final List<Vector3i> faces)
	{
		final var lineToken = token.split("/");
		final var length    = lineToken.length;
		int       pos       = -1, coords = -1, normal = -1;
		pos = Integer.parseInt(lineToken[0]) - 1;
		if(length > 1)
		{
			final var textCoord = lineToken[1];
			coords = textCoord.length() > 0 ? Integer.parseInt(textCoord) - 1 : -1;
			if(length > 2) normal = Integer.parseInt(lineToken[2]) - 1;
		}
		final var facesVec = new Vector3i(pos, coords, normal);
		faces.add(facesVec);
	}

	private void processVertex(final int pos, final int texCoord, final int normal, final List<Vector2f> texCoordList, final List<Vector3f> normalList, final List<Integer> indicesList,
			final float[] texCoordArr, final float[] normalArr)
	{
		indicesList.add(pos);

		if(texCoord >= 0)
		{
			final var texCoordVec = texCoordList.get(texCoord);
			texCoordArr[pos * 2]     = texCoordVec.x;
			texCoordArr[pos * 2 + 1] = 1 - texCoordVec.y;
		}

		if(normal >= 0)
		{
			final var normalVec = normalList.get(normal);
			normalArr[pos * 3]     = normalVec.x;
			normalArr[pos * 3 + 1] = normalVec.y;
			normalArr[pos * 3 + 2] = normalVec.z;
		}
	}

	private List<String> readAllLines(final String fileName)
	{
		final List<String> list = new ArrayList<>();
		try(var br = new BufferedReader(new InputStreamReader(Class.forName(ObjectLoader.class.getName()).getResourceAsStream(fileName))))
		{
			String line;
			while((line = br.readLine()) != null)
				list.add(line);
		}
		catch(IOException | ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}

	private void storeDataInAttribList(final int attribNo, final int vertexCount, final float[] data)
	{
		final var vbo = GL15.glGenBuffers();
		vbos.add(vbo);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		final var buffer = MemoryUtil.memAllocFloat(data.length);
		buffer.put(data).flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attribNo, vertexCount, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}

	private void storeIndicesBuffer(final int[] indices)
	{
		final var vbo = GL15.glGenBuffers();
		vbos.add(vbo);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vbo);
		final var buffer = MemoryUtil.memAllocInt(indices.length);
		buffer.put(indices).flip();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	private void unbind()
	{
		GL30.glBindVertexArray(0);
	}

	void cleanup()
	{
		for(final int vao: vaos)
			GL30.glDeleteVertexArrays(vao);
		for(final int vbo: vbos)
			GL15.glDeleteBuffers(vbo);
		for(final int texture: textures)
			GL11.glDeleteTextures(texture);
	}
}