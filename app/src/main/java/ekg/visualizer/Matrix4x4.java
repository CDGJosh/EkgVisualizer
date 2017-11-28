package ekg.visualizer;

/**
 * Created by Josh on 28.11.2017.
 */

public class Matrix4x4 {
    private float matrix[] = new float[16];

    public Matrix4x4(float x1, float x2, float x3, float x4,
                     float y1, float y2, float y3, float y4,
                     float z1, float z2, float z3, float z4,
                     float w1, float w2, float w3, float w4)
    {
        this.matrix = new float[]{x1,x2,x3,x4, y1,y2,y3,y4, z1,z2,z3,z4, w1,w2,w3,w4};
    }

    public Matrix4x4(float[] val)
    {
        this.matrix = val;
    }

    private float get(int r, int c)
    {
        return this.matrix[r * 4 + c];
    }

    public float[] getUnfiorm()
    {
        return this.matrix;
    }


    public Matrix4x4 multiply(Matrix4x4 other)
    {
        float[] nm = new float[16];

        for(int r = 0; r < 4; r++)
        {
            for(int c = 0; c < 4; c++)
            {
                nm[r * 4 + c] = matrix[r * 4 + 0] * other.get(0, c) + matrix[r * 4 + 1] * other.get(1, c) + matrix[r * 4 + 2] * other.get(2, c) + matrix[r * 4 + 3] * other.get(3, c);
            }
        }
        return new Matrix4x4(nm);
    }
}
