package cs4620.gl.manip;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blister.input.KeyboardEventDispatcher;
import blister.input.KeyboardKeyEventArgs;
import blister.input.MouseButton;
import blister.input.MouseButtonEventArgs;
import blister.input.MouseEventDispatcher;
import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.UUIDGenerator;
import cs4620.common.event.SceneTransformationEvent;
import cs4620.gl.PickingProgram;
import cs4620.gl.RenderCamera;
import cs4620.gl.RenderEnvironment;
import cs4620.gl.RenderObject;
import cs4620.gl.Renderer;
import cs4620.scene.form.ControlWindow;
import cs4620.scene.form.ScenePanel;
import egl.BlendState;
import egl.DepthState;
import egl.IDisposable;
import egl.RasterizerState;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;
import ext.csharp.ACEventFunc;

public class ManipController implements IDisposable {
  public final ManipRenderer renderer = new ManipRenderer();
  public final HashMap<Manipulator, UUIDGenerator.ID> manipIDs = new HashMap<>();
  public final HashMap<Integer, Manipulator> manips = new HashMap<>();
  
  private final Scene scene;
  private final ControlWindow propWindow;
  private final ScenePanel scenePanel;
  private final RenderEnvironment rEnv;
  private ManipRenderer manipRenderer = new ManipRenderer();
  
  private final Manipulator[] currentManips = new Manipulator[3];
  private RenderObject currentObject = null;
  
  private Manipulator selectedManipulator = null;
  
  /**
   * Is parent mode on?  That is, should manipulation happen in parent rather than object coordinates?
   */
  private boolean parentSpace = false;
  
  /**
   * Last seen mouse position in normalized coordinates
   */
  private final Vector2 lastMousePos = new Vector2();
  
  public ACEventFunc<KeyboardKeyEventArgs> onKeyPress = new ACEventFunc<KeyboardKeyEventArgs>() {
    @Override
    public void receive(Object sender, KeyboardKeyEventArgs args) {
      if(selectedManipulator != null) return;
      switch (args.key) {
      case Keyboard.KEY_T:
        setCurrentManipType(Manipulator.Type.TRANSLATE);
        break;
      case Keyboard.KEY_R:
        setCurrentManipType(Manipulator.Type.ROTATE);
        break;
      case Keyboard.KEY_Y:
        setCurrentManipType(Manipulator.Type.SCALE);
        break;
      case Keyboard.KEY_P:
        parentSpace = !parentSpace;
        break;
      }
    }
  };
  public ACEventFunc<MouseButtonEventArgs> onMouseRelease = new ACEventFunc<MouseButtonEventArgs>() {
    @Override
    public void receive(Object sender, MouseButtonEventArgs args) {
      if(args.button == MouseButton.Right) {
        selectedManipulator = null;
      }
    }
  };
  
  public ManipController(RenderEnvironment re, Scene s, ControlWindow cw) {
    scene = s;
    propWindow = cw;
    Component o = cw.tabs.get("Object");
    scenePanel = o == null ? null : (ScenePanel)o;
    rEnv = re;
    
    // Give Manipulators Unique IDs
    manipIDs.put(Manipulator.ScaleX, scene.objects.getID("ScaleX"));
    manipIDs.put(Manipulator.ScaleY, scene.objects.getID("ScaleY"));
    manipIDs.put(Manipulator.ScaleZ, scene.objects.getID("ScaleZ"));
    manipIDs.put(Manipulator.RotateX, scene.objects.getID("RotateX"));
    manipIDs.put(Manipulator.RotateY, scene.objects.getID("RotateY"));
    manipIDs.put(Manipulator.RotateZ, scene.objects.getID("RotateZ"));
    manipIDs.put(Manipulator.TranslateX, scene.objects.getID("TranslateX"));
    manipIDs.put(Manipulator.TranslateY, scene.objects.getID("TranslateY"));
    manipIDs.put(Manipulator.TranslateZ, scene.objects.getID("TranslateZ"));
    for(Entry<Manipulator, UUIDGenerator.ID> e : manipIDs.entrySet()) {
      manips.put(e.getValue().id, e.getKey());
    }
    
    setCurrentManipType(Manipulator.Type.TRANSLATE);
  }
  @Override
  public void dispose() {
    manipRenderer.dispose();
    unhook();
  }
  
  private void setCurrentManipType(int type) {
    switch (type) {
    case Manipulator.Type.TRANSLATE:
      currentManips[Manipulator.Axis.X] = Manipulator.TranslateX;
      currentManips[Manipulator.Axis.Y] = Manipulator.TranslateY;
      currentManips[Manipulator.Axis.Z] = Manipulator.TranslateZ;
      break;
    case Manipulator.Type.ROTATE:
      currentManips[Manipulator.Axis.X] = Manipulator.RotateX;
      currentManips[Manipulator.Axis.Y] = Manipulator.RotateY;
      currentManips[Manipulator.Axis.Z] = Manipulator.RotateZ;
      break;
    case Manipulator.Type.SCALE:
      currentManips[Manipulator.Axis.X] = Manipulator.ScaleX;
      currentManips[Manipulator.Axis.Y] = Manipulator.ScaleY;
      currentManips[Manipulator.Axis.Z] = Manipulator.ScaleZ;
      break;
    }
  }
  
