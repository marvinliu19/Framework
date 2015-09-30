package cs4620.ray1.shader;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Light;
import cs4620.ray1.Ray;
import cs4620.ray1.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Phong extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }
	public Colord getDiffuseColor() {return new Colord(diffuseColor);}

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }
	public Colord getSpecularColor() {return new Colord(specularColor);}

	/** The exponent controlling the sharpness of the specular reflection. */
	protected double exponent = 1.0;
	public void setExponent(double exponent) { this.exponent = exponent; }
	public double getExponent() {return exponent;}

	public Phong() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "phong " + diffuseColor + " " + specularColor + " " + exponent + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Phong shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record) {
		// TODO#A2: Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for the light.
		//	  See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		//    the intersection point from the light's position.
		// 4) Compute the color of the point using the Phong shading model. Add this value
		//    to the output.
		outIntensity.setZero();
		Vector3d v = ray.origin.clone().sub(record.location).normalize();
		
		for (Light light : scene.getLights()) {
			
			if (!isShadowed(scene, light, record, new Ray())) {
				
				Vector3d l = light.position.clone().sub(record.location).normalize();
				double r2 = light.position.distSq(record.location);
				
				Vector3d h = v.add(l).normalize();
				
				double diffProp = Math.max(0, record.normal.dot(l));				
				double specProp = Math.pow(Math.max(0, record.normal.dot(h)), exponent);
				
				Colord color = new Colord();
				color.set(diffuseColor.clone().mul(diffProp).addMultiple(specProp, specularColor.clone()).mul(light.intensity).div(r2));
			
				outIntensity.add(color);
				
			}
		}
		
	}

}
