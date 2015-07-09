package render;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import util.GLUtil;

public class Cube extends ShaderDrawable {

	class Plane {
		Vector3f v;
		Vector3f n;
	}

	Matrix4f matrix = new Matrix4f();

	Rectangle[] planes = new Rectangle[4];

	@Override
	public void draw(Matrix4f model, FloatBuffer modelFB, int modelBindLocation) {
		model.get(matrix);
		model.translate(-1f, 0, 0f).rotateY(-90f).get(modelFB);
		GL20.glUniformMatrix4fv(modelBindLocation, false, modelFB);
		planes[0].draw(model, modelFB, modelBindLocation);

		model.rotateY(90f).translate(2f, 0, 0).rotateY(90f).get(modelFB);
		GL20.glUniformMatrix4fv(modelBindLocation, false, modelFB);
		planes[1].draw(model, modelFB, modelBindLocation);

		model.rotateY(-90f).translate(-1f, 1f, 0).rotateX(-90f).get(modelFB);
		GL20.glUniformMatrix4fv(modelBindLocation, false, modelFB);
		planes[2].draw(model, modelFB, modelBindLocation);

		model.rotateX(90f).translate(0f, -2f, 0).rotateX(90f).get(modelFB);
		GL20.glUniformMatrix4fv(modelBindLocation, false, modelFB);
		planes[3].draw(model, modelFB, modelBindLocation);
	}

	public Cube(float left, float right, float bottom, float top, float near,
			float far, int slices) {
		super();
		int textureId = GLUtil.loadPNGTexture(
				Cube.class.getResourceAsStream("/metal.png"), GL13.GL_TEXTURE0);
		planes[0] = new Rectangle(textureId, new Vector3f(far - near, top
				- bottom, 0), 1.f); // LEFT
		planes[1] = new Rectangle(textureId, new Vector3f(far - near, top
				- bottom, 0), 1.f); // RIGHT
		planes[2] = new Rectangle(textureId, new Vector3f(right - left, far
				- near, 0), 1.f); // TOP
		planes[3] = new Rectangle(textureId, new Vector3f(right - left, far
				- near, 0), 1.f); // BOTTOM
	}
}
