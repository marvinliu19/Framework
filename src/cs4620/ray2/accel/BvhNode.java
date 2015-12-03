package cs4620.ray2.accel;

import cs4620.ray2.Ray;
import egl.math.Vector3d;

/**
 * A class representing a node in a bounding volume hierarchy.
 * 
 * @author pramook 
 */
public class BvhNode {

	/** The current bounding box for this tree node.
	 *  The bounding box is described by 
	 *  (minPt.x, minPt.y, minPt.z) - (maxBound.x, maxBound.y, maxBound.z).
	 */
	public final Vector3d minBound, maxBound;
	
	/**
	 * The array of children.
	 * child[0] is the left child.
	 * child[1] is the right child.
	 */
	public final BvhNode child[];

	/**
	 * The index of the first surface under this node. 
	 */
	public int surfaceIndexStart;
	
	/**
	 * The index of the surface next to the last surface under this node.	 
	 */
	public int surfaceIndexEnd; 
	
	/**
	 * Default constructor
	 */
	public BvhNode()
	{
		minBound = new Vector3d();
		maxBound = new Vector3d();
		child = new BvhNode[2];
		child[0] = null;
		child[1] = null;		
		surfaceIndexStart = -1;
		surfaceIndexEnd = -1;
	}
	
	/**
	 * Constructor where the user can specify the fields.
	 * @param minBound
	 * @param maxBound
	 * @param leftChild
	 * @param rightChild
	 * @param start
	 * @param end
	 */
	public BvhNode(Vector3d minBound, Vector3d maxBound, BvhNode leftChild, BvhNode rightChild, int start, int end) 
	{
		this.minBound = new Vector3d();
		this.minBound.set(minBound);
		this.maxBound = new Vector3d();
		this.maxBound.set(maxBound);
		this.child = new BvhNode[2];
		this.child[0] = leftChild;
		this.child[1] = rightChild;		   
		this.surfaceIndexStart = start;
		this.surfaceIndexEnd = end;
	}
	
	/**
	 * @return true if this node is a leaf node
	 */
	public boolean isLeaf()
	{
		return child[0] == null && child[1] == null; 
	}
	
	/** 
	 * Check if the ray intersects the bounding box.
	 * @param ray
	 * @return true if ray intersects the bounding box
	 */
	public boolean intersects(Ray ray) {
		// TODO#A7: fill in this function.
		// We need to check if our ray intersects with our bounding box
		// Our box is defined by [x_min, x_max] x [y_min, y_max] x [z_min, z_max]
		// Code adapted from 34 Ray Tracing Lecture
		Ray r = new Ray(ray);
		
		double txmin = (minBound.x - r.origin.x)/r.direction.x;
		double txmax = (maxBound.x - r.origin.x)/r.direction.x;
		
		double tymin = (minBound.y - r.origin.y)/r.direction.y;
		double tymax = (maxBound.y - r.origin.y)/r.direction.y;
		
		double tzmin = (minBound.z - r.origin.z)/r.direction.z;
		double tzmax = (maxBound.z - r.origin.z)/r.direction.z;
		
		double txenter = Math.min(txmin, txmax);
		double txexit = Math.max(txmin,  txmax);
		
		double tyenter = Math.min(tymin, tymax);
		double tyexit = Math.max(tymin,  tymax);
		
		double tzenter = Math.min(tzmin, tzmax);
		double tzexit = Math.max(tzmin,  tzmax);
		
		double tenter = Math.max(txenter, Math.max(tyenter, tzenter));
		double texit = Math.min(txexit, Math.min(tyexit, tzexit));
		
        return texit >= tenter;
	}
}
