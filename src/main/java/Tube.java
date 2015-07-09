import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import render.Cube;

public class Tube extends BoundingBox {
	private Cube cube;
	FloatBuffer modelFB = BufferUtils.createFloatBuffer(16);

	public Tube(float width, float height, float near, float far) {
		super(new Vector3f(0, 0, (near + far) / 2), new Vector3f(width, height,
				Math.abs(far - near)));
		cube = new Cube(-width / 2, width / 2, -height / 2, height / 2, near,
				far, 6);
	}

	public void draw(Matrix4f modelMatrix, FloatBuffer fb, int modelBindLocation) {
		modelMatrix.translation(getCenter()).get(modelFB);
		GL20.glUniformMatrix4fv(modelBindLocation, false, modelFB);
		cube.draw(modelMatrix, fb, modelBindLocation);
	}
}
