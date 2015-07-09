import static java.lang.Math.abs;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

import render.Rectangle;

public class Paddle extends BoundingBox implements MovingObject {

	final private Rectangle mRect;
	final private Vector3f mMotion = new Vector3f();

	public Paddle(boolean playable, float width, float height, float z) {
		super(new Vector3f(0, 0, z), new Vector3f(width, height, 0));
		mRect = new Rectangle(playable, new Vector3f(width, height, 0), 4);
	}

	@Override
	public void draw(Matrix4f modelMatrix, FloatBuffer fb, int modelBindLocation) {
		modelMatrix.translation(getCenter()).get(fb);
		GL20.glUniformMatrix4fv(modelBindLocation, false, fb);
		mRect.draw(modelMatrix, fb, modelBindLocation);
	}

	@Override
	public boolean overlap(BoundingBox box) {
		if (abs(center.x - box.center.x) > (halfwidth.x + box.halfwidth.x))
			return false;
		if (abs(center.y - box.center.y) > (halfwidth.y + box.halfwidth.y))
			return false;

		// We have an overlap
		return true;
	}

	public void move(BoundingBox box, float xrel, float yrel) {
		center.add(xrel, yrel, 0);
		if (!insideOf(box)) {
			center.x = Math.max(box.getLeft() + halfwidth.x,
					Math.min(box.getRight() - halfwidth.x, center.x));

			center.y = Math.max(box.getBottom() + halfwidth.y,
					Math.min(box.getTop() - halfwidth.y, center.y));
		}
	}

	public Vector3f getPosition() {
		return center;
	}

	public boolean hit(Ball ball) {
		return hit(ball.center);
	}

	public boolean hit(Vector3f center) {
		if (center.x < getLeft())
			return false;
		if (center.x > getRight())
			return false;
		if (center.y > getTop())
			return false;
		if (center.y < getBottom())
			return false;
		return true;
	}

	public Vector3f getMotion() {
		return mMotion;
	}
}
