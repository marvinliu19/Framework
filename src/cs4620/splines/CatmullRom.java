/**
 * @author Jimmy, Andrew 
 */

package cs4620.splines;
import java.util.ArrayList;

import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector4;

public class CatmullRom extends SplineCurve {

	public CatmullRom(ArrayList<Vector2> controlPoints, boolean isClosed,
			float epsilon) throws IllegalArgumentException {
		super(controlPoints, isClosed, epsilon);
	}

	@Override
	public CubicBezier toBezier(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3,
			float eps) {
		
		Vector4 px = new Vector4(p0.x, p1.x, p2.x, p3.x);
		Vector4 py = new Vector4(p0.y, p1.y, p2.y, p3.y);
		
//		Matrix4 cM = new Matrix4(0.0f, 1.0f, 0.0f, 0.0f,
//								-0.5f, 0.0f, 0.5f, 0.0f,
//								1.0f, -2.5f, 2.0f, -0.5f,
//								-0.5f, 1.5f, -1.5f, 0.5f);
//		
//		Matrix4 bM = new Matrix4(1, 0, 0, 0,
//								-3, 3, 0, 0,
//								3, -6, 3, 0,
//								-1, 3, -3, 1);
		
		Matrix4 cM = new Matrix4(-0.5f, 1.5f, -1.5f, 0.5f,
								1.0f, -2.5f, 2.0f, -0.5f,
								-0.5f, 0.0f, 0.5f, 0.0f,
								0.0f, 1.0f, 0.0f, 0.0f);

		Matrix4 bM = new Matrix4(-1, 3, -3, 1,
								3, -6, 3, 0,
								-3, 3, 0, 0,
								1, 0, 0, 0);
		
		bM.invert();
				
		bM.mul(cM.mul(px));
		bM.mul(cM.mul(py));
		
		return new CubicBezier(new Vector2(px.x, py.x), new Vector2(px.y, py.y), new Vector2(px.z, py.z), new Vector2(px.w, py.w), eps);
	}
}
