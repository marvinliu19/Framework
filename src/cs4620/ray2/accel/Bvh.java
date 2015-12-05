
package cs4620.ray2.accel;

import java.util.Arrays;
import java.util.Comparator;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.surface.Surface;
import egl.math.Vector3d;

/**
 * Class for Axis-Aligned-Bounding-Box to speed up the intersection look up time.
 *
 * @author ss932, pramook
 */
public class Bvh implements AccelStruct {   
	/** A shared surfaces array that will be used across every node in the tree. */
	private Surface[] surfaces;

	/** A comparator class that can sort surfaces by x, y, or z coordinate.
	 *  See the subclass declaration below for details.
	 */
	static MyComparator cmp = new MyComparator();
	
	/** The root of the BVH tree. */
	BvhNode root;

	public Bvh() { }

	/**
	 * Set outRecord to the first intersection of ray with the scene. Return true
	 * if there was an intersection and false otherwise. If no intersection was
	 * found outRecord is unchanged.
	 *
	 * @param outRecord the output IntersectionRecord
	 * @param ray the ray to intersect
	 * @param anyIntersection if true, will immediately return when found an intersection
	 * @return true if and intersection is found.
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {
		return intersectHelper(root, outRecord, rayIn, anyIntersection);
	}
	
	/**
	 * A helper method to the main intersect method. It finds the intersection with
	 * any of the surfaces under the given BVH node.  
	 *   
	 * @param node a BVH node that we would like to find an intersection with surfaces under it
	 * @param outRecord the output InsersectionMethod
	 * @param rayIn the ray to intersect
	 * @param anyIntersection if true, will immediately return when found an intersection
	 * @return true if an intersection is found with any surface under the given node
	 */
	private boolean intersectHelper(BvhNode node, IntersectionRecord outRecord, Ray rayIn, boolean anyIntersection) {	
		// TODO#A7: fill in this function.
		// Hint: For a leaf node, use a normal linear search. Otherwise, search in the left and right children.
		// Another hint: save time by checking if the ray intersects the node first before checking the childrens.
		Ray r = new Ray(rayIn);

		IntersectionRecord rec = new IntersectionRecord();
		boolean hit = false;
		
		if (!node.intersects(r)) return false;
		
		if (node.isLeaf()) {
			// at a leaf node, so do a linear search
			for (int i = node.surfaceIndexStart; i < node.surfaceIndexEnd; i++) {
				if (surfaces[i].intersect(rec, r)) {
					// if we enter this section, then we indeed have an intersection with the i'th surface
					if (rec.t < r.end) {
						if (anyIntersection) return true;
						hit = true;
						// update the end of our ray to be the first intersection
						r.end = rec.t;
						if (outRecord != null) outRecord.set(rec);		// update the output intersection record
					}
				}
			}
			
			return hit;
		} else {
			// is not a leaf, need to call on left and right nodes
			if (intersectHelper(node.child[0], rec, r, anyIntersection)) {
				// at some point, we found an intersection on the left side
				if (anyIntersection) return true;		// if any OK, then let's just return true immediately
				hit = true;								// otherwise, signal that we found something
				r.end = rec.t;							
				if (outRecord != null) outRecord.set(rec);
			}
	
			if (intersectHelper(node.child[1], rec, r, anyIntersection)) {
				// the right side
				if (anyIntersection) return true;
				hit = true;
				r.end = rec.t;
				if (outRecord != null) outRecord.set(rec);
			}
		}
		
        return hit; 
	}


	@Override
	public void build(Surface[] surfaces) {
		this.surfaces = surfaces;
		root = createTree(0, surfaces.length);
	}
	
	/**
	 * Create a BVH [sub]tree.  This tree node will be responsible for storing
	 * and processing surfaces[start] to surfaces[end-1]. If the range is small enough,
	 * this will create a leaf BvhNode. Otherwise, the surfaces will be sorted according
	 * to the axis of the axis-aligned bounding box that is widest, and split into 2
	 * children.
	 * 
	 * @param start The start index of surfaces
	 * @param end The end index of surfaces
	 */
	private BvhNode createTree(int start, int end) {
		// TODO#A7: fill in this function.

		// ==== Step 1 ====
		// Find out the BIG bounding box enclosing all the surfaces in the range [start, end)
		// and store them in minB and maxB.
		// Hint: To find the bounding box for each surface, use getMinBound() and getMaxBound() */
		//Vector3d minBound = surfaces[0].getMinBound();
		//Vector3d maxBound = surfaces[0].getMaxBound();
	
		Vector3d minBound = new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY); 
		Vector3d maxBound = new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		
		// Find the minimum and maximum bounding boxes
		for (int i = start; i < end; i++) {
			Vector3d minForSurface = surfaces[i].getMinBound();
			Vector3d maxForSurface = surfaces[i].getMaxBound();
			minBound = new Vector3d(Math.min(minForSurface.x, minBound.x), 
					Math.min(minForSurface.y, minBound.y), Math.min(minForSurface.z, minBound.z));
			maxBound = new Vector3d(Math.max(maxForSurface.x, maxBound.x), 
					Math.max(maxForSurface.y, maxBound.y), Math.max(maxForSurface.z, maxBound.z));
		}

		// ==== Step 2 ====
		// Check for the base case. 
		// If the range [start, end) is small enough (e.g. less than or equal to 10), just return a new leaf node.
		if ((end - start) <= 10)  return new BvhNode(minBound, maxBound, null, null, start, end);

		// ==== Step 3 ====
		// Figure out the widest dimension (x or y or z).
		// If x is the widest, set widestDim = 0. If y, set widestDim = 1. If z, set widestDim = 2.
		double xWidth = maxBound.x - minBound.x;
		double yWidth = maxBound.y - minBound.y;
		double zWidth = maxBound.z - minBound.z;
		
		int widestDim = 0;						// assumes this will always be set properly
		if (xWidth > yWidth && xWidth > zWidth) {
			// x is greatest
			widestDim = 0;
		} else if (yWidth > xWidth && yWidth > zWidth) {
			// y is greatest
			widestDim = 1;
		} else if (zWidth > xWidth && zWidth > yWidth) {
			// z is greatest
			widestDim = 2;
		}
		// ==== Step 4 ====
		// Sort surfaces according to the widest dimension.
		cmp.setIndex(widestDim);		// set our comparator's compare function
		Arrays.sort(surfaces, start, end, cmp); // sort our surfaces array by our comparator

		// ==== Step 5 ====
		// Recursively create left and right children.
		// Call on left and right half of arrays
		BvhNode left = createTree(start, (start + end)/2);
		BvhNode right = createTree((start + end)/2, end);
        
		return new BvhNode(minBound, maxBound, left, right, start, end);
       // return root;
	}

}

/**
 * A subclass that compares the average position two surfaces by a given
 * axis. Use the setIndex(i) method to select which axis should be considered.
 * i=0 -> x-axis, i=1 -> y-axis, and i=2 -> z-axis.  
 *
 */
class MyComparator implements Comparator<Surface> {
	int index;
	public MyComparator() {  }

	public void setIndex(int index) {
		this.index = index;
	}

	public int compare(Surface o1, Surface o2) {
		double v1 = o1.getAveragePosition().get(index);
		double v2 = o2.getAveragePosition().get(index);
		if(v1 < v2) return 1;
		if(v1 > v2) return -1;
		return 0;
	}

}
