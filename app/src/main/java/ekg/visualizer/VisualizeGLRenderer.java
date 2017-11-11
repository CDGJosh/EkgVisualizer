package ekg.visualizer;

import android.opengl.*;
import android.opengl.GLES10;

import java.util.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;


public class VisualizeGLRenderer implements GLSurfaceView.Renderer {

    private List<Vector3> vectors;
    private float cubeScale = 1.0f;

    private int width,height;

    private GLCube cube;
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.vectors = new ArrayList<>();

        this.vectors.add(new Vector3(0.0f, 0.0f, +0.5f));
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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        if(this.cube == null)
            this.cube = GLCube.getInstance();

        for(int i = 0; i < this.vectors.size(); i++)
        {
            this.cube.drawAt(this.vectors.get(i).getX(),this.vectors.get(i).getY(),this.vectors.get(i).getZ(), this.cubeScale);
        }

    }

    public void addVector(Vector3 vec)
    {
        this.vectors.add(vec);
    }

    public void removeVectorAt(int i)
    {
        this.vectors.remove(i);
    }

    public void clearVectors()
    {
        this.vectors.clear();
    }

}