package cs4620.ray2.shader;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Light;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * This interface specifies what is necessary for an object to be a shader.
 * @author ags, pramook
 */
public abstract class Shader {
	
	/**
	 * The material given to all surfaces unless another is specified.
	 */
	public static final Shader DEFAULT_SHADER = new Lambertian();
	
	
	protected Texture texture = null;
	public void setTexture(Texture t) { texture = t; }
	public Texture getTexture() { return texture; }
	
	/**	
	 * Calculate the intensity (color) for this material at the intersection described in
	 * the record contained in workspace.
	 * 	 
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	public abstract void shade(Colord outIntensity, Scene scene, Ray ray, 
			IntersectionRecord record, int depth);
	
	/**
	 * A utility method to check if there is any surface between the given intersection
	 * point and the given light. shadowRay is set to point from the intersection point
	 * towards the light.
	 * 
	 * @param scene The scene in which the surface exists.
	 * @param light A light in the scene.
	 * @param record The intersection point on a surface.
	 * @param shadowRay A ray that is set to point from the intersection point towards
	 * the given light.
	 * @return true if there is any surface between the intersection point and the light;
	 * false otherwise.
	 */
	protected boolean isShadowed(Scene scene, Light light, IntersectionRecord record, Ray shadowRay) {		
		// Setup the shadow ray to start at surface and end at light
		shadowRay.origin.set(record.location);
		shadowRay.direction.set(light.getDirection(record.location));

		double end = light.getShadowRayEnd(record.location);//shadowRay.direction.len();
		shadowRay.direction.normalize();
		
		// Set the ray to end at the light
		shadowRay.makeOffsetSegment(end);
		
		return scene.getAnyIntersection(shadowRay);
	}
	
	protected double fresnel(Vector3d normal, Vector3d outgoing, double refractiveIndex) {
		//TODO#A7 compute the fresnel term using the equation in the lecture
		double n1 = 1;		// assume air according to piazza
		double n2 = refractiveIndex;
		
		if (normal.dot(outgoing) < 0) {
			// normal and outgoing in different directions
			normal.mul(-1.0);
		}
		
		double theta1 = normal.angle(outgoing);
		double theta2 = Math.asin((n1/n2)*Math.sin(theta1));		// use snell's eqn, with n1 = 1 = air

		double Fp = (n2*Math.cos(theta1) - n1*Math.cos(theta2))/(n2*Math.cos(theta1) + n1*Math.cos(theta2));
		double Fs = (n1*Math.cos(theta1) - n2*Math.cos(theta2))/(n1*Math.cos(theta1) + n2*Math.cos(theta2));
		
		double R = 0.5*(Math.pow(Fp, 2) + Math.pow(Fs,  2));
		return R;
	}
}