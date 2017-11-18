package ekg.visualizer;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class VisualizerGLSurfaceView extends GLSurfaceView {

    private final VisualizeGLRenderer mRenderer;

    public VisualizerGLSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new VisualizeGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
    }

    public VisualizeGLRenderer getRenderer() { return  this.mRenderer; }
}