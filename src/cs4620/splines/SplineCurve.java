package cs4620.splines;
import java.util.ArrayList;

import cs4620.mesh.MeshData;
import egl.NativeMem;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;
import egl.math.Vector3i;
import egl.math.Vector4;


public abstract class SplineCurve {
	private float epsilon;
	
	//Spline Control Points
	private ArrayList<Vector2> controlPoints;
	
	//Bezier Curves that make up this Spline
	private ArrayList<CubicBezier> bezierCurves;
	
	//Whether or not this curve is a closed curve
	private boolean isClosed;
	
	public static final float DIST_THRESH = 0.15f;
	public static final int MIN_OPEN_CTRL_POINTS= 4,
			                           MIN_CLOSED_CTRL_POINTS= 3,
			                           MAX_CTRL_POINTS= 20;

	public SplineCurve(ArrayList<Vector2> controlPoints, boolean isClosed, float epsilon) throws IllegalArgumentException {
		if(isClosed) {
			if(controlPoints.size() < MIN_CLOSED_CTRL_POINTS)
				throw new IllegalArgumentException("Closed Splines must have at least 3 control points.");
		} else {
			if(controlPoints.size() < MIN_OPEN_CTRL_POINTS)
				throw new IllegalArgumentException("Open Splines must have at least 4 control points.");
		}

		this.controlPoints = controlPoints;
		this.isClosed = isClosed;
		this.epsilon = epsilon;
		setBeziers();
	}
	
	public boolean isClosed() {
		return this.isClosed;
	}
	
	public boolean setClosed(boolean closed) {
		if(this.isClosed && this.controlPoints.size() == 3) {
			System.err.println("You must have at least 4 control points to make an open spline.");
			return false;
		}
		this.isClosed= closed;
		setBeziers();
		return true;
	}
	
	public ArrayList<Vector2> getControlPoints() {
		return this.controlPoints;
	}
	
	public void setControlPoint(int index, Vector2 point) {
		this.controlPoints.set(index, point);
		setBeziers();
	}
	
	public boolean addControlPoint(Vector2 point) {
		if(this.controlPoints.size() == MAX_CTRL_POINTS) {
			System.err.println("You can only have "+ SplineCurve.MAX_CTRL_POINTS + " control points per spline.");
			return false;
		}
		/* point= (x0, y0), prev= (x1, y1), curr= (x2,y2)
		 * 
		 * v= [ (y2-y1), -(x2-x1) ]
		 * 
		 * r= [ (x1-x0), (y1-y0) ]
		 * 
		 * distance between point and line prev -> curr is v . r
		 */
		Vector2 curr, prev;
		Vector2 r= new Vector2(), v= new Vector2();
		float distance= Float.POSITIVE_INFINITY;
		int index= -1;
		for(int i= 0; i < controlPoints.size(); i++) {
			curr= controlPoints.get(i);
			if(i == 0) {
				if(isClosed) {
					// add line between first and last ctrl points
					prev= controlPoints.get(controlPoints.size()-1);
				} else {
					continue;
				}
			} else {
				prev= controlPoints.get(i-1);
			}
			v.set(curr.y-prev.y, -(curr.x-prev.x)); v.normalize();
			r.set(prev.x-point.x, prev.y-point.y);
			float newDist = Math.abs(v.dot(r));
			Vector2 v2 = curr.clone().sub(prev);
			v2.mul(1.0f / v2.lenSq());
			float newParam = -v2.dot(r);
			if(newDist < DIST_THRESH && newDist <= distance && 0 < newParam && newParam < 1) {
				distance= newDist;
				index= i;
			}
		}
		
		if (index >= 0) {
			controlPoints.add(index, point);
			setBeziers();
			return true;
		}
		System.err.println("Invalid location, try selecting a point closer to the spline.");
		return false;
	}
	