  public void hook() {
    KeyboardEventDispatcher.OnKeyPressed.add(onKeyPress);
    MouseEventDispatcher.OnMouseRelease.add(onMouseRelease);
  }
  public void unhook() {
    KeyboardEventDispatcher.OnKeyPressed.remove(onKeyPress);    
    MouseEventDispatcher.OnMouseRelease.remove(onMouseRelease);
  }
  
  /**
   * Get the transformation that should be used to draw <manip> when it is being used to manipulate <object>.
   * 
   * This is just the object's or parent's frame-to-world transformation, but with a rotation appended on to 
   * orient the manipulator along the correct axis.  One problem with the way this is currently done is that
   * the manipulator can appear very small or large, or very squashed, so that it is hard to interact with.
   * 
   * @param manip The manipulator to be drawn (one axis of the complete widget)
   * @param mViewProjection The camera (not needed for the current, simple implementation)
   * @param object The selected object
   * @return
   */
  public Matrix4 getTransformation(Manipulator manip, RenderCamera camera, RenderObject object) {
    Matrix4 mManip = new Matrix4();
    
    switch (manip.axis) {
    case Manipulator.Axis.X:
      Matrix4.createRotationY((float)(Math.PI / 2.0), mManip);
      break;
    case Manipulator.Axis.Y:
      Matrix4.createRotationX((float)(-Math.PI / 2.0), mManip);
      break;
    case Manipulator.Axis.Z:
      mManip.setIdentity();
      break;
    }
    if (parentSpace) {
      if (object.parent != null)
        mManip.mulAfter(object.parent.mWorldTransform);
    } else
      mManip.mulAfter(object.mWorldTransform);

    return mManip;
  }
  
  /**
   * Apply a transformation to <b>object</b> in response to an interaction with <b>manip</b> in which the user moved the mouse from
   * <b>lastMousePos</b> to <b>curMousePos</b> while viewing the scene through <b>camera</b>.  The manipulation happens differently depending
   * on the value of ManipController.parentMode; if it is true, the manipulator is aligned with the parent's coordinate system, 
   * or if it is false, with the object's local coordinate system.  
   * @param manip The manipulator that is active (one axis of the complete widget)
   * @param camera The camera (needed to map mouse motions into the scene)
   * @param object The selected object (contains the transformation to be edited)
   * @param lastMousePos The point where the mouse was last seen, in normalized [-1,1] x [-1,1] coordinates.
   * @param curMousePos The point where the mouse is now, in normalized [-1,1] x [-1,1] coordinates.
   */
  public void applyTransformation(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos, Vector2 curMousePos) {
    if (manip.type == Manipulator.Type.ROTATE) {
    	rotate(manip.axis, object, lastMousePos, curMousePos);
    } else if (manip.type == Manipulator.Type.TRANSLATE) {
    	translateOrRotate(manip, camera, object, lastMousePos, curMousePos, true);
    } else {
    	translateOrRotate(manip, camera, object, lastMousePos, curMousePos, false);
    }
  }
  
  private void rotate(int axis, RenderObject object, Vector2 lastMousePos, Vector2 curMousePos) {
    float vertMotion = curMousePos.y - lastMousePos.y;
    Matrix4 rotMat = new Matrix4();
    
    if (axis == Manipulator.Axis.X) {
    	rotMat.set(Matrix4.createRotationX(vertMotion));
    } else if (axis == Manipulator.Axis.Y) {
    	rotMat.set(Matrix4.createRotationY(vertMotion));
    } else {
    	rotMat.set(Matrix4.createRotationZ(vertMotion));
    }
    
    if (parentSpace) {
      object.sceneObject.transformation.mulAfter(rotMat);
    } else {
      object.sceneObject.transformation.mulBefore(rotMat);
    }
  }
  
