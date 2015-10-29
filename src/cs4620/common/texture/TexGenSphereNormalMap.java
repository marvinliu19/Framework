package cs4620.common.texture;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Matrix4d;
import egl.math.Vector2i;
import egl.math.Vector3d;
import egl.math.Vector4d;

public class TexGenSphereNormalMap extends ACTextureGenerator {
	// 0.5f means that the discs are tangent to each other
	// For greater values discs intersect
	// For smaller values there is a "planar" area between the discs
	private float bumpRadius;
	// The number of rows and columns
	// There should be one disc in each row and column
	private int resolution;
	
	public TexGenSphereNormalMap() {
		this.bumpRadius = 0.5f;
		this.resolution = 10;
		this.setSize(new Vector2i(256));
	}
	
	public void setBumpRadius(float bumpRadius) {
		this.bumpRadius = bumpRadius;
	}
	
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	@Override
	public void getColor(float u, float v, Color outColor) {
		
		float gridSize = 1.0f/resolution;
		float radius = bumpRadius*gridSize;
		
//		System.out.println("Resolution: " + resolution);
//		System.out.println("u: " + u);
//		System.out.println("v: " + v);
		float cU = ((float) Math.round(u * resolution)) / (float) resolution;
		float cV = ((float) Math.round(v * resolution)) / (float) resolution;
		
//		System.out.println("bump U: " + cU);
//		System.out.println("bump V: " + cV);
		
		float phi = (float) (2 * Math.PI * u);
		float theta = (float) (Math.PI * v) - (float) (Math.PI/2);
		
		float cPhi = (float) (2 * Math.PI * cU);
		float cTheta = (float) (Math.PI * cV) - (float) (Math.PI/2);
		
		float x = (float) (Math.cos(theta) * Math.sin(phi));
		float y = (float) (Math.sin(theta));
		float z = (float) (Math.cos(theta) * Math.cos(phi));
		
		float cX = (float) (Math.cos(cTheta) * Math.sin(cPhi));
		float cY = (float) (Math.sin(cTheta));
		float cZ = (float) (Math.cos(cTheta) * Math.cos(cPhi));
		
		Vector3d cN = new Vector3d(cX, cY, cZ);
		
		Vector3d N = new Vector3d(x, y, z);
		Vector3d T = new Vector3d(z, 0, -x);		
		N.normalize();
		T.normalize();
		Vector3d B = T.clone().cross(N).normalize();
		
		Matrix4d TBN = new Matrix4d(
				new Vector4d(T.x, T.y, T.z, 0),
				new Vector4d(B.x, B.y, B.z, 0),
				new Vector4d(N.x, N.y, N.z, 0),
				new Vector4d(0.0, 0.0, 0.0, 1.0));
		
		float uDistFromCenter = cU - u;
		float vDistFromCenter = cV - v;
		
		Vector3d normal = new Vector3d();
		if (Math.sqrt(Math.pow(uDistFromCenter, 2) + Math.pow(vDistFromCenter, 2)) < radius) {
			normal = cN.clone();
		} else {
			normal = N.clone();
		}
		
		TBN.mulDir(normal);
		outColor.set(new Colord(normal));	
	}

}
