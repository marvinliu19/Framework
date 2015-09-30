package cs4620.mesh;

import java.util.ArrayList;

import egl.NativeMem;
import egl.math.Vector3;
import egl.math.Vector3i;

/**
 * Performs Normals Reconstruction Upon A Mesh Of Positions
 * @author Cristian
 *
 */
public class MeshConverter {
	/**
	 * Reconstruct A Mesh's Normals So That It Appears To Have Sharp Creases
	 * @param positions List Of Positions
	 * @param tris List Of Triangles (A Group Of 3 Values That Index Into The Positions List)
	 * @return A Mesh With Normals That Lie Normal To Faces
	 */
	public static MeshData convertToFaceNormals(ArrayList<Vector3> positions, ArrayList<Vector3i> tris) {
		MeshData data = new MeshData();

		// Notice
		System.out.println("This Feature Has Been Removed For The Sake Of Assignment Consistency");
		System.out.println("This Feature Will Be Added In A Later Assignment");
		
		// Please Do Not Fill In This Function With Code
		
		// After You Turn In Your Assignment, Chuck Norris Will
		// Substitute This Function With His Fiery Will Of Steel
		
		// TODO#A1 SOLUTION START
		
		// Allocate Mesh Data
		data.vertexCount = tris.size() * 3;
		data.indexCount = tris.size() * 3;
		data.positions = NativeMem.createFloatBuffer(data.vertexCount * 3);
		data.normals = NativeMem.createFloatBuffer(data.vertexCount * 3);
		data.indices = NativeMem.createIntBuffer(data.indexCount);
		
		// Loop Through Triangles
		int vertIndex = 0;
		for(Vector3i t : tris) {
			// Compute The Normal
			Vector3 n = new Vector3(positions.get(t.z));
			n.sub(positions.get(t.y));
			n.cross(positions.get(t.x).clone().sub(positions.get(t.y)));
			n.normalize();
			
			// Check For Degenerate Triangle
			if(Float.isNaN(n.x) || Float.isNaN(n.y) || Float.isNaN(n.z)) {
				data.vertexCount -= 3;
				data.indexCount -= 3;
				continue;
			}
			
			// Add A Vertex
			for(int vi = 0;vi < 3;vi++) {
				Vector3 v = positions.get(t.get(vi));
				data.positions.put(v.x); data.positions.put(v.y); data.positions.put(v.z);
				data.normals.put(n.x); data.normals.put(n.y); data.normals.put(n.z);
				data.indices.put(vertIndex++);
			}
		}
		
		// #SOLUTION END

		return data;
	}
	/**
	 * Reconstruct A Mesh's Normals So That It Appears To Be Smooth
	 * @param positions List Of Positions
	 * @param tris List Of Triangles (A Group Of 3 Values That Index Into The Positions List)
	 * @return A Mesh With Normals That Extrude From Vertices
	 */
	public static MeshData convertToVertexNormals(ArrayList<Vector3> positions, ArrayList<Vector3i> tris) {
		// TODO#A1 SOLUTION START
		MeshData data = new MeshData();
		
		data.vertexCount = positions.size();
		data.indexCount = tris.size()*3;
		
		data.positions = NativeMem.createFloatBuffer(positions.size()*3);
		data.normals = NativeMem.createFloatBuffer(positions.size()*3);
		data.indices = NativeMem.createIntBuffer(tris.size()*3);
		
		for (Vector3 vertex : positions) {
			data.positions.put(vertex.x);
			data.positions.put(vertex.y);
			data.positions.put(vertex.z);
		}
		
		for (Vector3i tri : tris) {
			data.indices.put(tri.x);
			data.indices.put(tri.y);
			data.indices.put(tri.z);
		}
		
		
	// instantiate array of vertex normals with zero vectors
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
	// #SOLUTION END
		
		return data;
	}
}