  private void translateOrRotate(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos, Vector2 curMousePos, Boolean isTranslate) {
    Vector3 manipOrigin = new Vector3();
    Vector3 manipAxis = new Vector3();
    
    // get the manipulator axis in manipulator space
    if (manip.axis == Manipulator.Axis.X) {
    	manipAxis.set(1, 0, 0);
    } else if (manip.axis == Manipulator.Axis.Y) {
    	manipAxis.set(0, 1, 0);
    } else {
    	manipAxis.set(0, 0, 1);
    }
    
    if(parentSpace && object.parent != null) {
      object.parent.mWorldTransform.mulPos(manipOrigin); //origin is now in world space
      object.parent.mWorldTransform.mulDir(manipAxis); //axis is now in world space
    } else if (!parentSpace) {
      object.mWorldTransform.mulPos(manipOrigin); //origin is now in world space
      object.mWorldTransform.mulDir(manipAxis); //axis is now in world space
    }
    
    // compute click points
    Vector3 lastPoint = new Vector3(lastMousePos.x, lastMousePos.y, -1); // canonical view space
    Vector3 curPoint = new Vector3(curMousePos.x, curMousePos.y, -1); // canonical view space

    Matrix4 canonicalViewToWorld = camera.mViewProjection.clone().invert();
    canonicalViewToWorld.mulPos(lastPoint); // last mouse click in world space
    canonicalViewToWorld.mulPos(curPoint); // current mouse click in world space
    
    // project rays from manipOrigin to click points onto the manip axis to find t values
    float t1 = curPoint.clone().sub(manipOrigin).dot(manipAxis);
    float t2 = lastPoint.clone().sub(manipOrigin).dot(manipAxis);
    
    float translationAmount = (t1-t2) * (float) 500;
    float scaleAmount = (t1/t2);
    
    // amplify the scale amount so that it can be seen
    for (int i = 0; i < 100; i++) {
    	 if (Math.abs(scaleAmount-1) < 0.01) {
    	    	scaleAmount *= scaleAmount;
    	 } else {
    		 break;
    	 }
    }
   
    // set the transformation vector
    Vector3 transformation = new Vector3();
    if (manip.axis == Manipulator.Axis.X && isTranslate) {
    	transformation.set(translationAmount, 0, 0);
    } else if (manip.axis == Manipulator.Axis.X && !isTranslate) {
    	transformation.set(scaleAmount, 1, 1);
    } else if (manip.axis == Manipulator.Axis.Y && isTranslate) {
    	transformation.set(0, translationAmount, 0);
    } else if (manip.axis == Manipulator.Axis.Y && !isTranslate) {
    	transformation.set(1, scaleAmount, 1);
    } else if (manip.axis == Manipulator.Axis.Z && isTranslate) {
    	transformation.set(0, 0, translationAmount);
    } else {
    	transformation.set(1, 1, scaleAmount);
    }
    
    // apply either translation or scaling
    Matrix4 m = (isTranslate) ? Matrix4.createTranslation(transformation) : Matrix4.createScale(transformation);
    
    if (parentSpace) {
      object.sceneObject.transformation.mulAfter(m);
    } else {
      object.sceneObject.transformation.mulBefore(m);
    }
    
  }
  
  public void checkMouse(int mx, int my, RenderCamera camera) {
    Vector2 curMousePos = new Vector2(mx, my).add(0.5f).mul(2).div(camera.viewportSize.x, camera.viewportSize.y).sub(1);
    if(curMousePos.x != lastMousePos.x || curMousePos.y != lastMousePos.y) {
      if(selectedManipulator != null && currentObject != null) {
        applyTransformation(selectedManipulator, camera, currentObject, lastMousePos, curMousePos);
        scene.sendEvent(new SceneTransformationEvent(currentObject.sceneObject));
      }
      lastMousePos.set(curMousePos);
    }
  }

  public void checkPicking(Renderer renderer, RenderCamera camera, int mx, int my) {
    if(camera == null) return;
    
    // Pick An Object
    renderer.beginPickingPass(camera);
    renderer.drawPassesPick();
    if(currentObject != null) {
      // Draw Object Manipulators
      GL11.glClearDepth(1.0);
      GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
      
      DepthState.DEFAULT.set();
      BlendState.OPAQUE.set();
      RasterizerState.CULL_NONE.set();
      
      drawPick(camera, currentObject, renderer.pickProgram);
    }
    int id = renderer.getPickID(Mouse.getX(), Mouse.getY());
    
    selectedManipulator = manips.get(id);
    if(selectedManipulator != null) {
      // Begin Manipulator Operations
      System.out.println("Selected Manip: " + selectedManipulator.type + " " + selectedManipulator.axis);
      return;
    }
    
    SceneObject o = scene.objects.get(id);
    if(o != null) {
      System.out.println("Picked An Object: " + o.getID().name);
      if(scenePanel != null) {
        scenePanel.select(o.getID().name);
        propWindow.tabToForefront("Object");
      }
      currentObject = rEnv.findObject(o);
    }
    else if(currentObject != null) {
      currentObject = null;
    }
  }
  
  public RenderObject getCurrentObject() {
    return currentObject;
  }
  
  public void draw(RenderCamera camera) {
    if(currentObject == null) return;
    
    DepthState.NONE.set();
    BlendState.ALPHA_BLEND.set();
    RasterizerState.CULL_CLOCKWISE.set();
    
    for(Manipulator manip : currentManips) {
      Matrix4 mTransform = getTransformation(manip, camera, currentObject);
      manipRenderer.render(mTransform, camera.mViewProjection, manip.type, manip.axis);
    }
    
    DepthState.DEFAULT.set();
    BlendState.OPAQUE.set();
    RasterizerState.CULL_CLOCKWISE.set();
    
    for(Manipulator manip : currentManips) {
      Matrix4 mTransform = getTransformation(manip, camera, currentObject);
      manipRenderer.render(mTransform, camera.mViewProjection, manip.type, manip.axis);
    }

}
  public void drawPick(RenderCamera camera, RenderObject ro, PickingProgram prog) {
    for(Manipulator manip : currentManips) {
      Matrix4 mTransform = getTransformation(manip, camera, ro);
      prog.setObject(mTransform, manipIDs.get(manip).id);
      manipRenderer.drawCall(manip.type, prog.getPositionAttributeLocation());
    }
  }
  
}
