import static java.lang.Math.abs;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

abstract public class BoundingBox {
	protected Vector3f center;
	protected Vector3f halfwidth;

	public BoundingBox(Vector3f center, Vector3f dimension) {
		this.center = center;
		this.halfwidth = new Vector3f(dimension).mul(.5f);
	}

	public final static Vector3f LEFT_NORMAL_IN = new Vector3f(1, 0, 0);
	public final static Vector3f RIGHT_NORMAL_IN = new Vector3f(-1, 0, 0);
	public final static Vector3f TOP_NORMAL_IN = new Vector3f(0, -1, 0);
	public final static Vector3f BOTTOM_NORMAL_IN = new Vector3f(0, 1, 0);
	public final static Vector3f NEAR_NORMAL_IN = new Vector3f(0, 0, -1);
	public final static Vector3f FAR_NORMAL_IN = new Vector3f(0, 0, 1);

	abstract public void draw(Matrix4f modelMatrix, FloatBuffer fb,
			int modelBindLocation);

	public boolean overlap(BoundingBox box) {
		if (abs(center.x - box.center.x) > (halfwidth.x + box.halfwidth.x))
			return false;
		if (abs(center.y - box.center.y) > (halfwidth.y + box.halfwidth.y))
			return false;
		if (abs(center.z - box.center.z) > (halfwidth.z + box.halfwidth.z))
			return false;

		// We have an overlap
		return true;
	}

	public boolean insideOf(BoundingBox box) {
		if (box.getLeft() < getLeft())
			return false;
		if (box.getRight() > getRight())
			return false;
		if (box.getNear() > getNear())
			return false;
		if (box.getFar() < getFar())
			return false;

		return true;
	}

	public final Vector3f getCenter() {
		return center;
	}

	public float getLeft() {
		return center.x - halfwidth.x;
	}

	public Vector3f getLeftVector() {
		return new Vector3f(getCenter()).sub(halfwidth.x, 0, 0);
	}

	public float getRight() {
		return center.x + halfwidth.x;
	}

	public Vector3f getRightVector() {
		return new Vector3f(getCenter()).add(halfwidth.x, 0, 0);
	}

	public float getTop() {
		return center.y + halfwidth.y;
	}

	public Vector3f getTopVector() {
		return new Vector3f(getCenter()).add(0, halfwidth.y, 0);
	}

	public float getBottom() {
		return center.y - halfwidth.y;
	}

	public Vector3f getBottomVector() {
		return new Vector3f(getCenter()).sub(0, halfwidth.y, 0);
	}

	public float getNear() {
		return center.z + halfwidth.z;
	}

	public Vector3f getNearVector() {
		return new Vector3f(getCenter()).add(0, 0, halfwidth.z);
	}

	public float getFar() {
		return center.z - halfwidth.z;
	}

	public Vector3f getFarVector() {
		return new Vector3f(getCenter()).sub(0, 0, halfwidth.z);
	}
}