	public boolean removeControlPoint(int index) {
		if(this.isClosed) {
			if(this.controlPoints.size() == MIN_CLOSED_CTRL_POINTS) {
				System.err.println("You must have at least "+MIN_CLOSED_CTRL_POINTS+" for a closed Spline.");
				return false;
			}
		} else {
			if(this.controlPoints.size() == MIN_OPEN_CTRL_POINTS) {
				System.err.println("You must have at least "+MIN_OPEN_CTRL_POINTS+" for an open Spline.");
				return false;
			}
		}
		this.controlPoints.remove(index);
		setBeziers();
		return true;
	}
	
	public void modifyEpsilon(float newEps) {
		epsilon = newEps;
		setBeziers();
	}
	
	public float getEpsilon() {
		return epsilon;
	}
	
	/**
	 * Returns the sequence of 2D vertices on this Spline specified by the sequence of Bezier curves
	 */
	public ArrayList<Vector2> getPoints() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : bezierCurves)
			for(Vector2 p : b.getPoints())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Returns the sequence of normals on this Spline specified by the sequence of Bezier curves
	 */
	public ArrayList<Vector2> getNormals() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : bezierCurves)
			for(Vector2 p : b.getNormals())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Returns the sequence of tangents on this Spline specified by the sequence of Bezier curves
	 */
	public ArrayList<Vector2> getTangents() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : bezierCurves)
			for(Vector2 p : b.getTangents())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Using this.controlPoints, create the CubicBezier objects that make up this curve and
	 * save them to this.bezierCurves. Assure that the order of the Bezier curves that you
	 * add to bezierCurves is the order in which the overall Spline is chained together.
	 * If the spline is closed, include additional CubicBeziers to account for this.
	 */
	private void setBeziers() {
		this.bezierCurves = new ArrayList<CubicBezier>();
		if (isClosed) {
			ArrayList<Vector2> controlPoints2 = new ArrayList<Vector2>();
			for (Vector2 p: controlPoints) {
				controlPoints2.add(p.clone());
			}
			
			controlPoints2.add(controlPoints.get(0).clone());
			controlPoints2.add(controlPoints.get(1).clone());
			controlPoints2.add(controlPoints.get(2).clone());
			
			for (int i = 1; i <= controlPoints2.size() - 3; i++) {
				Vector2 p0 = controlPoints2.get(i-1);
				Vector2 p1 = controlPoints2.get(i);
				Vector2 p2 = controlPoints2.get(i+1);
				Vector2 p3 = controlPoints2.get(i+2);
				
				bezierCurves.add(this.toBezier(p0, p1, p2, p3, epsilon));
			}
		} else {
			for (int i = 1; i <= controlPoints.size() - 3; i++) {
				Vector2 p0 = controlPoints.get(i-1);
				Vector2 p1 = controlPoints.get(i);
				Vector2 p2 = controlPoints.get(i+1);
				Vector2 p3 = controlPoints.get(i+2);
				
				bezierCurves.add(this.toBezier(p0, p1, p2, p3, epsilon));
			}
		}
	}
	
	/**
	 * Reverses the tangents and normals associated with this Spline
	 */
	public void reverseNormalsAndTangents() {
		for(CubicBezier b : bezierCurves) {
			for(Vector2 p : b.getNormalReferences())
				p.mul(-1);
			for(Vector2 p : b.getTangentReferences())
				p.mul(-1);
		}
	}
	
	//Debug code
	public double getMaxAngle() {
		ArrayList<Vector2> myPoints = getPoints();
		double max = 0;
		for(int i = 0; i < myPoints.size() - 2; ++i) {
			Vector2 A = myPoints.get(i);
			Vector2 B = myPoints.get(i+1);
			Vector2 C = myPoints.get(i+2);
			
			Vector2 v1 = B.clone().sub(A);
			Vector2 v2 = C.clone().sub(B);
			
			v1.normalize();
			v2.normalize();
			
			double cur = Math.acos(v1.dot(v2));
			if (cur > max) max = cur;
		}
		return max;
	}
	
	
	public abstract CubicBezier toBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps);
	
	
	/**
	 * Given a curve that defines the cross section along the axis, fill the three GLBuffer objects appropriately.
	 * Here, we revolve the crossSection curve about the positive Z-axis.
	 * @param crossSection, the 2D spline for which every point defines the cross section of the surface
	 * @param data, a MeshData where we will output our triangle mesh
	 * @param scale > 0, parameter that controls how much the resulting surface should be scaled
	 * @param sliceTolerance > 0, the maximum angle in radians between adjacent vertical slices.
	 */
	public static void build3DRevolution(SplineCurve crossSection, MeshData data, float scale, float sliceTolerance) {
		// Calculate Vertex And Index Count
		ArrayList<Vector2> points = crossSection.getPoints();
		
		int vertsPerSlice = points.size();
		int numSlices = (int) Math.ceil(2*Math.PI / sliceTolerance);
		data.vertexCount = (numSlices+1)*(vertsPerSlice+1);
		int tris = numSlices*vertsPerSlice* 2;
		data.indexCount = tris * 3;

		// Create Storage Spaces
		data.positions = NativeMem.createFloatBuffer(data.vertexCount * 3);
		data.uvs = NativeMem.createFloatBuffer(data.vertexCount * 2);
		data.normals = NativeMem.createFloatBuffer(data.vertexCount * 3);
		data.indices = NativeMem.createIntBuffer(data.indexCount);
		
		// Traverse Up The curve
		for(int p = 0; p < vertsPerSlice + 1; p++) {
			int j = p%(points.size());
			
			float z = points.get(j).y * scale;
			float r = points.get(j).x * scale;
						
			for(int i = 0; i < numSlices + 1; i++) {
				float theta = (float) (i * (2*Math.PI/ (float) numSlices));
				float x = (float) (r * Math.cos(theta));
				float y = (float) (r * Math.sin(theta));
				data.positions.put(x); data.positions.put(y); data.positions.put(z);
			}
		}
	
		
		// Create The Indices
		for(int i = 0; i < vertsPerSlice; i++) {
			int si = i * (numSlices+1);
			
			for(int j = 0; j < numSlices; j++) {
				data.indices.put(si + j);
				data.indices.put(si + j + 1);
				data.indices.put(si + j + 1 + numSlices);
				
				data.indices.put(si + j);
				data.indices.put(si + j + 1 + numSlices);
				data.indices.put(si + j + numSlices);
			}
		}
		
		
		// instantiate array of vertex normals with zero vectors
		Vector3[] normals = new Vector3[data.vertexCount];
		for (int i = 0; i < normals.length; i++) {
			normals[i] = new Vector3(0,0,0);
		}
		
		// for each triangle (a set of 3 indices from data.indices)
		for (int i = 0; i < data.indexCount; i+=3) {
			// get the index of the first vertex of this triangle
			// get the (x,y,z) of the first vertex of this triangle
			int v1i = data.indices.get(i);
			Vector3 v1 = new Vector3(data.positions.get(3*v1i),data.positions.get(3*v1i+1),data.positions.get(3*v1i+2));
			
			int v2i = data.indices.get(i+1);
			Vector3 v2 = new Vector3(data.positions.get(3*v2i),data.positions.get(3*v2i+1),data.positions.get(3*v2i+2));
			
			int v3i = data.indices.get(i+2);
			Vector3 v3 = new Vector3(data.positions.get(3*v3i),data.positions.get(3*v3i+1),data.positions.get(3*v3i+2));
			
			// calculate the two vectors of the triangle and cross them to find the normal of the triangle
			Vector3 p = v2.sub(v1);
			Vector3 q = v3.sub(v1);
			Vector3 normal = p.cross(q);
			
			// add the normal of the triangle to the normals of all vertices in the triangle
			normals[v1i] = normals[v1i].add(normal);
			normals[v2i] = normals[v2i].add(normal);
			normals[v3i] = normals[v3i].add(normal);	
		}
		
		// normalize each vertex normal and output it
		for (int i = 0; i < normals.length; i++) {
			normals[i] = normals[i].normalize();
			data.normals.put(normals[i].x);
			data.normals.put(normals[i].y);
			data.normals.put(normals[i].z);
		}

	}
}

