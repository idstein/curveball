package render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import util.GLUtil;

/**
 * A Rectangle primitve that will render a rectangle of the size (1,1). X and Y
 * define how many vertices are used in each direction.
 * 
 * @author idstein
 *
 */
public class Rectangle extends ShaderDrawable {

	private int width = 2;
	private int height = 2;

	private int textureId = GLUtil.loadPNGTexture(getClass()
			.getResourceAsStream("/paddle.png"), GL13.GL_TEXTURE0);

	@Override
	public void draw(Matrix4f model, FloatBuffer modelFB, int modelBindLocation) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		super.draw(model, modelFB, modelBindLocation);
	}

	public Rectangle(boolean positiveNormal, Vector3f dimension, int resolution) {
		this(positiveNormal, dimension, resolution, 0.5f);
	}

	public Rectangle(boolean positiveNormal, Vector3f dimension,
			int resolution, float alpha) {
		super();
		this.width = Math.max(resolution, 2);
		this.height = Math.max(resolution, 2);

		final float left = -dimension.x / 2;
		final float bottom = -dimension.y / 2;

		final float[] normal = { 0, 0, positiveNormal ? 1 : -1 };
		final float[] color = { 0, positiveNormal ? 0 : 1, 1,
				positiveNormal ? alpha : 1 };

		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(3 * width
				* height);
		FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(3 * width
				* height);
		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(4 * width
				* height);
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(2
				* width * height);

		final float heightStep = dimension.y / ((float) height - 1.f);
		final float widthStep = dimension.x / ((float) width - 1.f);

		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				verticesBuffer.put(left + column * widthStep); // X
				verticesBuffer.put(bottom + row * heightStep); // Y
				verticesBuffer.put(dimension.z); // Z

				normalsBuffer.put(normal);
				colorsBuffer.put(color);

				textureCoordsBuffer.put((float) row / (float) (height - 1));
				textureCoordsBuffer.put((float) column / (float) (width - 1));
			}

		}

		IntBuffer indicesBuffer = BufferUtils.createIntBuffer((2 * width + 2)
				* (height - 1) - 2);
		indicesCount = (2 * width + 2) * (height - 1) - 2;
		for (int row = 0; row < height - 1; row++) {

			if (row > 0) {
				indicesBuffer.put(row * width);
			}

			for (int column = 0; column < width; column++) {
				// OpenGL expects to draw the first vertices in counter
				// clockwise order
				// by default
				indicesBuffer.put(column + row * width);
				indicesBuffer.put(column + (row + 1) * width);
			}
			if (row < (height - 2))
				indicesBuffer.put((width - 1) + (row + 1) * width);
		}

		bindBuffers(verticesBuffer, colorsBuffer, normalsBuffer,
				textureCoordsBuffer, indicesBuffer);
	}

	public Rectangle(int texture, Vector3f dimension, float alpha) {
		this(true, dimension, 2, alpha);
		textureId = texture;
	}
}
