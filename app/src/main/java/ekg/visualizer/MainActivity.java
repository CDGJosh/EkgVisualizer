package ekg.visualizer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    VisualizerGLSurfaceView view = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.view = new VisualizerGLSurfaceView(this);
        super.onCreate(savedInstanceState);
        setContentView(this.view);

        VisualizeGLRenderer r = this.view.getRenderer();

        r.setZoom(0.7f);
        r.setRotation(0.3f,0.0f,0.0f);
        r.setAutorotationEnabled(false, true, false);
        r.setAutorotationSpeedDeg(0.0f, 360.0f*5.0f, 0.0f);


        for(int i = 0; i < 100; i++) {
            int val = ThreadLocalRandom.current().nextInt(500, 1499 + 1);

            float x = ((float) ThreadLocalRandom.current().nextInt(val / 10 * 9, val / 10 * 11)) / 1000.0f;
            float y = ((float) ThreadLocalRandom.current().nextInt(val / 10 * 9, val / 10 * 11)) / 1000.0f;
            float z = ((float) ThreadLocalRandom.current().nextInt(val / 10 * 9, val / 10 * 11)) / 1000.0f;

            r.addVector(new Vector3(x, y, z));
        }

        //change color to get completly insane. the urge to puke is big.
        GLCube.getInstance().setCubeColor(1.0f, 0.0f, 0.0f, 0.8f);

        GLCube.getInstance().setCubeOutlineColor(100, 0, 255, 255);

        GLCube.getInstance().setSingleOutlineColor(new Vector4(0.0f, 1.0f, 0.0f, 1.0f));
    }
}


