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
		// TODO#A7: fill in this function.
		double n1, n2;
		Vector3d v = ray.direction.normalize();			// outgoing ray "viewing ray"
		Vector3d norm = record.normal.clone();
		
        // 1) Determine whether the ray is coming from the inside of the surface or the outside.
		if (v.dot(norm) < 0) {
			// points in same direction of normal
			n1 = refractiveIndex;
			n2 = 1.0;
			norm.mul(-1);
			//System.out.println("here");
		} else {
			n1 = 1.0;
			n2 = refractiveIndex;
		}
		
		boolean totalInternal = false;
		// 2) Determine whether total internal reflection occurs.
		double theta1 = norm.angle(v);
		//System.out.println("Theta1" + theta1);
		
		if (n1 > n2) {
			// https://en.wikipedia.org/wiki/Total_internal_reflection
			double critical = Math.asin(n2/n1);		// angle of incidence greater, total internal refl occurs
			if (theta1 > critical) totalInternal = true;
			System.out.println("Theta1" + theta1 + " Critical" + critical);
		} else {
			System.out.println("n2 can't be greater than n1 by Snell's law");
		}
		
		//System.out.println(totalInternal);

        // 3) Compute the reflected ray and refracted ray (if total internal reflection does not occur)
        //    using Snell's law and call RayTracer.shadeRay on them to shade them
		outIntensity.setZero();
		double R = totalInternal ? 1 : fresnel(norm, v, n1);

		Vector3d reflDir = (norm.clone().sub(v)).mul(2*v.dot(norm));
				
		Ray reflected = new Ray();
		reflected.set(record.location, reflDir);
		
		Colord reflColour = new Colord(reflDir);
		
		RayTracer.shadeRay(reflColour, scene,  reflected,  depth+1);
		reflColour.mul(R);
		outIntensity.add(reflColour);
		
		if (!totalInternal) {
			// if we don't have total internal reflection, we have to calculate refraction as well
			Ray refracted = new Ray();
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
	

}