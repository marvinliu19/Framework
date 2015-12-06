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
		// v points from the surface to the light source
		Vector3d v = ray.origin.clone().sub(record.location).normalize();
		Vector3d norm = record.normal.clone();
		
		Ray reflected = new Ray();
		Ray refracted = new Ray();
		reflected.makeOffsetRay();
		refracted.makeOffsetRay();
		
		double cosTheta1;
		double sinTheta1;
		Vector3d reflDir;
		boolean totalInternal = false;
		
		if (v.dot(norm) > 0) {
			// outside
			n1 = 1.0;
			n2 = refractiveIndex;
			cosTheta1 = norm.dot(v);
			sinTheta1 = Math.sqrt(1 - Math.pow(cosTheta1, 2));
			
			reflDir = norm.clone().sub(v).mul(2*v.dot(norm));
			reflected.set(record.location, reflDir);
			
			totalInternal = sinTheta1 > n2; 
		} else {
			// inside, pointing in different directions so need to flip normal
			n1 = refractiveIndex;
			n2 = 1.0;
			norm.negate();	
			cosTheta1 = norm.dot(v);
			sinTheta1 = Math.sqrt(1 - Math.pow(cosTheta1, 2));
			
			reflDir = norm.clone().sub(v).mul(2*v.dot(norm));
			reflected.set(record.location, reflDir);
			
			totalInternal = sinTheta1 * n1 > 1; 
		}
		
		if (totalInternal) {
			Colord reflColour = new Colord(reflDir);
			RayTracer.shadeRay(reflColour, scene,  reflected,  depth+1);
			outIntensity.add(reflColour);
		} else {
			double sinTheta2 = sinTheta1/n2;
			double cosTheta2 = Math.sqrt(1 - Math.pow(sinTheta2, 2));
			double R = fresnelCalc(norm, v, n1, n2);
			Vector3d refrDir = norm.clone().mul((n1/n2)*cosTheta1 - cosTheta2).sub(v.mul(n1/n2));
			refracted.set(record.location, refrDir);
			
			Colord reflColour = new Colord(reflDir);
			Colord refrColour = new Colord(refrDir);
			RayTracer.shadeRay(reflColour, scene,  reflected,  depth+1);
			RayTracer.shadeRay(refrColour, scene, refracted, depth+1);
			
			outIntensity.setMultiple(R,reflColour).addMultiple(1-R, refrColour);
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