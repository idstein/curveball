import org.joml.Vector3f;

public class Plane {

	// Point on plane
	public Vector3f v;

	// Normal of plane
	public Vector3f n;

	public Vector3f getReflection(Vector3f inboundVelocity) {
		float speed = inboundVelocity.length();
		final float dotN = inboundVelocity.negate().dot(n);
		Vector3f newVec = new Vector3f(n).mul(dotN).mul(2).add(inboundVelocity)
				.normalize();
		return newVec.mul(speed);
	}

}
