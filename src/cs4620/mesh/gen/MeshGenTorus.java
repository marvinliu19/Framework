package cs4620.mesh.gen;

import cs4620.common.BasicType;
import cs4620.mesh.MeshData;
import egl.NativeMem;
import egl.math.Matrix4;
import egl.math.Vector3;

/**
 * Generates A Torus Mesh
 * @author Cristian (Original)
 * @author Tongcheng (Revised 8/26/2015)
 */
@SuppressWarnings("unused")
public class MeshGenTorus extends MeshGenerator {
	@Override
	public void generate(MeshData outData, MeshGenOptions opt) {
		// TODO#A1 SOLUTION START
		// set up properties of the sphere
		int n = opt.divisionsLongitude;
		int m = opt.divisionsLatitude;
		float r = opt.innerRadius;
		
		double theta = Math.PI * 2 / m; 
		double phi = Math.PI * 2 / n;
				
		// calculate vertex and index count
		outData.vertexCount = (n+1)*(m+1); // m + 1 vertices per cross section, n + 1 cross sections
		int tris = 2 * m * n;
		outData.indexCount = tris*3; 
		
		// create storage space
		outData.positions = NativeMem.createFloatBuffer(outData.vertexCount * 3);
		outData.uvs = NativeMem.createFloatBuffer(outData.vertexCount * 2);
		outData.normals = NativeMem.createFloatBuffer(outData.vertexCount * 3);
		outData.indices = NativeMem.createIntBuffer(outData.indexCount);
		
		// create the vertices
		float[] positions = new float[outData.vertexCount *3];	
		
		int vertex = 0;
		for (int lon = 0; lon < n+1; lon++) {
			for (int lat = 0; lat < m + 1; lat++) {
				positions[vertex * 3] = (float) ((1 - r * Math.cos(lat*theta)) * Math.sin(lon*phi - Math.PI)); // x
				positions[vertex * 3 + 1] = (float) (r * Math.sin(lat*theta)); // y
				positions[vertex * 3 + 2] = (float) ((1 - r * Math.cos(lat*theta)) * Math.cos(lon*phi - Math.PI)); // z
				
				float u = ((float) lon) / ((float) n);
				float v = ((float) lat) / ((float) (m));
				outData.uvs.put(u);
				outData.uvs.put(v);
				
				vertex++;
			}
			
		}
		
		outData.positions.put(positions);
		
		// Create The Indices
		
		// Right pointing triangles
		for (int i = 0; i < n; i++) {
			for (int t = 0; t < m; t++) {
				outData.indices.put(i*(m+1) + t);
				outData.indices.put(i*(m+1) + t + 1);
				outData.indices.put((i+1)*(m+1) + t);
			}	
		}
		
		// Left pointing triangles
		for (int i = 0; i < n; i++) {
			for (int t = 0; t < m; t++) {
				outData.indices.put(i*(m+1) + t + 1);
				outData.indices.put((i+1)*(m+1) + t + 1);
				outData.indices.put((i+1)*(m+1) + t);
			}	
		}
		
		// instantiate array of vertex normals with zero vectors
		Vector3[] normals = new Vector3[outData.vertexCount];
		for (int i = 0; i < normals.length; i++) {
			normals[i] = new Vector3(0,0,0);
		}
		
		// for each triangle (a set of 3 indices from outData.indices)
		for (int i = 0; i < outData.indexCount; i+=3) {
			// get the index of the first vertex of this triangle
			// get the (x,y,z) of the first vertex of this triangle
			int v1i = outData.indices.get(i);
			Vector3 v1 = new Vector3(outData.positions.get(3*v1i),outData.positions.get(3*v1i+1),outData.positions.get(3*v1i+2));
			
			int v2i = outData.indices.get(i+1);
			Vector3 v2 = new Vector3(outData.positions.get(3*v2i),outData.positions.get(3*v2i+1),outData.positions.get(3*v2i+2));
			
			int v3i = outData.indices.get(i+2);
			Vector3 v3 = new Vector3(outData.positions.get(3*v3i),outData.positions.get(3*v3i+1),outData.positions.get(3*v3i+2));
			
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
			outData.normals.put(normals[i].x);
			outData.normals.put(normals[i].y);
			outData.normals.put(normals[i].z);
		}
		
		// #SOLUTION END
	}
	
	@Override
	public BasicType getType() {
		return BasicType.TriangleMesh; // Ray-casting Slightly More Difficult On A Torus 
	}
}
