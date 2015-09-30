package cs4620.ray1.surface;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Vector3d;
import egl.math.Vector3i;
import cs4620.ray1.shader.Shader;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
  /** The normal vector of this triangle, if vertex normals are not specified */
  Vector3d norm;
  
  /** The mesh that contains this triangle */
  Mesh owner;
  
  /** 3 indices to the vertices of this triangle. */
  Vector3i index;
  
  double a, b, c, d, e, f;
  public Triangle(Mesh owner, Vector3i index, Shader shader) {
    this.owner = owner;
    this.index = new Vector3i(index);
    
    Vector3d v0 = owner.getPosition(index.x);
    Vector3d v1 = owner.getPosition(index.y);
    Vector3d v2 = owner.getPosition(index.z);
    
    if (!owner.hasNormals()) {
    	Vector3d e0 = new Vector3d(), e1 = new Vector3d();
    	e0.set(v1).sub(v0);
    	e1.set(v2).sub(v0);
    	norm = new Vector3d();
    	norm.set(e0).cross(e1);
    }
    a = v0.x-v1.x;
    b = v0.y-v1.y;
    c = v0.z-v1.z;
    
    d = v0.x-v2.x;
    e = v0.y-v2.y;
    f = v0.z-v2.z;
    
    this.setShader(shader);
  }

  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param rayIn the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    // TODO#A2: fill in this function.
    Vector3d vA = owner.getPosition(index.x).clone();
    
    double g = rayIn.direction.x;
    double h = rayIn.direction.y;
    double i = rayIn.direction.z;
    double j = vA.x - rayIn.origin.x; 
    double k = vA.y - rayIn.origin.y;
    double l = vA.z - rayIn.origin.z;
    
    
    double m = a*(e*i - h*f) + b*(g*f - d*i) + c*(d*h - e*g);
    
    double t = -(f*(a*k - j*b) + e*(j*c - a*l) + d*(b*l - k*c))/m;
    if (t > rayIn.end || t < rayIn.start) return false;
    
    double beta = (j*(e*i - h*f) + k*(g*f - d*i) + l*(d*h - e*g))/m;
    double gamma = (i*(a*k - j*b) + h*(j*c - a*l) + g*(b*l - k*c))/m;

    if (beta > 0 && gamma > 0 && (beta + gamma) < 1) {
      Vector3d intersect = new Vector3d(rayIn.direction).mul(t).add(rayIn.origin);
      outRecord.location.set(intersect);
      
      if (norm != null) {
        outRecord.normal.set(norm.normalize());
      } else {
        Vector3d nA = owner.getNormal(index.x).clone();
        Vector3d nB = owner.getNormal(index.y).clone();
        Vector3d nC = owner.getNormal(index.z).clone();
        
        nA.mul(1 - beta - gamma);
        nB.mul(beta);
        nC.mul(gamma);
        outRecord.normal.set(nA.add(nB.add(nC)).normalize());
      }   
      outRecord.t = t;
      outRecord.surface = this;
      return true;
    } 
    return false;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Triangle ";
  }
}