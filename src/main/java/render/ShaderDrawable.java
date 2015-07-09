package render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public abstract class ShaderDrawable {

	protected int vaoId;
	protected int vboiId;
	protected int indicesCount;

	public int getVertexArrayObject() {
		return vaoId;
	}

	public void draw(Matrix4f model, FloatBuffer modelFB, int modelBindLocation) {
		model.get(modelFB);
		GL20.glUniformMatrix4fv(modelBindLocation, false, modelFB);
		// Bind to the VAO that has all the information about the vertices
		GL30.glBindVertexArray(getVertexArrayObject());
		GL20.glEnableVertexAttribArray(0); // vertices
		GL20.glEnableVertexAttribArray(1); // colors
		GL20.glEnableVertexAttribArray(2); // normals
		GL20.glEnableVertexAttribArray(3); // texture coordinates

		// Bind to the index VBO that has all the information about the order of
		// the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, getVertexBufferObject());

		// Draw the vertices
		GL11.glDrawElements(GL11.GL_TRIANGLE_STRIP, getIndicesCount(),
				GL11.GL_UNSIGNED_INT, 0);

		// Put everything back to default (deselect)
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
	}

	protected void bindBuffers(FloatBuffer verticesBuffer,
			FloatBuffer colorsBuffer, FloatBuffer normalsBuffer,
			FloatBuffer textureCoordsBuffer, IntBuffer indicesBuffer) {
		normalsBuffer.flip();
		verticesBuffer.flip();
		indicesBuffer.flip();
		colorsBuffer.flip();
		textureCoordsBuffer.flip();

		// Create a new Vertex Array Object in memory and select it (bind)
		// A VAO can have up to 16 attributes (VBO's) assigned to it by default
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object (VBO) in memory and select it
		// (bind)
		// A VBO is a collection of Vectors which in this case resemble the
		// location of each vertex.
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer,
				GL15.GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - COLORS
		int vbocId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer,
				GL15.GL_STATIC_DRAW);
		// index 1, in 0 are the vertices stored; 4 values (RGAB) instead of 3
		// (XYZ)
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - NORMALS
		int vbonId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbonId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, normalsBuffer,
				GL15.GL_STATIC_DRAW);
		// index 2, 3 values (XYZ)
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, true, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO and select it (bind) - TEXTURE COORDS
		int vbotId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer,
				GL15.GL_STATIC_DRAW);
		// index 3, 2 values (ST)
		GL20.glVertexAttribPointer(3, 2, GL11.GL_FLOAT, true, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - INDICES
		vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer,
				GL15.GL_STATIC_DRAW);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
	}

	public int getIndicesCount() {
		return indicesCount;
	}

	public int getVertexBufferObject() {
		return vboiId;
	}

}
