package cs4620.anim;

import java.util.HashMap;
import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneTransformationEvent;
import egl.math.Matrix4;
import egl.math.Vector3;
import egl.math.Matrix3;
import egl.math.Quat;

/**
 * A Component Resting Upon Scene That Gives
 * Animation Capabilities
 * @author Cristian
 *
 */
public class AnimationEngine {
	/**
	 * The First Frame In The Global Timeline
	 */
	private int frameStart = 0;
	/**
	 * The Last Frame In The Global Timeline
	 */
	private int frameEnd = 100;
	/**
	 * The Current Frame In The Global Timeline
	 */
	private int curFrame = 0;
	/**
	 * Scene Reference
	 */
	private final Scene scene;
	/**
	 * Animation Timelines That Map To Object Names
	 */
	public final HashMap<String, AnimTimeline> timelines = new HashMap<>();

	/**
	 * An Animation Engine That Works Only On A Certain Scene
	 * @param s The Working Scene
	 */
	public AnimationEngine(Scene s) {
		scene = s;
	}
	
	/**
	 * Set The First And Last Frame Of The Global Timeline
	 * @param start First Frame
	 * @param end Last Frame (Must Be Greater Than The First
	 */
	public void setTimelineBounds(int start, int end) {
		// Make Sure Our End Is Greater Than Our Start
		if(end < start) {
			int buf = end;
			end = start;
			start = buf;
		}
		
		frameStart = start;
		frameEnd = end;
		moveToFrame(curFrame);
	}
	/**
	 * Add An Animating Object
	 * @param oName Object Name
	 * @param o Object
	 */
	public void addObject(String oName, SceneObject o) {
		timelines.put(oName, new AnimTimeline(o));
	}
	/**
	 * Remove An Animating Object
	 * @param oName Object Name
	 */
	public void removeObject(String oName) {
		timelines.remove(oName);
	}

	/**
	 * Set The Frame Pointer To A Desired Frame (Will Be Bounded By The Global Timeline)
	 * @param f Desired Frame
	 */
	public void moveToFrame(int f) {
		if(f < frameStart) f = frameStart;
		else if(f > frameEnd) f = frameEnd;
		curFrame = f;
	}
	/**
	 * Looping Forwards Play
	 * @param n Number Of Frames To Move Forwards
	 */
	public void advance(int n) {
		curFrame += n;
		if(curFrame > frameEnd) curFrame = frameStart + (curFrame - frameEnd - 1);
	}
	/**
	 * Looping Backwards Play
	 * @param n Number Of Frames To Move Backwards
	 */
	public void rewind(int n) {
		curFrame -= n;
		if(curFrame < frameStart) curFrame = frameEnd - (frameStart - curFrame - 1);
	}

	public int getCurrentFrame() {
		return curFrame;
	}
	public int getFirstFrame() {
		return frameStart;
	}
	public int getLastFrame() {
		return frameEnd;
	}
	public int getNumFrames() {
		return frameEnd - frameStart + 1;
	}

	/**
	 * Adds A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void addKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.addKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	/**
	 * Removes A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void removeKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.removeKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	
	/**
	 * Loops Through All The Animating Objects And Updates Their Transformations To
	 * The Current Frame - For Each Updated Transformation, An Event Has To Be 
	 * Sent Through The Scene Notifying Everyone Of The Change
	 */
	 public void updateTransformations() {

		for (String k : timelines.keySet()) {
			AnimTimeline t = timelines.get(k);
			AnimKeyframe[] surrounding = new AnimKeyframe[2];
			t.getSurroundingFrames(getCurrentFrame(), surrounding);
			AnimKeyframe f1 = surrounding[0];
			AnimKeyframe f2 = surrounding[1];
			
			float ratio = getRatio(f1.frame, f2.frame, getCurrentFrame());
			
			Vector3 trans1 = f1.transformation.getTrans();
			Vector3 trans2 = f2.transformation.getTrans();
			Matrix3 rs1 = f1.transformation.getAxes();
			Matrix3 rs2 = f2.transformation.getAxes();
			Matrix3 r1 = new Matrix3();
			Matrix3 s1 = new Matrix3();
			Matrix3 r2 = new Matrix3();
			Matrix3 s2 = new Matrix3();
			rs1.polar_decomp(r1,s1);
			rs2.polar_decomp(r2, s2);
			
			Vector3 trans = trans1.clone().lerp(trans2, ratio);
			Matrix3 scale = s1.clone().interpolate(s1, s2, ratio);
			
			Quat q1 = new Quat(r1);
			Quat q2 = new Quat(r2);
			
			Quat q = Quat.slerp(q1, q2, ratio);
			Matrix3 rotate = q.toRotationMatrix(new Matrix3());
			
			Matrix3 rs = rotate.clone().mulBefore(scale);
			Matrix4 rst = new Matrix4(rs);
			rst.set(0, 3, trans.x);
			rst.set(1, 3, trans.y);
			rst.set(2, 3, trans.z);
			
			t.object.transformation.set(rst);
			this.scene.sendEvent(new SceneTransformationEvent(t.object));
		}
	 }

         public static float getRatio(int min, int max, int cur) {
	     if(min == max) return 0f;
	     float total = max - min;
	     float diff = cur - min;
	     return diff / total;
	 }
}
