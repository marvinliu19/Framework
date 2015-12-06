package cs4620.ray2.shader;

import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Glass extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }


	public Glass() { 
		refractiveIndex = 1.0;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "glass " + refractiveIndex + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Glass shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		double n1, n2;
		Vector3d v = ray.origin.clone().sub(record.location).normalize();
		//Vector3d v = ray.direction.normalize();			// outgoing ray 
		Vector3d norm = record.normal.clone();

		if (v.dot(norm) > 0) {
			// points in the same direction as the normal
			n1 = 1.0;
			n2 = refractiveIndex;
		} else {
			n1 = refractiveIndex;
			n2 = 1.0;
			norm.negate();				// pointing in different directions need to flip normal
		}
		
		// 2) Determine whether total internal reflection occurs.
		boolean totalInternal = false;
		double theta1 = norm.angle(ray.direction);
		
		if (n1 > n2) {
			// https://en.wikipedia.org/wiki/Total_internal_reflection
			double critical = Math.asin(n2/n1);		// angle of incidence greater, total internal refl occurs
			if (theta1 > critical) totalInternal = true;
		} 

        // 3) Compute the reflected ray and refracted ray (if total internal reflection does not occur)
        //    using Snell's law and call RayTracer.shadeRay on them to shade them
		outIntensity.setZero();
		double R = totalInternal ? 1 : fresnelCalc(norm, v, n1, n2);

		//Vector3d reflDir = (norm.clone().sub(v)).mul(2*v.dot(norm));
		Vector3d reflDir = norm.clone().sub(ray.direction).mul(2*ray.direction.dot(norm));
				
		Ray reflected = new Ray();
		reflected.makeOffsetRay();
		reflected.set(record.location, reflDir);
		
		Colord reflColour = new Colord(reflDir);
		
		RayTracer.shadeRay(reflColour, scene,  reflected,  depth+1);
		reflColour.mul(R);
		outIntensity.add(reflColour);
		
		
		if (!totalInternal) {
			// if we don't have total internal reflection, we have to calculate refraction as well
			Ray refracted = new Ray();
			refracted.makeOffsetRay();
			double theta2 = Math.asin((n1/n2)*Math.sin(theta1));
			double constant = (n1/n2)*Math.cos(theta1) - Math.cos(theta2);
			Vector3d refrDir = v.clone().mul(n1/n2).add(norm.clone().mul(constant));
			
			
			refracted.set(record.location, refrDir);
			
			Colord refrColour = new Colord(refrDir);
			RayTracer.shadeRay(refrColour, scene, refracted, depth+1);
			refrColour.mul(1 - R);
			outIntensity.add(refrColour);
		}
				
	}
				
	protected double fresnelCalc(Vector3d normal, Vector3d outgoing, double n1, double n2) {
		// Modified fresnel to take in an arbitrary n1 and n2
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