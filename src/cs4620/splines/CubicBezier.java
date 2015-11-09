package cs4620.splines;

import java.util.ArrayList;

import egl.math.Vector2;
/*
 * Cubic Bezier class for the splines assignment
 */

public class CubicBezier {
	
	// ratio for division of subsegments
	public static final float RATIO = 0.5f;
	
	//This Bezier's control points
	public Vector2 p0, p1, p2, p3;
	
	//Control parameter for curve smoothness
	float epsilon;
	
	//The points on the curve represented by this Bezier
	private ArrayList<Vector2> curvePoints;
	
	//The normals associated with curvePoints
	private ArrayList<Vector2> curveNormals;
	
	//The tangent vectors of this bezier
	private ArrayList<Vector2> curveTangents;
	
	
	/**
	 * 
	 * Cubic Bezier Constructor
	 * 
	 * Given 2-D BSpline Control Points correctly set self.{p0, p1, p2, p3},
	 * self.uVals, self.curvePoints, and self.curveNormals
	 * 
	 * @param bs0 First Bezier Spline Control Point
	 * @param bs1 Second Bezier Spline Control Point
	 * @param bs2 Third Bezier Spline Control Point
	 * @param bs3 Fourth Bezier Spline Control Point
	 * @param eps Maximum angle between line segments
	 */
	public CubicBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, float eps) {
		curvePoints = new ArrayList<Vector2>();
		curveTangents = new ArrayList<Vector2>();
		curveNormals = new ArrayList<Vector2>();
		epsilon = eps;
		
		this.p0 = new Vector2(p0);
		this.p1 = new Vector2(p1);
		this.p2 = new Vector2(p2);
		this.p3 = new Vector2(p3);
		
		tessellate();
	}
	
	// checks if curve needs to be further subdivided or not
	private boolean isLinear(ArrayList<Vector2> curve){
		Vector2 v1 = curve.get(1).clone().sub(curve.get(0));
		Vector2 v2 = curve.get(2).clone().sub(curve.get(1));
		Vector2 v3 = curve.get(3).clone().sub(curve.get(2));
		
		return (v1.angle(v2) < epsilon && v2.angle(v3) < epsilon);
	}
	
	// Adds curve point, tangent, and normal
	// Tangent is 3(p1-p0), approximation of derivative of cubic
	private void draw(ArrayList<Vector2> curve){
		curvePoints.add(curve.get(0));
		Vector2 tangent = curve.get(1).clone().sub(curve.get(0)).mul(3).normalize();
		curveTangents.add(tangent);
		curveNormals.add(new Vector2(tangent.y, -tangent.x));
	}
	
	// Applies de Casteljau's algorithm to approximate the Bezier curve
	private void drawRecBezier(ArrayList<Vector2> curve, int level) {
		if (isLinear(curve) || level == 10) {
			draw(curve);
		} else {
			ArrayList<Vector2> leftCurve = new ArrayList<Vector2>();
			ArrayList<Vector2> rightCurve = new ArrayList<Vector2>();
			
			Vector2 a0 = curve.get(0).clone();
			Vector2 a1 = curve.get(1).clone();
			Vector2 a2 = curve.get(2).clone();
			Vector2 a3 = curve.get(3).clone();
			Vector2 b0 = a0.clone().lerp(a1, RATIO);
			Vector2 b1 = a1.clone().lerp(a2, RATIO);
			Vector2 b2 = a2.clone().lerp(a3, RATIO);
			Vector2 c0 = b0.clone().lerp(b1, RATIO);
			Vector2 c1 = b1.clone().lerp(b2, RATIO);
			Vector2 d0 = c0.clone().lerp(c1, RATIO);
			
			leftCurve.add(a0);
			leftCurve.add(b0);
			leftCurve.add(c0);
			leftCurve.add(d0);
			rightCurve.add(d0);
			rightCurve.add(c1);
			rightCurve.add(b2);
			rightCurve.add(a3);
			
			drawRecBezier(leftCurve, level + 1);
			drawRecBezier(rightCurve, level + 1);
		}
	}
	
    /**
     * Approximate a Bezier segment with a number of vertices, according to an appropriate
     * smoothness criterion for how many are needed.  The points on the curve are written into the
     * array self.curvePoints, the tangents into self.curveTangents, and the normals into self.curveNormals.
     * The final point, p3, is not included, because cubic Beziers will be "strung together".
     */
    private void tessellate() {
    	ArrayList<Vector2> curve = new ArrayList<Vector2>();
    	curve.add(p0);
    	curve.add(p1);
    	curve.add(p2);
    	curve.add(p3);
    	
    	drawRecBezier(curve, 1);
    }
    
    /**
     * @return The points on this cubic bezier
     */
    public ArrayList<Vector2> getPoints() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curvePoints) returnList.add(p.clone());
    	return returnList;
    }
    
    /**
     * @return The tangents on this cubic bezier
     */
    public ArrayList<Vector2> getTangents() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveTangents) returnList.add(p.clone());
    	return returnList;
    }
    
    /**
     * @return The normals on this cubic bezier
     */
    public ArrayList<Vector2> getNormals() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveNormals) returnList.add(p.clone());
    	return returnList;
    }
    
    
    /**
     * @return The references to points on this cubic bezier
     */
    public ArrayList<Vector2> getPointReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curvePoints) returnList.add(p);
    	return returnList;
    }
    
    /**
     * @return The references to tangents on this cubic bezier
     */
    public ArrayList<Vector2> getTangentReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveTangents) returnList.add(p);
    	return returnList;
    }
    
    /**
     * @return The references to normals on this cubic bezier
     */
    public ArrayList<Vector2> getNormalReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveNormals) returnList.add(p);
    	return returnList;
    }
}
