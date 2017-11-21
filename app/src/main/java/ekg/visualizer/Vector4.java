package ekg.visualizer;

/**
 * Created by Josh on 11.11.2017.
 */

public class Vector4 {
    private float x,y,z,w;

    public float getX() {
        return x;
    }

    public float getR() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public float getG() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public float getB() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getW() {  return w;}

    public float getA() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public Vector4(float x, float y, float z, float w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

}
