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
		
		float theta = (float) (2.0 * Math.PI * u);
		float phi = (float) (Math.PI * v);
		
		float cTheta = (float) (2.0 * Math.PI * cU);
		float cPhi = (float) (Math.PI * cV);
		
		float x = (float) (-1.0 * Math.sin(theta) * Math.sin(phi));
		float y = (float) (Math.cos(phi));
		float z = (float) (-1.0 * Math.cos(theta) * Math.sin(phi));
		
		float cX = (float) (-1.0 * Math.sin(cTheta) * Math.sin(cPhi));
		float cY = (float) (Math.cos(cPhi));
		float cZ = (float) (-1.0 * Math.cos(cTheta) * Math.sin(cPhi));
		
		Vector3d cN = new Vector3d(cX, cY, cZ);
		
		Vector3d N = new Vector3d(x, y, z);
		N.normalize();
		Vector3d T = new Vector3d(N.z, 0, -N.x);		
		T.normalize();
		Vector3d B = N.clone().cross(T).normalize();
		
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
		
		outColor.set(new Colord((normal.x + 1)/2, (normal.y + 1)/2, (normal.z + 1)/2));	
	}

}
