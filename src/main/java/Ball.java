import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import render.Cube;
import render.Rectangle;
import render.Sphere;
import util.GLUtil;

public class Ball extends BoundingBox implements MovingObject {

	private final Vector3f mMotion = new Vector3f();
	private final Sphere sphere;
	private final float radius;
	private final float initialZ;

	private Rectangle mRectMarker;

	public float getRadius() {
		return radius;
	}

	public Ball(float radius, float z) {
		super(new Vector3f(0, 0, z), new Vector3f(radius, radius, radius)
				.mul(2));
		initialZ = z;
		this.radius = radius;
		sphere = new Sphere(radius, 20, 20);
		int textureId = GLUtil.loadPNGTexture(
				Cube.class.getResourceAsStream("/follow_up.png"),
				GL13.GL_TEXTURE0);
		this.mRectMarker = new Rectangle(textureId, new Vector3f(2f, 2f, 0),
				.3f);
	}

	public void reset() {
		mMotion.zero();
		center.set(0, 0, initialZ);
	}

	@Override
	public void draw(Matrix4f modelMatrix, FloatBuffer fb, int modelBindLocation) {
		modelMatrix.translation(getCenter()).get(fb);
		GL20.glUniformMatrix4fv(modelBindLocation, false, fb);
		sphere.draw(modelMatrix, fb, modelBindLocation);
		modelMatrix.translation(0, 0, center.z).get(fb);
		GL20.glUniformMatrix4fv(modelBindLocation, false, fb);
		mRectMarker.draw(modelMatrix, fb, modelBindLocation);
	}

	public void move(float secondEllapsed) {
		center.add(mMotion.x * secondEllapsed, mMotion.y * secondEllapsed,
				mMotion.z * secondEllapsed);
		// System.out.printf("Speed is %f new pos (%s)\n", secondEllapsed,
		// center.toString());
	}

	public void bounce(BoundingBox box) {
		// Collision detection; with reflection adjustment. That means
		// motion
		// speed is not changed. Yet any value outside the tube is simply
		// reflected back to the inside.
		if (getRight() >= box.getRight()) {
			mMotion.reflect(BoundingBox.RIGHT_NORMAL_IN);
		} else if (getLeft() <= box.getLeft()) {
			mMotion.reflect(BoundingBox.LEFT_NORMAL_IN);
		}

		if (getTop() >= box.getTop()) {
			mMotion.reflect(BoundingBox.TOP_NORMAL_IN);
		} else if (getBottom() <= box.getBottom()) {
			mMotion.reflect(BoundingBox.BOTTOM_NORMAL_IN);
		}

		if (getNear() >= box.getNear()) {
			mMotion.reflect(BoundingBox.NEAR_NORMAL_IN);
		} else if (getFar() <= box.getFar()) {
			mMotion.reflect(BoundingBox.FAR_NORMAL_IN);
		}
	}

	public Vector3f getPosition() {
		return center;
	}

	public Vector3f getMotion() {
		return mMotion;
	}
}
