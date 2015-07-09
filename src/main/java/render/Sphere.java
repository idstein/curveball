package render;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import util.GLUtil;

public class Sphere extends ShaderDrawable {
	private int textureId = GLUtil.loadPNGTexture(getClass()
			.getResourceAsStream("/golf.png"), GL13.GL_TEXTURE0);

	@Override
	public void draw(Matrix4f model, FloatBuffer modelFB, int modelBindLocation) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		super.draw(model, modelFB, modelBindLocation);
	}

	public Sphere(float radius, int slices, int stacks) {
		super();

		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(stacks
				* slices * 3);
		FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(stacks
				* slices * 3);
		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(stacks
				* slices * 4);
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(stacks
				* slices * 2);

		float R = 1.f / (float) (stacks - 1);
		float S = 1.f / (float) (slices - 1);
		int r, s;

		for (r = 0; r < stacks; r++)
			for (s = 0; s < slices; s++) {

				float y = (float) sin(-PI / 2 + PI * r * R);
				float x = (float) (cos(2 * PI * s * S) * sin(PI * r * R));
				float z = (float) (sin(2 * PI * s * S) * sin(PI * r * R));

				verticesBuffer.put(x * radius);
				verticesBuffer.put(y * radius);
				verticesBuffer.put(z * radius);

				normalsBuffer.put(x).put(y).put(z);

				colorsBuffer.put(0.f); // R
				colorsBuffer.put(1.f); // G
				colorsBuffer.put(0.f); // B
				colorsBuffer.put(1.f); // A

				// texture coordinates x between 0 and 1
				textureCoordsBuffer.put((float) (Math.asin(x) / PI + 0.5f))
						.put((float) (Math.asin(y) / PI + 0.5f));
			}

		indicesCount = stacks * slices * 4;
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesCount);
		for (r = 0; r < stacks - 1; r++)
			for (s = 0; s < slices - 1; s++) {
				indicesBuffer.put(r * slices + s);
				indicesBuffer.put(r * slices + (s + 1));
				indicesBuffer.put((r + 1) * slices + (s + 1));
				indicesBuffer.put((r + 1) * slices + s);
			}

		// ================================== 3. Make the data accessible
		// =========================
		bindBuffers(verticesBuffer, colorsBuffer, normalsBuffer,
				textureCoordsBuffer, indicesBuffer);
	}
}
