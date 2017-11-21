package ekg.visualizer;

import android.opengl.*;
import android.opengl.GLES10;

import java.util.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.concurrent.ThreadLocalRandom;

import static android.opengl.GLES20.*;


public class VisualizeGLRenderer implements GLSurfaceView.Renderer {

    private static final int AXIS_X = 0;
    private static final int AXIS_Y = 1;
    private static final int AXIS_Z = 2;

    private List<Vector3> vectors = new ArrayList<>();
    private float cubeScale = .02f;
    private Vector3 rotation = new Vector3(0.3f, 0.0f,0.0f);
    private Vector3 center = new Vector3(1.0f, 1.0f,1.0f);

    private float zoom = 1.0f;

    private int width,height;

    private GLCube cube;
    private GLAxisSystem axisSystem;

    private boolean[] autorotationEnabled = new boolean[] {false, false, false};
    private Vector3 autorotationSpeed = new Vector3(0.0f, 0.0f, 0.0f);

    private long last;
    private int passed;
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        this.last = System.currentTimeMillis();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0, 0, width, height);

        this.width = width;
        this.height = height;
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        long tmp = System.currentTimeMillis();
        this.passed += tmp - this.last;

        if(this.autorotationEnabled[AXIS_X])
            this.rotation.setX(this.rotation.getX() - (this.getAutorotationSpeed().getX()/60000.0f) * passed);
        if(this.rotation.getX() < -6.28 || this.rotation.getX() > 6.28)
            this.rotation.setX(0.0f);

        if(this.autorotationEnabled[AXIS_Y])
            this.rotation.setY(this.rotation.getY() - (this.getAutorotationSpeed().getY()/60000.0f) * passed);
        if(this.rotation.getY() < -6.28 || this.rotation.getY() > 6.28)
            this.rotation.setY(0.0f);

        if(this.autorotationEnabled[AXIS_Z])
            this.rotation.setZ(this.rotation.getZ() - (this.getAutorotationSpeed().getZ()/60000.0f) * passed);
        if(this.rotation.getZ() < -6.28 || this.rotation.getZ() > 6.28)
            this.rotation.setZ(0.0f);


        this.last = tmp;
        this.passed = 0;

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        glEnable(GL_DEPTH_TEST);
        //glDisable(GL_DEPTH_TEST);

        if(this.cube == null) {
            this.cube = GLCube.getInstance();
            this.cube.init();
        }

        if(this.axisSystem == null)
            this.axisSystem = GLAxisSystem.getInstance();



        this.cube.drawOutlineAt(1.0f,1.0f,1.0f,
                this.rotation.getX(), this.rotation.getY(), this.rotation.getZ(),
                this.center.getX(), this.center.getY(), this.center.getZ(),
                1.0f, this.zoom);

        for(int i = 0; i < this.vectors.size(); i++)
        {
            this.cube.drawAt(this.vectors.get(i).getX(),this.vectors.get(i).getY(),this.vectors.get(i).getZ(),
                             this.rotation.getX(), this.rotation.getY(), this.rotation.getZ(),
                             this.center.getX(), this.center.getY(), this.center.getZ(),
                             this.cubeScale, this.zoom);
        }

    }

    /**
    * Adds a Vector3 which should be displayed
    *
    * @param  vec  the new Vector3 that should be displayed
    * */
    public void addVector(Vector3 vec)
    {
        this.vectors.add(vec);
    }

    /**
     * Removes a Vector at given Index i
     *
     * @param  i  the index of the vector which should be removed
     * */
    public void removeVectorAt(int i)
    {
        this.vectors.remove(i);
    }

    /**
     * Removes all vectors
     *
     * */
    public void clearVectors()
    {
        this.vectors.clear();
    }

    /**
     * Retrieves rotation around each axis
     *
     * @return  removes a Vector3 which contains the rotation around every axis in rad
     * */
    public Vector3 getRotation() { return this.rotation; }

    /**
     * Sets rotation around every axis in rad
     *
     * @param  x  the rotation around x axis in rad
     * @param  y  the rotation around y axis in rad
     * @param  z  the rotation around z axis in rad
     * */
    public void setRotation(float x, float y, float z) { this.rotation.setX(x); this.rotation.setY(y); this.rotation.setZ(z); }

    private float degToRad(float deg) { return  ((2.0f*(float)Math.PI)/360.0f) * deg; }

    /**
     * Sets rotation around every axis in deg
     *
     * @param  x  the rotation around x axis in deg
     * @param  y  the rotation around y axis in deg
     * @param  z  the rotation around z axis in deg
     * */
    public void setRotationDeg(float x, float y, float z) { this.rotation.setX(degToRad(x)); this.rotation.setY(degToRad(y)); this.rotation.setZ(degToRad(z)); }

    /**
     * Retrieves the specified center (of rotation) in ms
     *
     * @return  a Vector3 which specifies the center in ms
     * */
    public Vector3 getCenter() { return this.center; }

    /**
     * Sets the center (of rotation) in ms
     *
     * @param  x  the x value of the center in ms
     * @param  y  the y value of the center in ms
     * @param  z  the z value of the center in ms
     * */
    public void setCenter(float x, float y, float z) { this.center.setX(x); this.center.setY(y); this.center.setZ(z); }

    /**
     * Sets the center (of rotation)
     *
     * @param  center  a Vector3 which contains the center
     * */
    public void setCenter(Vector3 center) { this.center = center; }

    /**
     * Gets the zoom of the scene
     * Default: 1.0
     * Nearer: >1.0
     * Farer: <1.0
     * Invisible: 0.0
     *
     * @return   the zoom value
     * */
    public float getZoom() { return this.zoom; }

    /**
     * Sets the zoom of the scene
     * Default: 1.0
     * Nearer: >1.0
     * Farer: <1.0
     * Invisible: 0.0
     *
     * @param zoom the zoom value
     * */
    public void setZoom(float zoom) { this.zoom = zoom; }

    /**
     * Gets the current size of the cubes
     * Default: 0.02
     * Bigger: >0.02
     * Smaller: <0.02
     * Invisible: 0.0
     *
     * @return  the current size of the cubes
     * */
    public float getCubeScale() { return this.cubeScale; }

    /**
     * Sets the size of the cubes
     * Default: 0.02
     * Bigger: >0.02
     * Smaller: <0.02
     * Invisible: 0.0
     *
     * @param cubeScale the zoom value
     * */
    public void setCubeScale(float cubeScale) { this.cubeScale = cubeScale; }

    /**
     * Gets a Vector3 which contains the autorotation speed of every axis in rad/min
     *
     * @return  a Vector3 which containing the autorotation speed of every axis in rad/min
     * */
    public Vector3 getAutorotationSpeed() { return this.autorotationSpeed; }

    /**
     * Sets the autorotation speed of every axis in rad/min
     * to change rotation direction, just negate the value
     *
     * @param  x  the autorotation speed around the x axis in rad/min
     * @param  y  the autorotation speed around the y axis in rad/min
     * @param  z  the autorotation speed around the z axis in rad/min
     * */
    public void setAutorotationSpeed(float x, float y, float z) { this.autorotationSpeed.setX(x); this.autorotationSpeed.setY(y); this.autorotationSpeed.setZ(z); }

    /**
     * Sets the autorotation speed of every axis in deg/min
     * to change rotation direction, just negate the value
     *
     * @param  x  the autorotation speed around the x axis in deg/min
     * @param  y  the autorotation speed around the y axis in deg/min
     * @param  z  the autorotation speed around the z axis in deg/min
     * */
    public void setAutorotationSpeedDeg(float x, float y, float z) { this.autorotationSpeed.setX(degToRad(x)); this.autorotationSpeed.setY(degToRad(y)); this.autorotationSpeed.setZ(degToRad(z)); }

    /**
     * Returns if the autorotation around the x axis is enabled
     *
     * @return  boolean indicating if autorotation around x axis is enabled
     * */
    public boolean isXAutorotaionEnabled() { return  this.autorotationEnabled[AXIS_X]; }

    /**
     * Returns if the autorotation around the y axis is enabled
     *
     * @return  boolean indicating if autorotation around y axis is enabled
     * */
    public boolean isYAutorotaionEnabled() { return  this.autorotationEnabled[AXIS_Y]; }

    /**
     * Returns if the autorotation around the z axis is enabled
     *
     * @return  boolean indicating if autorotation around z axis is enabled
     * */
    public boolean isZAutorotaionEnabled() { return  this.autorotationEnabled[AXIS_Z]; }

    /**
     * Enables/disables the autorotation around the given axis
     *
     * @param  x  Enables/disables the autorotation around the x axis
     * @param  y  Enables/disables the autorotation around the y axis
     * @param  z  Enables/disables the autorotation around the z axis
     * */
    public void setAutorotationEnabled(boolean x, boolean y, boolean z) { this.autorotationEnabled[AXIS_X] = x; this.autorotationEnabled[AXIS_Y] = y; this.autorotationEnabled[AXIS_Z] = z;}

}