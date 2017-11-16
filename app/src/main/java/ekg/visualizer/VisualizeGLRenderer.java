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

    private List<Vector3> vectors;
    private float cubeScale = .02f;
    private Vector3 rotation = new Vector3(0.3f, 0.0f,0.0f);
    private Vector3 center = new Vector3(1.0f, 1.0f,1.0f);

    private float zoom = 0.7f;

    private int width,height;

    private GLCube cube;
    private GLAxisSystem axisSystem;


    private long last;
    private int passed;
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.vectors = new ArrayList<>();

        this.vectors.add(new Vector3(0.96f, 0.96f, +0.96f));
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

        this.rotation.setY(this.rotation.getY() - 3.14f/10000.0f * passed);
        if(this.rotation.getY() < -6.28)
            this.rotation.setY(0.0f);

        this.last = tmp;
        if(this.passed > 00 && this.vectors.size() < 500)
        {

            int val = ThreadLocalRandom.current().nextInt(500, 1499 + 1);

            //float x = ((float)ThreadLocalRandom.current().nextInt(500, 1499 + 1)) / 1000.0f;
            //float y = ((float)ThreadLocalRandom.current().nextInt(500, 1499 + 1)) / 1000.0f;
            //float z = ((float)ThreadLocalRandom.current().nextInt(500, 1499 + 1)) / 1000.0f;

            float x = ((float)ThreadLocalRandom.current().nextInt(val/10 *9, val/10 *11)) / 1000.0f;
            float y = ((float)ThreadLocalRandom.current().nextInt(val/10 *9, val/10 *11)) / 1000.0f;
            float z = ((float)ThreadLocalRandom.current().nextInt(val/10 *9, val/10 *11)) / 1000.0f;

            this.vectors.add(new Vector3(x,y,z));
            System.out.println(z);
        }
        this.passed = 0;

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        glEnable(GL_DEPTH_TEST);
        //glDisable(GL_DEPTH_TEST);

        if(this.cube == null)
            this.cube = GLCube.getInstance();

        if(this.axisSystem == null)
            this.axisSystem = GLAxisSystem.getInstance();

        this.axisSystem.drawAt(this.rotation.getX(), this.rotation.getY(), this.rotation.getZ(),
                               this.center.getX(), this.center.getY(), this.center.getZ(),
                               this.cubeScale, this.zoom);
        for(int i = 0; i < this.vectors.size(); i++)
        {
            this.cube.drawAt(this.vectors.get(i).getX(),this.vectors.get(i).getY(),this.vectors.get(i).getZ(),
                             this.rotation.getX(), this.rotation.getY(), this.rotation.getZ(),
                             this.center.getX(), this.center.getY(), this.center.getZ(),
                             this.cubeScale, this.zoom);
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