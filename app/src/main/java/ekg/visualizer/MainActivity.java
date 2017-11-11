package ekg.visualizer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    VisualizerGLSurfaceView view = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.view = new VisualizerGLSurfaceView(this);
        super.onCreate(savedInstanceState);
        setContentView(this.view);
    }
}


