package cs4620.ray2.shader;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Light;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;
import java.util.*;

public class CookTorrance extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The roughness controlling the roughness of the surface. */
	protected double roughness = 1.0;
	public void setRoughness(double roughness) { this.roughness = roughness; }

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
	
	public CookTorrance() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "CookTorrance " + diffuseColor + " " + specularColor + " " + roughness + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the CookTorrance shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		// TODO#A7 Fill in this function.
		// create the outgoing ray from where the incoming ray hits the surface
		Vector3d v = ray.origin.clone().sub(record.location).normalize();			// outgoing ray "viewing ray"
		Vector3d l = new Vector3d();												// incoming ray
		
		outIntensity.setZero();

    	// 1) Loop through each light in the scene.
        for (Light light : scene.getLights()) {
        	// 2) If the intersection point is shadowed, skip the calculation for the light.
    		//	  See Shader.java for a useful shadowing function.
        	if (!isShadowed(scene, light, record, new Ray())) {
        		// 3) Compute the incoming direction by subtracting the intersection point from the light's position.
        		l.set(light.getDirection(record.location)).normalize();
        		Vector3d h = l.add(v).normalize();
        		
        		// 4) Compute the color of the point using the CookTorrance shading model. Add this value to the output.
        		// Fresnel:
        		double F = fresnel(record.normal, v, depth);
        		double r2 = light.getRSq(record.location);
        		
        		// Microfacet Distribution
        		double m2 = Math.pow(roughness, 2);
        		double nh = record.normal.dot(h);
        		double expFrac = (Math.pow(nh, 2) - 1)/(m2 * Math.pow(nh, 2));
        		double D = 1/(m2 * Math.pow(nh, 4)) * Math.exp(expFrac);
        		
        		// Geometric Attenuation
        		double nv = record.normal.dot(v);
        		double nl = record.normal.dot(l);
        		double vh = v.dot(h);
        		double G = Math.min(1., Math.min((2.*nh*nv)/vh, (2.*nh*nl)/vh));
        		
        		// Putting it all together
        		Colord ks = specularColor;
        		Colord kd = diffuseColor;
        	
				double stuff = (F/Math.PI) * ((G * D)/(nv * nl));
				Vector3d stuff2 = ks.mul(stuff).add(kd).mul(Math.max(nl, 0.0)).mul(light.intensity.div(r2));

				Colord out = new Colord();					// our output colour calculations
				out.set(stuff2);

        		outIntensity.add(out);			
        	}
        }
        
    }
}
